package fr.skytasul.quests.structure;

import com.google.gson.JsonSyntaxException;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.stages.*;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.compatibility.BQBackwardCompat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;

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
	public void finishStage(@NotNull Quester quester) {
		QuestUtils.runSync(() -> branch.finishPlayerStage(quester, this));
	}

	@Override
	public boolean hasStarted(@NotNull Quester acc) {
		return branch.hasStageLaunched(acc, this);
	}

	@Override
	public @NotNull Collection<Quester> getApplicableQuesters(@NotNull Player player) {
		return List.of(PlayersManager.getPlayerAccount(player));
		// TODO add more possibilities!
	}

	@Override
	public void updateObjective(@NotNull Quester quester, @NotNull String dataKey, @Nullable Object dataValue) {
		QuesterQuestData questData = quester.getDataHolder().getQuestData(branch.getQuest());
		Map<String, Object> datas = questData.getStageDatas(getStorageId());
		if (datas == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Account {} did not have data for {}. Creating some.",
					quester.getDetailedName(), toString());
			datas = new HashMap<>();
			stage.initPlayerDatas(quester, datas);
		}

		datas.put(dataKey, dataValue);
		questData.setStageDatas(getStorageId(), datas);

		propagateStageHandlers(handler -> handler.stageUpdated(quester, this));
		branch.getManager().questUpdated(quester);
	}

	@Override
	public <D> @Nullable D getData(@NotNull Quester acc, @NotNull String dataKey, @Nullable Class<D> dataType) {
		QuesterQuestData playerDatas = acc.getDataHolder().getQuestData(branch.getQuest());
		Map<String, Object> datas = playerDatas.getStageDatas(getStorageId());

		if (datas == null) {
			if (!hasStarted(acc))
				throw new IllegalStateException("Trying to fetch data of not launched stage");

			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Q uester {} did not have data for {}. Creating some.",
					acc.getDetailedName(), this);
			datas = new HashMap<>();
			stage.initPlayerDatas(acc, datas);
			acc.getDataHolder().getQuestData(branch.getQuest()).setStageDatas(getStorageId(), datas);
		}

		Object data = datas.get(dataKey);
		if (dataType == null) // case when we do not have explicit data type to match for: we can only do direct cast
			return (D) data;

		if (dataType.isInstance(data)) // easy: the data is directly compatible with the expected type
			return dataType.cast(data);

		// hard: the data is not compatible. It may be because the deserialization process previously did
		// not know the exact type to deserialize. Hence we go back to serialized to deserialize again, but
		// this time with the correct type.
		String serialized = CustomizedObjectTypeAdapter.serializeNullable(data);
		try {
			return CustomizedObjectTypeAdapter.deserializeNullable(serialized, dataType);
		} catch (JsonSyntaxException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"Cannot convert data " + dataKey + " to " + dataType.toString() + ". Serialized form: " + serialized);
			throw ex;
		}
	}

	@Override
	public @Nullable String getDescriptionLine(@NotNull Quester acc, @NotNull DescriptionSource source) {
		try {
			String description = stage.getCustomText();
			if (description != null) {
				if (description.equals("none"))
					return null;
				description = "§e" + description;
			}

			StageDescriptionPlaceholdersContext context = StageDescriptionPlaceholdersContext.of(true, acc, source, null);
			if (description == null)
				description = stage.getDefaultDescription(context);

			return MessageUtils.finalFormat(description, stage.getPlaceholdersRegistry(), context);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"An error occurred while getting the description line for {} in {}", ex, acc.getDetailedName(), this);
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

	public void start(@NotNull Quester acc) {
		MessageUtils.sendMessage(acc, stage.getStartMessage(), MessageType.DefaultMessageType.OFF);
		Map<String, Object> datas = new HashMap<>();
		stage.initPlayerDatas(acc, datas);
		acc.getDataHolder().getQuestData(branch.getQuest()).setStageDatas(getStorageId(), datas);
		propagateStageHandlers(handler -> handler.stageStart(acc, this));
		stage.started(acc);
	}

	public void end(@NotNull Quester acc) {
		acc.getDataHolder().getQuestData(branch.getQuest()).setStageDatas(getStorageId(), null);
		propagateStageHandlers(handler -> handler.stageEnd(acc, this));
		stage.ended(acc);
	}

	public void load() {
		QuestUtils.autoRegister(stage);
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		propagateStageHandlers(handler -> handler.stageLoad(this));
		stage.load();
	}

	public void unload() {
		QuestUtils.autoUnregister(stage);
		HandlerList.unregisterAll(this);
		propagateStageHandlers(handler -> handler.stageUnload(this));
		stage.unload();
	}

	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (e.isFirstJoin())
			return;

		if (hasStarted(e.getQuester())) {
			propagateStageHandlers(handler -> handler.stageJoin(e.getPlayer(), e.getQuester(), this));
			stage.joined(e.getPlayer(), e.getQuester());
		}
	}

	@EventHandler
	public void onLeave(PlayerAccountLeaveEvent e) {
		if (hasStarted(e.getQuester())) {
			propagateStageHandlers(handler -> handler.stageLeave(e.getPlayer(), e.getQuester(), this));
			stage.left(e.getPlayer(), e.getQuester());
		}
	}

	@Override
	public @NotNull String getFlowId() {
		String flow = getBranch().getId() + ":";
		if (branch.isEndingStage(this))
			flow += "E" + branch.getEndingStageId(this);
		flow += branch.getRegularStageId(this);
		return flow;
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

		Optional<StageType<?>> stageType = QuestsAPI.getAPI().getStages().getType(typeID);

		if (!stageType.isPresent())
			stageType = BQBackwardCompat.loadStageFromConfig(typeID, section);

		return loadFromConfig(branch, section, stageType.orElseThrow(() -> new IllegalArgumentException("Unknown stage type " + typeID)));
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
