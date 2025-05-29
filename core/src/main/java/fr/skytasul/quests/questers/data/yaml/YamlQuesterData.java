package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.questers.AbstractQuesterDataImplementation;
import fr.skytasul.quests.questers.AbstractQuesterPoolDataImplementation;
import fr.skytasul.quests.questers.AbstractQuesterQuestDataImplementation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YamlQuesterData extends AbstractQuesterDataImplementation {

	private final int id;
	private final @NotNull YamlDataManager dataManager;
	private final @NotNull Path path;

	private YamlConfiguration yaml;

	public YamlQuesterData(int id, @NotNull YamlDataManager dataManager) {
		this.id = id;
		this.dataManager = dataManager;
		this.path = dataManager.getDataPath().resolve(id + ".yml");

		if (Files.exists(path))
			load();
		else
			yaml = new YamlConfiguration();
	}

	public int getId() {
		return id;
	}

	public void load() {
		yaml = YamlConfiguration.loadConfiguration(path.toFile());

		if (yaml.isList("quests")) {
			// TODO remove, migration to 2.0
			var questsMapList = yaml.getMapList("quests");
			yaml.createSection("quests"); // to remove old list
			for (var questMap : questsMapList) {
				var questId = questMap.remove("questID");
				yaml.createSection("quests." + questId, questMap);
			}
		}
		if (yaml.contains("quests")) {
			for (String questIdRaw : yaml.getConfigurationSection("quests").getKeys(false)) {
				int questId = Integer.parseInt(questIdRaw);
				super.questData.put(questId, new QuestData(questId));
			}
		}

		if (yaml.isList("pools")) {
			// TODO remove, migration to 2.0
			var poolsMapList = yaml.getMapList("pools");
			yaml.createSection("pools"); // to remove old list
			for (var poolMap : poolsMapList) {
				var poolId = poolMap.remove("poolID");
				yaml.createSection("pools." + poolId, poolMap);
			}
		}
		if (yaml.contains("pools")) {
			for (String poolIdRaw : yaml.getConfigurationSection("pools").getKeys(false)) {
				int poolId = Integer.parseInt(poolIdRaw);
				super.poolData.put(poolId, new PoolData(poolId));
			}
		}

		for (SavableData<?> data : QuestsAPI.getAPI().getQuesterManager().getSavableData()) {
			if (yaml.contains(data.getId()))
				super.additionalData.put(data, yaml.getObject(data.getId(), data.getDataType()));
		}
	}

	@Override
	public <T> @NotNull CompletableFuture<Void> setDataInternal(@NotNull SavableData<T> data, @Nullable T value) {
		yaml.set(data.getId(), value);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	protected QuesterQuestData createQuestData(@NotNull Quest quest) {
		return new QuestData(quest.getId());
	}

	@Override
	protected QuesterPoolData createPoolData(@NotNull QuestPool pool) {
		return new PoolData(pool.getId());
	}

	protected QuesterQuestData removeQuestDataSilently(int questId) {
		QuesterQuestData data = super.questData.remove(questId);
		if (data != null)
			data.remove();
		return data;
	}

	@Override
	public void save() throws DataSavingException {
		try {
			yaml.save(path.toFile());
		} catch (IOException ex) {
			throw new DataSavingException("Failed to write to " + path.toString(), ex);
		}
	}

	@Override
	public void unload() {
		dataManager.uncache(this);
	}

	public class QuestData extends AbstractQuesterQuestDataImplementation {

		private final ConfigurationSection questConfig;

		public QuestData(int questId) {
			super(questId);

			if (yaml.isConfigurationSection("quests." + questId)) {
				questConfig = yaml.getConfigurationSection("quests." + questId);
				load();
			} else {
				questConfig = yaml.createSection("quests." + questId);
			}
		}

		private void load() {
			if (questConfig.contains("datas")) {
				// TODO delete, migration 2.0

				if (questConfig.contains("datas.starting_time")) {
					questConfig.set("startingTime", questConfig.getLong("datas.starting_time"));
					questConfig.set("datas.starting_time", null);
				}

				var stageDataPattern = Pattern.compile("stage(\\d+)");
				for (String dataKey : questConfig.getConfigurationSection("datas").getKeys(false)) {
					var stageDataMatcher = stageDataPattern.matcher(dataKey);
					String newPath = stageDataMatcher.matches()
							? "stageData." + stageDataMatcher.group(1)
							: "additionalData." + dataKey;

					Utils.copyConfigValue(questConfig, "datas." + dataKey, questConfig, newPath);
				}

				questConfig.set("datas", null);
			}

			if (questConfig.contains("timesFinished"))
				super.finished = questConfig.getInt("timesFinished");
			if (questConfig.contains("timer"))
				super.timer = OptionalLong.of(questConfig.getLong("timer"));
			if (questConfig.contains("currentBranch"))
				super.branch = OptionalInt.of(questConfig.getInt("currentBranch"));
			if (questConfig.contains("currentStage"))
				super.stage = OptionalInt.of(questConfig.getInt("currentStage"));
			if (questConfig.contains("startingTime"))
				super.startingTime = OptionalLong.of(questConfig.getLong("startingTime"));

			if (questConfig.contains("state"))
				super.state = State.valueOf(questConfig.getString("state"));
			else
				super.migrateState();

			if (questConfig.contains("questFlow") && getQuest() != null) {
				for (String flowPart : questConfig.getString("questFlow").split(";")) {
					try {
						StageController stageFlow = getQuest().getBranchesManager().getStageFromFlow(flowPart);
						super.questFlow.add(stageFlow);
					} catch (IllegalArgumentException ex) {
						QuestsPlugin.getPlugin().getLoggerExpanded().severe(
								"Cannot find a part of the quest flow for quester {}, quest {}: {}", ex, id, questID,
								flowPart);
					}
				}
			}

			if (questConfig.contains("additionalData"))
				super.additionalData =
						Utils.mapFromConfigurationSection(questConfig.getConfigurationSection("additionalData"));

			if (questConfig.contains("stageData"))
				for (String dataKey : questConfig.getConfigurationSection("stageData").getKeys(false))
					super.stageData.put(Integer.parseInt(dataKey),
							Utils.mapFromConfigurationSection(questConfig.getConfigurationSection("stageData." + dataKey)));
		}

		@Override
		public void setTimesFinished(int times) {
			super.setTimesFinished(times);
			questConfig.set("timesFinished", times);
		}

		@Override
		public <T> T setAdditionalData(String key, T value) {
			T t = super.setAdditionalData(key, value);
			questConfig.set("additionalData." + key, value);
			return t;
		}

		@Override
		public void setBranch(@NotNull OptionalInt branch) {
			super.setBranch(branch);
			questConfig.set("currentBranch", branch.isPresent() ? branch.getAsInt() : null);
		}

		@Override
		public void setStage(@NotNull OptionalInt stage) {
			super.setStage(stage);
			questConfig.set("currentStage", stage.isPresent() ? stage.getAsInt() : null);
		}

		@Override
		public void setStageDatas(int stage, Map<String, Object> datas) {
			super.setStageDatas(stage, datas);
			questConfig.set("stageData." + stage, datas);
		}

		@Override
		public void setStartingTime(@NotNull OptionalLong time) {
			super.setStartingTime(time);
			questConfig.set("startingTime", time.isPresent() ? time.getAsLong() : null);
		}

		@Override
		public void setState(@NotNull State state) {
			super.setState(state);
			questConfig.set("state", state.name());
		}

		@Override
		public void setTimer(@NotNull OptionalLong timer) {
			super.setTimer(timer);
			questConfig.set("timer", timer.isPresent() ? timer.getAsLong() : null);
		}

		@Override
		public void addQuestFlow(StageController finished) {
			super.addQuestFlow(finished);
			questConfig.set("questFlow",
					super.questFlow.stream().map(StageController::getFlowId).collect(Collectors.joining(";")));
		}

		@Override
		public void resetQuestFlow() {
			super.resetQuestFlow();
			questConfig.set("questFlow", null);
		}

		@Override
		public CompletableFuture<Void> remove() {
			yaml.set("quests." + super.getQuestID(), null);
			return CompletableFuture.completedFuture(null);
		}

	}

	public class PoolData extends AbstractQuesterPoolDataImplementation {

		private final ConfigurationSection poolConfig;

		public PoolData(int poolId) {
			super(poolId);

			if (yaml.isConfigurationSection("pools." + poolId)) {
				poolConfig = yaml.getConfigurationSection("pools." + poolId);
				load();
			} else {
				poolConfig = yaml.createSection("pools." + poolId);
			}
		}

		private void load() {
			super.lastGive = poolConfig.getLong("lastGive");
			super.completedQuests = (Set<Integer>) poolConfig.get("completedQuests", Set.class);
		}

		@Override
		public void setLastGive(long lastGive) {
			super.setLastGive(lastGive);
			poolConfig.set("lastGive", lastGive);
		}

		@Override
		public void setCompletedQuests(Set<Integer> completedQuests) {
			super.setCompletedQuests(completedQuests);
			poolConfig.set("completedQuests", completedQuests);
		}

		@Override
		public @NotNull CompletableFuture<Void> remove() {
			yaml.set("pools." + poolId, null);
			return CompletableFuture.completedFuture(null);
		}

	}

}
