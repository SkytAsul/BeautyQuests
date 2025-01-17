package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import fr.skytasul.quests.questers.QuesterPoolDataImplementation;
import fr.skytasul.quests.questers.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataHandler;
import fr.skytasul.quests.questers.data.QuesterQuestDataHandler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class YamlDataHandler implements QuesterDataHandler {

	private final @NotNull Path path;

	private YamlConfiguration yaml = new YamlConfiguration();

	public YamlDataHandler(@NotNull Path path) {
		this.path = path;
	}

	@Override
	public void load(@NotNull AbstractQuesterImplementation quester) {
		yaml = YamlConfiguration.loadConfiguration(path.toFile());

		if (yaml.isList("quests")) {
			// TODO remove, migration to 2.0
			var questsMapList = yaml.getMapList("quests");
			yaml.createSection("quests"); // to remove old list
			for (var questMap : questsMapList) {
				var questId = questMap.get("questID");
				yaml.createSection("quests." + questId, questMap);
			}
		}
		for (String questId : yaml.getConfigurationSection("quests").getKeys(false)) {
			var questConfig = yaml.getConfigurationSection("quests." + questId);
			var questHandler = new YamlQuestDataHandler(questConfig);
			quester.loadQuestData(Integer.parseInt(questId), questHandler);
		}

		for (Map<?, ?> poolConfig : yaml.getMapList("pools")) {
			var poolData = QuesterPoolDataImplementation.deserialize(this, (Map<String, Object>) poolConfig);
			poolDatas.put(poolData.getPoolID(), poolData);
		}

		for (SavableData<?> data : QuestsAPI.getAPI().getQuesterManager().getSavableData()) {
			if (yaml.contains(data.getId()))
				setData(quester, data); // generics error if not in a separate method
		}
	}

	private <T> void setData(@NotNull Quester quester, @NotNull SavableData<T> data) {
		quester.setData(data, yaml.getObject(data.getId(), data.getDataType()));
	}

	@Override
	public <T> @NotNull CompletableFuture<Void> setData(@NotNull SavableData<T> data, @Nullable T value) {
		yaml.set(data.getId(), value);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public @NotNull CompletableFuture<Void> resetData() {
		for (var savableData : QuestsAPI.getAPI().getQuesterManager().getSavableData()) {
			yaml.set(savableData.getId(), null);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public @NotNull QuesterQuestDataHandler createQuestHandler(int id) {
		var questConfig = yaml.getConfigurationSection("quests." + id);
		if (questConfig == null)
			questConfig = yaml.createSection("quests." + id);
		return new YamlQuestDataHandler(questConfig);
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
	public void unload() {}

}
