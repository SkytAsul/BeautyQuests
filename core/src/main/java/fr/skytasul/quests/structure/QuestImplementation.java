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
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.*;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.rewards.MessageReward;
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
	public List<Player> inAsyncStart = new ArrayList<>();

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

	public boolean hasAsyncStart() {
		return getOptionValueOrDef(OptionStartRewards.class).hasAsync();
	}

	public boolean hasAsyncEnd() {
		return getOptionValueOrDef(OptionEndRewards.class).hasAsync();
	}

	@Override
	public @NotNull BranchesManagerImplementation getBranchesManager() {
		return manager;
	}

	public @NotNull String getTimeLeft(@NotNull PlayerAccount acc) {
		return Utils.millisToHumanString(acc.getQuestDatas(this).getTimer() - System.currentTimeMillis());
	}

	@Override
	public boolean hasStarted(@NotNull PlayerAccount acc) {
		if (!acc.hasQuestDatas(this)) return false;
		if (acc.getQuestDatas(this).hasStarted()) return true;
		if (acc.isCurrent() && hasAsyncStart() && inAsyncStart.contains(acc.getPlayer()))
			return true;
		return false;
	}

	@Override
	public boolean hasFinished(@NotNull PlayerAccount acc) {
		return acc.hasQuestDatas(this) && acc.getQuestDatas(this).isFinished();
	}

	@Override
	public boolean cancelPlayer(@NotNull PlayerAccount acc) {
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(this);
		if (datas == null || !datas.hasStarted())
			return false;

		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Cancelling quest " + id + " for player " + acc.getNameAndID());
		cancelInternal(acc);
		return true;
	}

	private void cancelInternal(@NotNull PlayerAccount acc) {
		manager.remove(acc);
		QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questReset(acc, this));
		Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(acc, this));

		if (acc.isCurrent()) {
			try {
				getOptionValueOrDef(OptionCancelRewards.class).giveRewards(acc.getPlayer());
			} catch (InterruptingBranchException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Trying to interrupt branching in a cancel reward (useless). " + toString());
			}
		}
	}

	@Override
	public @NotNull CompletableFuture<Boolean> resetPlayer(@NotNull PlayerAccount acc) {
		boolean hadDatas = false;
		CompletableFuture<?> future = null;

		if (acc.hasQuestDatas(this)) {
			hadDatas = true;

			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Resetting quest " + id + " for player " + acc.getNameAndID());
			cancelInternal(acc);
			future = acc.removeQuestDatas(this);
		}

		if (acc.isCurrent() && hasOption(OptionStartDialog.class)
				&& getOption(OptionStartDialog.class).getDialogRunner().removePlayer(acc.getPlayer()))
			hadDatas = true;

		return future == null ? CompletableFuture.completedFuture(hadDatas) : future.thenApply(__ -> true);
	}

	@Override
	public boolean canStart(@NotNull Player p, boolean sendMessage) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			if (sendMessage) Lang.ALREADY_STARTED.send(p);
			return false;
		}
		if (!getOptionValueOrDef(OptionRepeatable.class) && hasFinished(acc)) return false;
		if (!testTimer(acc, sendMessage)) return false;
		if (!testRequirements(p, acc, sendMessage)) return false;
		return true;
	}

	public boolean testRequirements(@NotNull Player p, @NotNull PlayerAccount acc, boolean sendMessage) {
		if (!p.hasPermission("beautyquests.start")) return false;
		if (!testQuestLimit(p, acc, sendMessage)) return false;
		sendMessage = sendMessage && (!hasOption(OptionStarterNPC.class)
				|| (QuestsConfiguration.getConfig().getQuestsConfig().requirementReasonOnMultipleQuests()
						|| getOption(OptionStarterNPC.class).getValue().getQuests().size() == 1));
		return getOptionValueOrDef(OptionRequirements.class).allMatch(p, sendMessage);
	}

	public boolean testQuestLimit(@NotNull Player p, @NotNull PlayerAccount acc, boolean sendMessage) {
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

	public boolean testTimer(@NotNull PlayerAccount acc, boolean sendMessage) {
		if (isRepeatable() && acc.hasQuestDatas(this)) {
			long time = acc.getQuestDatas(this).getTimer();
			if (time > System.currentTimeMillis()) {
				if (sendMessage && acc.isCurrent())
					Lang.QUEST_WAIT.quickSend(acc.getPlayer(), "time_left", getTimeLeft(acc));
				return false;
			}else if (time != 0) acc.getQuestDatas(this).setTimer(0);
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
	public @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source) {
		if (!acc.hasQuestDatas(this)) throw new IllegalArgumentException("Account does not have quest datas for quest " + id);
		if (acc.isCurrent() && hasAsyncStart() && inAsyncStart.contains(acc.getPlayer()))
			return "§7x";
		PlayerQuestDatas datas = acc.getQuestDatas(this);
		if (datas.isInQuestEnd()) return Lang.SCOREBOARD_ASYNC_END.toString();
		QuestBranchImplementation branch = manager.getBranch(datas.getBranch());
		if (branch == null) throw new IllegalStateException("Account is in branch " + datas.getBranch() + " in quest " + id + ", which does not actually exist");
		return branch.getDescriptionLine(acc, source);
	}

	@Override
	public @NotNull List<String> provideDescription(QuestDescriptionContext context) {
		if (!context.getPlayerAccount().isCurrent())
			return Collections.emptyList();
		if (context.getCategory() != PlayerListCategory.IN_PROGRESS)
			return Collections.emptyList();

		return Arrays.asList(getDescriptionLine(context.getPlayerAccount(), context.getSource()));
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
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			if (!silently) Lang.ALREADY_STARTED.send(p);
			return;
		}
		QuestPreLaunchEvent event = new QuestPreLaunchEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		AdminMode.broadcast(p.getName() + " started the quest " + id);
		acc.getQuestDatas(this).setTimer(0);
		if (!silently) {
			String startMsg = getOptionValueOrDef(OptionStartMessage.class);
			if (!"none".equals(startMsg))
				MessageUtils.sendRawMessage(p, startMsg, getPlaceholdersRegistry(), PlaceholdersContext.of(p, true, null));
		}

		Runnable run = () -> {
			List<String> msg = Collections.emptyList();
			try {
				msg = getOptionValueOrDef(OptionStartRewards.class).giveRewards(p);
			} catch (InterruptingBranchException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Trying to interrupt branching in a starting reward (useless). " + toString());
			}
			getOptionValueOrDef(OptionRequirements.class).stream().filter(Actionnable.class::isInstance).map(Actionnable.class::cast).forEach(x -> x.trigger(p));
			if (!silently && !msg.isEmpty())
				Lang.FINISHED_OBTAIN.quickSend(p, "rewards",
						MessageUtils.itemsToFormattedString(msg.toArray(new String[0])));
			inAsyncStart.remove(p);

			QuestUtils.runOrSync(() -> {
				manager.startPlayer(acc);
				QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questStart(acc, this));
				Bukkit.getPluginManager().callEvent(new QuestLaunchEvent(p, QuestImplementation.this));
			});
		};
		if (hasAsyncStart()) {
			inAsyncStart.add(p);
			QuestUtils.runAsync(run);
		}else run.run();
	}

	@Override
	public void finish(@NotNull Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		AdminMode.broadcast(p.getName() + " is completing the quest " + id);
		PlayerQuestDatas questDatas = acc.getQuestDatas(this);

		Runnable run = () -> {
			try {
				List<String> msg = getOptionValueOrDef(OptionEndRewards.class).giveRewards(p);
				String obtained = MessageUtils.itemsToFormattedString(msg.toArray(new String[0]));
				if (hasOption(OptionEndMessage.class)) {
					String endMsg = getOption(OptionEndMessage.class).getValue();
					if (!"none".equals(endMsg))
						MessageUtils.sendRawMessage(p, endMsg, PlaceholderRegistry.of("rewards", obtained).with(this),
								PlaceholdersContext.of(p, true, null));
				} else
					MessageUtils.sendMessage(p, Lang.FINISHED_BASE.format(this)
							+ (msg.isEmpty() ? "" : " " + Lang.FINISHED_OBTAIN.quickFormat("rewards", obtained)),
							MessageType.DefaultMessageType.PREFIXED);
			}catch (Exception ex) {
				DefaultErrors.sendGeneric(p, "reward message");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while giving quest end rewards.", ex);
			}

			QuestUtils.runOrSync(() -> {
				manager.remove(acc);
				questDatas.setBranch(-1);
				questDatas.incrementFinished();
				questDatas.setStartingTime(0);
				if (hasOption(OptionQuestPool.class))
					((QuestPoolImplementation) getOptionValueOrDef(OptionQuestPool.class)).questCompleted(acc, this);
				if (isRepeatable()) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, Math.max(0, getOptionValueOrDef(OptionTimer.class)));
					questDatas.setTimer(cal.getTimeInMillis());
				}
				QuestUtils.spawnFirework(p.getLocation(), getOptionValueOrDef(OptionFirework.class));
				QuestUtils.playPluginSound(p, getOptionValueOrDef(OptionEndSound.class), 1);

				QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questFinish(acc, this));
				Bukkit.getPluginManager().callEvent(new QuestFinishEvent(p, this));
			});
		};

		if (hasAsyncEnd()) {
			questDatas.setInQuestEnd();
			new Thread(() -> {
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Using " + Thread.currentThread().getName() + " as the thread for async rewards.");
				run.run();
			}, "BQ async end " + p.getName()).start();
		}else run.run();
	}

	@Override
	public void delete(boolean silently, boolean keepDatas) {
		BeautyQuests.getInstance().getQuestsManager().removeQuest(this);
		unload();
		if (hasOption(OptionStarterNPC.class))
			((BqNpcImplementation) getOptionValueOrDef(OptionStarterNPC.class)).removeQuest(this);

		if (!keepDatas) {
			BeautyQuests.getInstance().getPlayersManager().removeQuestDatas(this).whenComplete(
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
		String endMessage = map.getString("endMessage");
		if (endMessage != null) {
			OptionEndRewards rewards;
			if (qu.hasOption(OptionEndRewards.class)) {
				rewards = qu.getOption(OptionEndRewards.class);
			}else {
				rewards = (OptionEndRewards) QuestOptionCreator.creators.get(OptionEndRewards.class).optionSupplier.get();
				rewards.getValue().add(new MessageReward(null, endMessage));
				qu.addOption(rewards);
			}
		}

		return qu;
	}

}
