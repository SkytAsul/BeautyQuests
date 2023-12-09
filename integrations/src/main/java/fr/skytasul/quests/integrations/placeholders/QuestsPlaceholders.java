package fr.skytasul.quests.integrations.placeholders;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import me.clip.placeholderapi.events.ExpansionRegisterEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class QuestsPlaceholders extends PlaceholderExpansion implements Listener {

	private static QuestsPlaceholders placeholders;

	private final int lineLength;
	private final int changeTime;
	private final String splitFormat;
	private final String inlineFormat;

	private BukkitTask task;
	private Map<Player, PlayerPlaceholderData> players = new HashMap<>();
	private ReentrantLock playersLock = new ReentrantLock();

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
		return Arrays.asList("total_amount", "quest_restartable_ID", "player_inprogress_amount", "player_finished_amount",
				"player_finished_total_amount", "started", "started_ordered", "started_ordered_X", "advancement_ID",
				"advancement_ID_raw", "player_quest_finished_ID", "started_id_list");
	}

	@Override
	public String onRequest(OfflinePlayer off, String identifier) {
		if (identifier.equals("total_amount"))
			return "" + QuestsAPI.getAPI().getQuestsManager().getQuests().size();

		if (identifier.startsWith("quest_restartable_")) {
			String sid = identifier.substring(18);
			try {
				Quest qu = QuestsAPI.getAPI().getQuestsManager().getQuest(Integer.parseInt(sid));
				if (qu == null)
					return "§c§lError: unknown quest §o" + sid;
				return Boolean.toString(qu.isRepeatable());
			} catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid;
			}
		}

		if (!off.isOnline()) return "§cerror: offline";
		Player p = off.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (acc == null) return "§cdatas not loaded";
		if (identifier.equals("player_inprogress_amount"))
			return "" + acc.getQuestsDatas().stream().filter(PlayerQuestDatas::hasStarted).count();
		if (identifier.equals("player_finished_amount"))
			return "" + acc.getQuestsDatas().stream().filter(PlayerQuestDatas::isFinished).count();
		if (identifier.equals("player_finished_total_amount"))
			return "" + acc.getQuestsDatas().stream().mapToInt(PlayerQuestDatas::getTimesFinished).sum();
		if (identifier.equals("started_id_list"))
			return acc.getQuestsDatas().stream().filter(PlayerQuestDatas::hasStarted)
					.map(x -> Integer.toString(x.getQuestID())).collect(Collectors.joining(";"));

		if (identifier.equals("started")) {
			return acc.getQuestsDatas()
					.stream()
					.filter(PlayerQuestDatas::hasStarted)
					.map(PlayerQuestDatas::getQuest)
					.filter(Objects::nonNull)
					.filter(Quest::isScoreboardEnabled)
					.map(quest -> {
						String desc = quest.getDescriptionLine(acc, DescriptionSource.PLACEHOLDER);
						return inlineFormat
								.replace("{questName}", quest.getName())
								.replace("{questDescription}", desc);
					})
					.collect(Collectors.joining("\n"));
		}

		if (identifier.startsWith("started_ordered")) {
			String after = identifier.substring(15);
			if (task == null) launchTask();

			playersLock.lock();
			try {
				PlayerPlaceholderData data = players.get(p);

				if (data == null) {
					data = new PlayerPlaceholderData(acc);
					players.put(p, data);
				}

				if (data.left.isEmpty()) {
					data.left = QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(data.acc, false, true);
				}else QuestsAPI.getAPI().getQuestsManager().updateQuestsStarted(acc, true, data.left);

				try {
					int i = -1;
					boolean noSplit = after.isEmpty();
					if (!noSplit) {
						try {
							i = Integer.parseInt(after.substring(1)) - 1;
						}catch (NumberFormatException ex) {
							i = -1;
						}
						if (i < 0) return "§cindex must be a positive integer";
					}

					if (data.left.isEmpty()) return i == -1 || i == 0 ? Lang.SCOREBOARD_NONE.toString() : "";

					Quest quest = data.left.get(0);
					String desc = quest.getDescriptionLine(acc, DescriptionSource.PLACEHOLDER);
					String format = noSplit ? inlineFormat : splitFormat;
					format = format.replace("{questName}", quest.getName()).replace("{questDescription}", desc);

					if (noSplit) return format;

					try {
						List<String> lines = ChatColorUtils.wordWrap(format, lineLength);
						if (i >= lines.size()) return "";
						return lines.get(i);
					}catch (Exception ex) {
						players.remove(p);
						return "§c" + ex.getMessage();
					}
				}catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error occurred while parsing palceholder " + identifier + " for " + p.getName(), ex);
					return "§cinvalid placeholder";
				}
			}finally {
				playersLock.unlock();
			}
		}

		if (identifier.startsWith("advancement_")) {
			int rawId = identifier.indexOf("_raw");
			String sid = rawId == -1 ? identifier.substring(12) : identifier.substring(12, rawId);
			try {
				Quest qu = QuestsAPI.getAPI().getQuestsManager().getQuest(Integer.parseInt(sid));
				if (qu == null) return "§c§lError: unknown quest §o" + sid;
				if (rawId == -1) {
					if (qu.hasStarted(acc)) {
						return qu.getDescriptionLine(acc, DescriptionSource.PLACEHOLDER);
					}
					if (qu.hasFinished(acc))
						return Lang.Finished.quickFormat("times_finished", acc.getQuestDatas(qu).getTimesFinished());
					return Lang.Not_Started.toString();
				}else {
					if (!acc.hasQuestDatas(qu)) return "-1";
					PlayerQuestDatas datas = acc.getQuestDatas(qu);
					if (datas.hasStarted()) return Integer.toString(datas.getStage());
					return "-1";
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
				if (!acc.hasQuestDatas(qu)) return "0";
				return Integer.toString(acc.getQuestDatas(qu).getTimesFinished());
			}catch (NumberFormatException ex) {
				return "§c§lError: §o" + sid;
			}
		}
		return null;
	}

	private void launchTask() {
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(QuestsPlugin.getPlugin(), () -> {
			playersLock.lock();
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
				playersLock.unlock();
			}
		}, 0, changeTime * 20);
	}

	@EventHandler
	public void onExpansionRegister(ExpansionRegisterEvent e) {
		for (Iterator<Entry<String, Consumer<PlaceholderExpansion>>> iterator = waitingExpansions.iterator(); iterator.hasNext();) {
			Entry<String, Consumer<PlaceholderExpansion>> entry = iterator.next();
			if (entry.getKey().equalsIgnoreCase(e.getExpansion().getIdentifier())) {
				entry.getValue().accept(e.getExpansion());
				iterator.remove();
			}
		}
	}

	class PlayerPlaceholderData {
		private List<Quest> left = Collections.emptyList();
		private PlayerAccount acc;

		public PlayerPlaceholderData(PlayerAccount acc) {
			this.acc = acc;
		}
	}

}
