package fr.skytasul.quests.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.StageHandler;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.utils.QuestUtils;

public class StageControllerImplementation<T extends AbstractStage> implements StageController, Listener {

	private final @NotNull QuestBranchImplementation branch;
	private final @NotNull StageType<T> type;

	private @Nullable T stage;

	public StageControllerImplementation(@NotNull QuestBranchImplementation branch, @NotNull StageType<T> type) {
		this.branch = Objects.requireNonNull(branch);
		this.type = Objects.requireNonNull(type);
	}

	public void setStage(@NotNull T stage) {
		if (this.stage != null)
			throw new IllegalStateException("Stage was already set");

		type.getStageClass().cast(stage); // to throw ClassCastException if needed

		this.stage = Objects.requireNonNull(stage);
	}

	@Override
	public @NotNull QuestBranchImplementation getBranch() {
		return branch;
	}

	@Override
	public @NotNull AbstractStage getStage() {
		if (stage == null)
			throw new IllegalStateException("Stage has not been loaded yet");
		return stage;
	}

	@Override
	public @NotNull StageType<T> getStageType() {
		if (type == null)
			throw new IllegalStateException("Stage has not been loaded yet");
		return type;
	}

	@Override
	public void finishStage(@NotNull Player player) {
		QuestUtils.runSync(() -> branch.finishStage(player, this));
	}

	@Override
	public boolean hasStarted(@NotNull PlayerAccount acc) {
		return branch.hasStageLaunched(acc, this);
	}

	@Override
	public void updateObjective(@NotNull Player player, @NotNull String dataKey, @Nullable Object dataValue) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		Map<String, Object> datas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStorageId());
		Validate.notNull(datas, "Account " + acc.debugName() + " does not have datas for " + toString());
		datas.put(dataKey, dataValue);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), datas);

		propagateStageHandlers(handler -> handler.stageUpdated(player, this));
		branch.getManager().questUpdated(player);
	}

	@Override
	public <D> @Nullable D getData(@NotNull PlayerAccount acc, @NotNull String dataKey) {
		Map<String, Object> stageDatas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStorageId());
		return stageDatas == null ? null : (D) stageDatas.get(dataKey);
	}

	@Override
	public @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source) {
		try {
			StageDescriptionPlaceholdersContext context = StageDescriptionPlaceholdersContext.of(true, acc, source);
			String description =
					stage.getCustomText() == null ? stage.getDefaultDescription(context) : ("§e" + stage.getCustomText());
			return MessageUtils.finalFormat(description, stage.getPlaceholdersRegistry(), context);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"An error occurred while getting the description line for player " + acc.getName() + " in " + toString(),
					ex);
			return "§a" + type.getName();
		}
	}

	private void propagateStageHandlers(@NotNull Consumer<@NotNull StageHandler> consumer) {
		Consumer<StageHandler> newConsumer = handler -> {
			try {
				consumer.accept(handler);
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while updating stage handler.", ex);
			}
		};
		QuestsAPI.getAPI().getQuestsHandlers().forEach(newConsumer);
		stage.getOptions().forEach(newConsumer);
	}

	public void start(@NotNull PlayerAccount acc) {
		if (acc.isCurrent())
			MessageUtils.sendMessage(acc.getPlayer(), stage.getStartMessage(), MessageType.OFF);
		Map<String, Object> datas = new HashMap<>();
		stage.initPlayerDatas(acc, datas);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), datas);
		propagateStageHandlers(handler -> handler.stageStart(acc, this));
		stage.started(acc);
	}

	public void end(@NotNull PlayerAccount acc) {
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), null);
		propagateStageHandlers(handler -> handler.stageEnd(acc, this));
		stage.ended(acc);
	}

	public void joins(@NotNull Player player) {
		propagateStageHandlers(handler -> handler.stageJoin(player, this));
		stage.joined(player);
	}

	public void leaves(@NotNull Player player) {
		propagateStageHandlers(handler -> handler.stageLeave(player, this));
		stage.left(player);
	}

	public void load() {
		QuestUtils.autoRegister(stage);
		propagateStageHandlers(handler -> handler.stageLoad(this));
		stage.load();
	}

	public void unload() {
		QuestUtils.autoUnregister(stage);
		propagateStageHandlers(handler -> handler.stageUnload(this));
		stage.unload();
	}

	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (e.isFirstJoin())
			return;

		if (hasStarted(e.getPlayerAccount()))
			joins(e.getPlayer());
	}

	@EventHandler
	public void onLeave(PlayerAccountLeaveEvent e) {
		if (hasStarted(e.getPlayerAccount()))
			leaves(e.getPlayer());
	}

	@Override
	public @NotNull String getFlowId() {
		if (branch.isEndingStage(this))
			return "E" + branch.getEndingStageId(this);
		return Integer.toString(branch.getRegularStageId(this));
	}

	public int getStorageId() {
		return branch.isEndingStage(this) ? branch.getEndingStageId(this) : branch.getRegularStageId(this);
	}

	@Override
	public String toString() {
		return "stage " + getFlowId() + " (" + type.getID() + ") of quest " + branch.getQuest().getId() + ", branch "
				+ branch.getId();
	}

	public static @NotNull StageControllerImplementation<?> loadFromConfig(@NotNull QuestBranchImplementation branch,
			@NotNull ConfigurationSection section) {
		String typeID = section.getString("stageType");

		StageType<?> stageType = QuestsAPI.getAPI().getStages().getType(typeID)
				.orElseThrow(() -> new IllegalArgumentException("Unknown stage type " + typeID));

		return loadFromConfig(branch, section, stageType);
	}

	private static <T extends AbstractStage> @NotNull StageControllerImplementation<T> loadFromConfig(
			@NotNull QuestBranchImplementation branch, @NotNull ConfigurationSection section, StageType<T> type) {
		// we need to separate into two methods to trick the generics

		StageControllerImplementation<T> controller = new StageControllerImplementation<>(branch, type);
		T stage = type.getLoader().supply(section, controller);
		controller.setStage(stage);
		stage.load(section);
		return controller;
	}

}