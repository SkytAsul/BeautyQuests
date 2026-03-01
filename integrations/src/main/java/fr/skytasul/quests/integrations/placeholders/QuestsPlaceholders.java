package fr.skytasul.quests.integrations.placeholders;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.Utils;
import me.clip.placeholderapi.events.ExpansionRegisterEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestsPlaceholders extends PlaceholderExpansion implements Listener {

	private static QuestsPlaceholders placeholders;

	private static final Pattern QUEST_PLACEHOLDER_PATTERN =
			Pattern.compile("quest_(\\d+)_(\\w+)");
	private static final Pattern PLAYER_QUEST_PLACEHOLDER_PATTERN =
			Pattern.compile("player_quest_(\\d+)_(\\w+)");
	private static final Pattern POOL_PLACEHOLDER_PATTERN =
			Pattern.compile("pool_(\\d+)_(can_start|can_start_reason|remaining|in_progress|completed|cooldown)");
	private static final Pattern STARTED_ORDERED_PATTERN =
			Pattern.compile("started_ordered(?:_(?:(?<id>\\d+)|(?<desc>\\w+)))?");

	private final int lineLength;
	private final int changeTime;
	private final String splitFormat;
	private final String inlineFormat;

	private BukkitTask task;
	private Map<Player, PlayerPlaceholderData> players = new HashMap<>();
	private ReadWriteLock playersLock = new ReentrantReadWriteLock();

	private List<Entry<String, Consumer<PlaceholderExpansion>>> waitingExpansions = new ArrayList<>();

	private QuestsPlaceholders(ConfigurationSection placeholderConfig) {
		lineLength = placeholderConfig.getInt("lineLength");
		changeTime = placeholderConfig.getInt("changeTime");
		splitFormat = placeholderConfig.getString("splitPlaceholderFormat");
		inlineFormat = placeholderConfig.getString("inlinePlaceholderFormat");
	}

	public static void registerPlaceholders(ConfigurationSection placeholderConfig) {
		placeholders = new QuestsPlaceholders(placeholderConfig);
		placeholders.register();
		Bukkit.getPluginManager().registerEvents(placeholders, QuestsPlugin.getPlugin());
		QuestsPlugin.getPlugin().getLoggerExpanded().info("Placeholders registered !");
	}

	public static void waitForExpansion(String identifier, Consumer<PlaceholderExpansion> callback) {
		placeholders.waitingExpansions.add(new AbstractMap.SimpleEntry<>(identifier, callback));
	}

	@Override
	public String getAuthor() {
		return QuestsPlugin.getPlugin().getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return "beautyquests";
	}

	@Override
	public String getVersion() {
		return QuestsPlugin.getPlugin().getDescription().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public List<String> getPlaceholders() {
		var placeholders = new ArrayList<String>();
		placeholders.addAll(List.of("total_amount"));
		placeholders.addAll(List.of("player_inprogress_amount", "player_finished_amount", "player_finished_total_amount", "player_quest_finished_ID"));
		placeholders.addAll(List.of("started", "started_ordered", "started_ordered_X", "started_id_list"));
		placeholders.addAll(List.of("advancement_ID", "advancement_ID_raw"));
		placeholders.addAll(List.of("pool_ID_can_start", "pool_ID_can_start_reason", "pool_ID_remaining", "pool_ID_in_progress", "pool_ID_completed", "pool_ID_cooldown"));
		for (var questOptionCreator : QuestsAPI.getAPI().getQuestOptions())
			placeholders.add("quest_ID_" + questOptionCreator.id);
		for (var questOptionCreator : QuestsAPI.getAPI().getQuestOptions())
			if (QuestDescriptionProvider.class.isAssignableFrom(questOptionCreator.optionClass)) {
				// TODO rework this atrocious code when refactoring options (fetch the data from a registry)
				var fakeOption = (QuestDescriptionProvider) questOptionCreator.optionSupplier.get();
				placeholders.add("player_quest_ID_" + fakeOption.getDescriptionId());
				placeholders.add("started_ordered_" + fakeOption.getDescriptionId());
			}
		placeholders.sort(String::compareTo);
		return placeholders;
	}

	@Override
	public String onRequest(OfflinePlayer off, String identifier) {
		if (identifier.equals("total_amount"))
			return "" + QuestsAPI.getAPI().getQuestsManager().getQuests().size();

		var questMatcher = QUEST_PLACEHOLDER_PATTERN.matcher(identifier);
		if (questMatcher.matches()) {
			int questId = Integer.parseInt(questMatcher.group(1));
			Quest quest = QuestsAPI.getAPI().getQuestsManager().getQuest(questId);
			if (quest == null)
				return "error: unknown quest %d".formatted(questId);

			String optionId = questMatcher.group(2);
			var optionCreatorOpt =
					QuestsAPI.getAPI().getQuestOptions().stream().filter(option -> option.id.equals(optionId)).findAny();
			if (optionCreatorOpt.isPresent()) {
				try {
					return quest.getOption(optionCreatorOpt.get().optionClass).getValueString();
				} catch (IllegalArgumentException __) {
					return "default value";
				}
			}
		}

		if (!off.isOnline())
			return "§cerror: offline";
		Player p = off.getPlayer();
		PlayerQuester quester = QuestsPlugin.getPlugin().getPlayersManager().getQuester(p);

		if (quester == null)
			return "§cdatas not loaded";
		if (identifier.equals("player_inprogress_amount"))
			return Long.toString(
					quester.getDataHolder().getAllQuestsData().stream().filter(QuesterQuestData::hasStarted).count());
		if (identifier.equals("player_finished_amount"))
			return Long.toString(
					quester.getDataHolder().getAllQuestsData().stream().filter(QuesterQuestData::hasFinishedOnce).count());
		if (identifier.equals("player_finished_total_amount"))
			return Integer.toString(
					quester.getDataHolder().getAllQuestsData().stream().mapToInt(QuesterQuestData::getTimesFinished).sum());
		if (identifier.equals("started_id_list"))
			return quester.getDataHolder().getAllQuestsData().stream().filter(QuesterQuestData::hasStarted)
					.map(x -> Integer.toString(x.getQuestId())).collect(Collectors.joining(";"));

		if (identifier.equals("started")) {
			return quester.getDataHolder().getAllQuestsData()
					.stream()
					.filter(QuesterQuestData::hasStarted)
					.map(QuesterQuestData::getQuest)
					.filter(Objects::nonNull)
					.filter(Quest::isScoreboardEnabled)
					.map(quest -> {
						String desc = quest.getDescriptionLine(quester, DescriptionSource.PLACEHOLDER);
						return inlineFormat
								.replace("{questName}", quest.getName())
								.replace("{questDescription}", desc);
					})
					.collect(Collectors.joining("\n"));
		}

		Matcher matcher = STARTED_ORDERED_PATTERN.matcher(identifier);
		if (matcher.matches())
			return getStartedOrderedPlaceholder(matcher, quester);

		if (identifier.startsWith("advancement_")) {
			int rawIndex = identifier.indexOf("_raw");
			String sid = rawIndex == -1 ? identifier.substring(12) : identifier.substring(12, rawIndex);
			try {
				Quest qu = QuestsAPI.getAPI().getQuestsManager().getQuest(Integer.parseInt(sid));
				if (qu == null) return "§c§lError: unknown quest §o" + sid;
				if (rawIndex == -1) {
					if (qu.hasStarted(quester)) {
						return qu.getDescriptionLine(quester, DescriptionSource.PLACEHOLDER);
					}
					if (qu.hasFinished(quester))
						return Lang.Finished.quickFormat("times_finished",
								quester.getDataHolder().getQuestData(qu).getTimesFinished());
					return Lang.Not_Started.toString();
				}else {
					return quester.getDataHolder().getQuestDataIfPresent(qu).map(data -> {
						return switch (data.getState()) {
							case IN_END -> "end";
							case IN_ENDING_STAGES -> "end_stages";
							case IN_REGULAR_STAGE -> Integer.toString(data.getStage().orElseThrow());
							case NOT_STARTED -> "-1";
						};
					}).orElse("-1");
				}
			}catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid + " not a number";
			}
		}
		if (identifier.startsWith("player_quest_finished_")) {
			String sid = identifier.substring(22);
			try {
				Quest qu = QuestsAPI.getAPI().getQuestsManager().getQuest(Integer.parseInt(sid));
				if (qu == null) return "§c§lError: unknown quest §o" + sid;
				return quester.getDataHolder().getQuestDataIfPresent(qu).map(data -> data.getTimesFinished()).orElse(0)
						.toString();
			}catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid;
			}
		}

		var playerQuestMatcher = PLAYER_QUEST_PLACEHOLDER_PATTERN.matcher(identifier);
		if (playerQuestMatcher.matches()) {
			int questId = Integer.parseInt(playerQuestMatcher.group(1));
			Quest quest = QuestsAPI.getAPI().getQuestsManager().getQuest(questId);
			if (quest == null)
				return "error: unknown quest %d".formatted(questId);

			String descriptionId = playerQuestMatcher.group(2);
			var descriptionProviderOpt =
					quest.getDescriptions().stream().filter(desc -> desc.getDescriptionId().equals(descriptionId)).findAny();
			if (descriptionProviderOpt.isPresent()) {
				var listCategory = quest.hasStarted(quester) ? PlayerListCategory.IN_PROGRESS
						: (quest.hasFinished(quester) ? PlayerListCategory.FINISHED : PlayerListCategory.NOT_STARTED);
				List<String> descriptionLines = descriptionProviderOpt.get().provideDescription(new QuestDescriptionContext(
						QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
						quest, p, quester, listCategory,
						DescriptionSource.PLACEHOLDER));
				return String.join("\n", descriptionLines);
			}
		}

		var poolMatcher = POOL_PLACEHOLDER_PATTERN.matcher(identifier);
		if (poolMatcher.matches()) {
			int poolId = Integer.parseInt(poolMatcher.group(1));
			QuestPoolController pool = QuestsAPI.getAPI().getPoolsManager().getPool(poolId);
			if (pool == null)
				return "error: unknown pool %d".formatted(poolId);

			return switch (poolMatcher.group(2)) {
				case "can_start" -> pool.canGive(p).isSuccess() ? "true" : "false";
				case "can_start_reason" -> pool.canGive(p).type().name();
				case "remaining" -> Integer.toString(pool.getQuestsRemaining(quester).size());
				case "in_progress" -> Integer.toString(pool.getQuestsInProgress(quester).size());
				case "completed" -> Integer.toString(pool.getQuestsCompleted(quester).size());
				case "cooldown" -> Utils.millisToHumanString(pool.getRemainingCooldown(quester).orElse(0));
				default -> throw new UnsupportedOperationException();
			};
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String getStartedOrderedPlaceholder(Matcher matcher, PlayerQuester quester) {
		Player p = quester.getPlayer().orElseThrow();

		if (task == null)
			launchTask();

		playersLock.readLock().lock();
		try {
			PlayerPlaceholderData data = players.get(p);

			if (data == null) {
				data = new PlayerPlaceholderData();
				players.put(p, data);
			}

			if (data.left.isEmpty()) {
				data.left = QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(quester, false, true);
			} else
				QuestsAPI.getAPI().getQuestsManager().updateQuestsStarted(quester, true, (List<Quest>) data.left);

			Quest quest = data.left.isEmpty() ? null : data.left.get(0);
			if (matcher.group("desc") != null) {
				// started_ordered_<desc ID>
				String descriptionId = matcher.group("desc");
				if (quest == null)
					return Lang.SCOREBOARD_NONE.toString();

				var descriptionProviderOpt =
						quest.getDescriptions().stream()
								.filter(desc -> desc.getDescriptionId().equals(descriptionId)).findAny();
				if (descriptionProviderOpt.isPresent()) {
					List<String> descriptionLines = descriptionProviderOpt.get()
							.provideDescription(new QuestDescriptionContext(
									QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
									quest, p, quester, PlayerListCategory.IN_PROGRESS,
									DescriptionSource.PLACEHOLDER));
					return descriptionLines == null ? null : String.join("\n", descriptionLines);
				} else
					return null;
			} else {
				// started_ordered or started_ordered_<line ID>
				OptionalInt splitId = matcher.group("id") == null ? OptionalInt.empty()
						: OptionalInt.of(Integer.parseInt(matcher.group("id")));
				if (quest == null)
					return splitId.isEmpty() || splitId.getAsInt() == 0 ? Lang.SCOREBOARD_NONE.toString() : "";

				String desc = quest.getDescriptionLine(quester, DescriptionSource.PLACEHOLDER);
				String format = splitId.isEmpty() ? inlineFormat : splitFormat;
				format = format.replace("{questName}", quest.getName()).replace("{questDescription}", desc);

				if (splitId.isEmpty())
					return format;

				List<String> lines = ChatColorUtils.wordWrap(format, lineLength);
				if (splitId.getAsInt() >= lines.size())
					return "";
				return lines.get(splitId.getAsInt());
			}
		} finally {
			playersLock.readLock().unlock();
		}
	}

	private void launchTask() {
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(QuestsPlugin.getPlugin(), () -> {
			playersLock.writeLock().lock();
			try {
				for (Iterator<Entry<Player, PlayerPlaceholderData>> iterator = players.entrySet().iterator(); iterator.hasNext();) {
					Entry<Player, PlayerPlaceholderData> entry = iterator.next();
					if (!entry.getKey().isOnline()) {
						iterator.remove();
						continue;
					}
					PlayerPlaceholderData data = entry.getValue();
					if (!data.left.isEmpty()) data.left.remove(0);
				}
			}finally {
				playersLock.writeLock().unlock();
			}
		}, 0, changeTime * 20);
	}

	@EventHandler
	public void onExpansionRegister(ExpansionRegisterEvent e) {
		for (Iterator<Entry<String, Consumer<PlaceholderExpansion>>> iterator = waitingExpansions.iterator(); iterator
				.hasNext();) {
			Entry<String, Consumer<PlaceholderExpansion>> entry = iterator.next();
			if (entry.getKey().equalsIgnoreCase(e.getExpansion().getIdentifier())) {
				entry.getValue().accept(e.getExpansion());
				iterator.remove();
			}
		}
	}

	private class PlayerPlaceholderData {
		private List<? extends Quest> left = Collections.emptyList();
	}

}
