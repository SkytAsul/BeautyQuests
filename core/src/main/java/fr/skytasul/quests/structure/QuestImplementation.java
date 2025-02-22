package fr.skytasul.quests.structure;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.*;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.*;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class QuestImplementation implements Quest, QuestDescriptionProvider {

	private static final Pattern PERMISSION_PATTERN = Pattern.compile("^beautyquests\\.start\\.(\\d+)$");

	private final int id;
	private final File file;
	private BranchesManagerImplementation manager;

	private List<QuestOption<?>> options = new ArrayList<>();
	private List<QuestDescriptionProvider> descriptions = new ArrayList<>();

	private boolean removed = false;

	private PlaceholderRegistry placeholders;

	public QuestImplementation(int id) {
		this(id, new File(BeautyQuests.getInstance().getQuestsManager().getSaveFolder(), id + ".yml"));
	}

	public QuestImplementation(int id, @NotNull File file) {
		this.id = id;
		this.file = file;
		this.manager = new BranchesManagerImplementation(this);
		this.descriptions.add(this);
	}

	public void load() {
		QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questLoaded(this));
	}

	@Override
	public boolean isValid() {
		return !removed;
	}

	@Override
	public @NotNull List<QuestDescriptionProvider> getDescriptions() {
		return descriptions;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator<QuestOption> iterator() {
		return (Iterator) options.iterator();
	}

	@Override
	public @NotNull <T extends QuestOption<?>> T getOption(@NotNull Class<T> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return (T) option;
		}
		throw new NullPointerException("Quest " + id + " do not have option " + clazz.getName());
	}

	@Override
	public boolean hasOption(@NotNull Class<? extends QuestOption<?>> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return true;
		}
		return false;
	}

	@Override
	public void addOption(@NotNull QuestOption<?> option) {
		if (!option.hasCustomValue()) return;
		options.add(option);
		option.attach(this);
		option.setValueUpdaterListener(() -> {
			if (!option.hasCustomValue()) {
				option.detach();
				options.remove(option);
			}
		});
	}

	@Override
	public void removeOption(@NotNull Class<? extends QuestOption<?>> clazz) {
		for (Iterator<QuestOption<?>> iterator = options.iterator(); iterator.hasNext();) {
			QuestOption<?> option = iterator.next();
			if (clazz.isInstance(option)) {
				option.detach();
				iterator.remove();
				break;
			}
		}
	}

	public boolean isRemoved(){
		return removed;
	}

	@Override
	public int getId() {
		return id;
	}

	public File getFile(){
		return file;
	}

	public String getNameAndId() {
		return getName() + " (#" + id + ")";
	}

	@Override
	public @Nullable String getName() {
		return getOptionValueOrDef(OptionName.class);
	}

	@Override
	public @Nullable String getDescription() {
		return getOptionValueOrDef(OptionDescription.class);
	}

	@Override
	public @NotNull ItemStack getQuestItem() {
		return getOptionValueOrDef(OptionQuestItem.class);
	}

	@Override
	public boolean isScoreboardEnabled() {
		return getOptionValueOrDef(OptionScoreboardEnabled.class);
	}

	@Override
	public boolean isCancellable() {
		return getOptionValueOrDef(OptionCancellable.class);
	}

	@Override
	public boolean isRepeatable() {
		return getOptionValueOrDef(OptionRepeatable.class);
	}

	@Override
	public boolean isHidden(QuestVisibilityLocation location) {
		return !getOptionValueOrDef(OptionVisibility.class).contains(location);
	}

	@Override
	public boolean isHiddenWhenRequirementsNotMet() {
		return getOptionValueOrDef(OptionHideNoRequirements.class);
	}

	@Override
	public boolean canBypassLimit() {
		return getOptionValueOrDef(OptionBypassLimit.class);
	}

	@Override
	public @Nullable BqNpc getStarterNpc() {
		return getOptionValueOrDef(OptionStarterNPC.class);
	}

	@Override
	public @NotNull BranchesManagerImplementation getBranchesManager() {
		return manager;
	}

	public @NotNull String getTimeLeft(@NotNull Quester quester) {
		var timer = quester.getDataHolder().getQuestData(this).getTimer();
		if (timer.isEmpty())
			return "x";
		return Utils.millisToHumanString(timer.getAsLong() - System.currentTimeMillis());
	}

	@Override
	public boolean hasStarted(@NotNull Quester quester) {
		if (!quester.getDataHolder().hasQuestData(this))
			return false;
		if (quester.getDataHolder().getQuestData(this).hasStarted())
			return true;
		if (getOptionValueOrDef(OptionStartRewards.class).isInAsyncReward(quester))
			return true;
		return false;
	}

	@Override
	public boolean hasFinished(@NotNull Quester quester) {
		return quester.getDataHolder().getQuestDataIfPresent(this).map(x -> x.hasFinishedOnce()).orElse(false);
	}

	@Override
	public boolean cancelPlayer(@NotNull Quester quester) {
		if (!quester.getDataHolder().getQuestDataIfPresent(this).map(x -> x.hasStarted()).orElse(false))
			return false;

		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Cancelling quest {} for {}", id, quester.getDetailedName());
		cancelInternal(quester);
		return true;
	}

	private void cancelInternal(@NotNull Quester quester) {
		manager.remove(quester);
		QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questReset(quester, this));
		Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(quester, this));

		getOptionValueOrDef(OptionCancelRewards.class).giveRewards(quester)
				.whenComplete((__, ex) -> QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Failed to execute cancel rewards for quester {} in quest {}", ex, quester.getDetailedName(),
						getId()));
	}

	@Override
	public @NotNull CompletableFuture<Boolean> resetPlayer(@NotNull Quester quester) {
		boolean hadDatas = false;
		CompletableFuture<?> future = null;

		if (quester.getDataHolder().hasQuestData(this)) {
			hadDatas = true;

			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Resetting quest {} for player {}", id,
					quester.getDetailedName());
			cancelInternal(quester);
			future = quester.getDataHolder().removeQuestData(this);
		}

		if (hasOption(OptionStartDialog.class)) {
			var dialogRunner = getOption(OptionStartDialog.class).getDialogRunner();
			for (Player p : quester.getOnlinePlayers())
				if (dialogRunner.removePlayer(p))
					hadDatas = true;
		}

		return future == null ? CompletableFuture.completedFuture(hadDatas) : future.thenApply(__ -> true);
	}

	@Override
	public boolean canStart(@NotNull Player p, boolean sendMessage) {
		Quester acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			if (sendMessage) Lang.ALREADY_STARTED.send(p);
			return false;
		}
		if (!getOptionValueOrDef(OptionRepeatable.class) && hasFinished(acc)) return false;
		if (!testTimer(acc, sendMessage)) return false;
		if (!testRequirements(p, acc, sendMessage)) return false;
		return true;
	}

	public boolean testRequirements(@NotNull Player p, @NotNull Quester acc, boolean sendMessage) {
		if (!p.hasPermission("beautyquests.start")) return false;
		if (!testQuestLimit(p, acc, sendMessage)) return false;
		sendMessage = sendMessage && (!hasOption(OptionStarterNPC.class)
				|| (QuestsConfiguration.getConfig().getQuestsConfig().requirementReasonOnMultipleQuests()
						|| getOption(OptionStarterNPC.class).getValue().getQuests().size() == 1));
		return getOptionValueOrDef(OptionRequirements.class).allMatch(p, sendMessage);
	}

	public boolean testQuestLimit(@NotNull Player p, @NotNull Quester acc, boolean sendMessage) {
		if (Boolean.TRUE.equals(getOptionValueOrDef(OptionBypassLimit.class)))
			return true;
		int playerMaxLaunchedQuest;
		OptionalInt playerMaxLaunchedQuestOpt = p.getEffectivePermissions().stream()
				.filter(permission -> permission.getValue()) // all "active" permissions
				.map(permission -> PERMISSION_PATTERN.matcher(permission.getPermission()))
				.filter(matcher -> matcher.matches()) // all permissions that matches "beautyquests.start.<number>"
				.mapToInt(matcher -> Integer.parseInt(matcher.group(1))) // get the effective number
				.max();
		if (playerMaxLaunchedQuestOpt.isPresent()) {
			playerMaxLaunchedQuest = playerMaxLaunchedQuestOpt.getAsInt();
		}else {
			if (QuestsConfiguration.getConfig().getQuestsConfig().maxLaunchedQuests() == 0)
				return true;
			playerMaxLaunchedQuest = QuestsConfiguration.getConfig().getQuestsConfig().maxLaunchedQuests();
		}
		if (QuestsAPI.getAPI().getQuestsManager().getStartedSize(acc) >= playerMaxLaunchedQuest) {
			if (sendMessage)
				Lang.QUESTS_MAX_LAUNCHED.quickSend(p, "quests_max_amount", playerMaxLaunchedQuest);
			return false;
		}
		return true;
	}

	public boolean testTimer(@NotNull Quester acc, boolean sendMessage) {
		if (isRepeatable() && acc.getDataHolder().hasQuestData(this)) {
			var data = acc.getDataHolder().getQuestData(this);
			if (data.getTimer().orElse(0) > System.currentTimeMillis()) {
				if (sendMessage)
					Lang.QUEST_WAIT.quickSend(acc, "time_left", getTimeLeft(acc));
				return false;
			} else if (data.getTimer().isPresent())
				data.setTimer(OptionalLong.empty());
		}
		return true;
	}

	public boolean isInDialog(@NotNull Player p) {
		return hasOption(OptionStartDialog.class) && getOption(OptionStartDialog.class).getDialogRunner().isPlayerInDialog(p);
	}

	@Override
	public void doNpcClick(@NotNull Player p) {
		if (hasOption(OptionStartDialog.class)) {
			getOption(OptionStartDialog.class).getDialogRunner().onClick(p);
		} else
			attemptStart(p);
	}

	public void leave(@NotNull Player p) {
		if (hasOption(OptionStartDialog.class)) {
			getOption(OptionStartDialog.class).getDialogRunner().removePlayer(p);
		}
	}

	@Override
	public @NotNull String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source) {
		if (!quester.getDataHolder().hasQuestData(this))
			throw new IllegalArgumentException("Account does not have quest datas for quest " + id);
		if (getOptionValueOrDef(OptionStartRewards.class).isInAsyncReward(quester))
			return "§7x";
		QuesterQuestData datas = quester.getDataHolder().getQuestData(this);
		if (datas.getState() == QuesterQuestData.State.IN_END)
			return Lang.SCOREBOARD_ASYNC_END.toString();
		QuestBranchImplementation branch = manager.getBranch(datas.getBranch().orElseThrow());
		if (branch == null) throw new IllegalStateException("Account is in branch " + datas.getBranch() + " in quest " + id + ", which does not actually exist");
		return branch.getDescriptionLine(quester, source);
	}

	@Override
	public @NotNull List<String> provideDescription(QuestDescriptionContext context) {
		if (context.getCategory() != PlayerListCategory.IN_PROGRESS)
			return Collections.emptyList();

		return Arrays.asList(getDescriptionLine(context.getQuester(), context.getSource()));
	}

	@Override
	public @NotNull String getDescriptionId() {
		return "advancement";
	}

	@Override
	public double getDescriptionPriority() {
		return 15;
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry()
					.registerIndexed("quest", this::getNameAndId)
					.register("quest_name", this::getName)
					.register("quest_id", id)
					.register("quest_description", this::getDescription);
		}
		return placeholders;
	}

	@Override
	public @NotNull CompletableFuture<Boolean> attemptStart(@NotNull Player p) {
		if (!canStart(p, true))
			return CompletableFuture.completedFuture(false);

		String confirm;
		if (QuestsConfiguration.getConfig().getQuestsConfig().questConfirmGUI()
				&& !"none".equals(confirm = getOptionValueOrDef(OptionConfirmMessage.class))) {
			CompletableFuture<Boolean> future = new CompletableFuture<>();
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createConfirmation(() -> {
				start(p);
				future.complete(true);
			}, () -> {
				future.complete(false);
			}, Lang.INDICATION_START.format(getPlaceholdersRegistry()), confirm).open(p);
			return future;
		}else {
			start(p);
			return CompletableFuture.completedFuture(true);
		}
	}

	@Override
	public void start(@NotNull Player p) {
		start(p, false);
	}

	@Override
	public void start(@NotNull Player p, boolean silently) {
		Quester quester = PlayersManager.getPlayerAccount(p);
		if (hasStarted(quester)) {
			if (!silently) Lang.ALREADY_STARTED.send(p);
			return;
		}

		QuestPreLaunchEvent event = new QuestPreLaunchEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		AdminMode.broadcast(p.getName() + " started the quest " + id);
		quester.getDataHolder().getQuestData(this).setTimer(OptionalLong.empty());

		if (!silently) {
			String startMsg = getOptionValueOrDef(OptionStartMessage.class);
			if (!"none".equals(startMsg))
				MessageUtils.sendRawMessage(p, startMsg, getPlaceholdersRegistry(), PlaceholdersContext.of(p, true, null));
		}

		getOptionValueOrDef(OptionStartRewards.class).giveRewards(quester).whenComplete((result, ex) -> {
			if (ex != null) {
				DefaultErrors.sendGeneric(quester, "giving reward");
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while giving quest {} start rewards to {}.", ex, getId(),
								quester.getDetailedName());
			}

			if (result.branchInterruption())
				QuestsPlugin.getPlugin().getLoggerExpanded().debug(
						"Useless branching interruption in the quest {} ending rewards", getId());

			if (!silently)
				result.earnings().forEach((player, earnings) -> Lang.FINISHED_OBTAIN.quickSend(player, "rewards",
						MessageUtils.itemsToFormattedString(earnings.toArray(String[]::new))));

			getOptionValueOrDef(OptionRequirements.class).stream().filter(Actionnable.class::isInstance)
					.map(Actionnable.class::cast).forEach(x -> x.trigger(p));

			QuestUtils.runOrSync(() -> {
				manager.startPlayer(quester);
				QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questStart(quester, this));
				Bukkit.getPluginManager().callEvent(new QuestLaunchEvent(p, QuestImplementation.this));
			});
		});
	}

	@Override
	public void finish(@NotNull Quester quester) {
		AdminMode.broadcast(quester.getFriendlyName() + " is completing the quest " + id);
		QuesterQuestData questDatas = quester.getDataHolder().getQuestData(this);

		questDatas.setState(QuesterQuestData.State.IN_END);
		getOptionValueOrDef(OptionEndRewards.class).giveRewards(quester).whenComplete((result, ex) -> {
			if (ex != null) {
				DefaultErrors.sendGeneric(quester, "giving reward");
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while giving quest {} end rewards to {}.", ex, getId(),
								quester.getDetailedName());
			}

			if (result.branchInterruption())
				QuestsPlugin.getPlugin().getLoggerExpanded().debug(
						"Useless branching interruption in the quest {} ending rewards", getId());

			for (var player : quester.getOnlinePlayers()) {
				String endMsg;
				MessageType msgType;
				if (hasOption(OptionEndMessage.class)) {
					endMsg = getOption(OptionEndMessage.class).getValue();
					msgType = MessageType.DefaultMessageType.UNPREFIXED;
				} else {
					// default message
					endMsg = Lang.FINISHED_BASE.getValue();
					msgType = MessageType.DefaultMessageType.PREFIXED;
					if (!result.getPlayerEarnings(player).isEmpty())
						endMsg += " " + Lang.FINISHED_OBTAIN.getValue();
				}
				MessageUtils.sendMessage(player, endMsg, msgType,
						PlaceholderRegistry.of("rewards",
								MessageUtils.itemsToFormattedString(result.getPlayerEarnings(player).toArray(String[]::new)))
						.compose(false, this));
			}

			// Fireworks have to be spawned synchronously
			QuestUtils.runOrSync(() -> {
				manager.remove(quester);
				questDatas.setState(QuesterQuestData.State.NOT_STARTED);
				questDatas.setBranch(OptionalInt.empty());
				questDatas.incrementFinished();
				questDatas.setStartingTime(OptionalLong.empty());
				if (hasOption(OptionQuestPool.class))
					((QuestPoolImplementation) getOptionValueOrDef(OptionQuestPool.class)).questCompleted(quester, this);
				if (isRepeatable()) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, Math.max(0, getOptionValueOrDef(OptionTimer.class)));
					questDatas.setTimer(OptionalLong.of(cal.getTimeInMillis()));
				}
				quester.getOnlinePlayers()
						.forEach(p -> QuestUtils.spawnFirework(p.getLocation(), getOptionValueOrDef(OptionFirework.class)));
				QuestUtils.playPluginSound(quester, getOptionValueOrDef(OptionEndSound.class), 1);

				QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questFinish(quester, this));
				Bukkit.getPluginManager().callEvent(new QuestFinishEvent(quester, this));
			});
		});
	}

	@Override
	public void delete(boolean silently, boolean keepDatas) {
		BeautyQuests.getInstance().getQuestsManager().removeQuest(this);
		unload();
		if (hasOption(OptionStarterNPC.class))
			((BqNpcImplementation) getOptionValueOrDef(OptionStarterNPC.class)).removeQuest(this);

		if (!keepDatas) {
			BeautyQuests.getInstance().getQuesterManager().getDataManager().resetQuestData(id).whenComplete(
					QuestsPlugin.getPlugin().getLoggerExpanded().logError("An error occurred while removing player datas after quest removal"));
			if (file.exists()) file.delete();
		}

		removed = true;
		Bukkit.getPluginManager().callEvent(new QuestRemoveEvent(this));
		if (!keepDatas)
			QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questRemove(this));
		if (!silently)
			QuestsPlugin.getPlugin().getLoggerExpanded().info("The quest \"" + getName() + "\" has been removed");
	}

	public void unload(){
		QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questUnload(this));
		manager.remove();
		options.forEach(QuestOption::detach);
	}

	@Override
	public String toString(){
		return "Quest{id=" + id + ", npcID=" + ", branches=" + manager.toString() + ", name=" + getName() + "}";
	}

	public boolean saveToFile() throws IOException {
		YamlConfiguration fc = new YamlConfiguration();

		BeautyQuests.getInstance().resetSavingFailure();
		save(fc);
		if (BeautyQuests.getInstance().hasSavingFailed()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error occurred while saving quest " + id);
			return false;
		}

		Path path = file.toPath();
		if (!Files.exists(path))
			Files.createFile(path);

		String questData = fc.saveToString();
		String oldQuestDatas = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		if (questData.equals(oldQuestDatas)) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Quest " + id + " was up-to-date.");
			return false;
		}else {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Saving quest " + id + " into " + path.toString());
			Files.write(path, questData.getBytes(StandardCharsets.UTF_8));
			return true;
		}
	}

	private void save(@NotNull ConfigurationSection section) {
		for (QuestOption<?> option : options) {
			try {
				if (option.hasCustomValue()) section.set(option.getOptionCreator().id, option.save());
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("An exception occured when saving an option for quest " + id, ex);
			}
		}

		manager.save(section.createSection("manager"));
		section.set("id", id);
	}


	public static @Nullable QuestImplementation loadFromFile(@NotNull File file) {
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			return deserialize(file, config);
		}catch (Exception e) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Error when loading quests from data file.", e);
			return null;
		}
	}

	private static @Nullable QuestImplementation deserialize(@NotNull File file, @NotNull ConfigurationSection map) {
		if (!map.contains("id")) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Quest doesn't have an id.");
			return null;
		}

		QuestImplementation qu = new QuestImplementation(map.getInt("id"), file);

		qu.manager = BranchesManagerImplementation.deserialize(map.getConfigurationSection("manager"), qu);
		if (qu.manager == null) return null;

		for (String key : map.getKeys(false)) {
			for (QuestOptionCreator<?, ?> creator : QuestOptionCreator.creators.values()) {
				if (creator.applies(key)) {
					try {
						QuestOption<?> option = creator.optionSupplier.get();
						option.load(map, key);
						qu.addOption(option);
					}catch (Exception ex) {
						QuestsPlugin.getPlugin().getLoggerExpanded().warning("An exception occured when loading the option " + key + " for quest " + qu.id, ex);
						QuestsPlugin.getPlugin().notifyLoadingFailure();
					}
					break;
				}
			}
		}

		return qu;
	}

}
