package fr.skytasul.quests.players.yaml;

import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import fr.skytasul.quests.players.PlayerPoolDatasImplementation;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class PlayerAccountYaml extends PlayerAccountImplementation {

	private final @NotNull PlayersManagerYAML playersManager;

	protected PlayerAccountYaml(@NotNull PlayersManagerYAML playersManager, @NotNull AbstractAccount account, int index) {
		super(account, index);
		this.playersManager = playersManager;
	}

	public void load(ConfigurationSection datas) {
		for (Map<?, ?> questConfig : datas.getMapList("quests")) {
			var questData = PlayerQuestDatasImplementation.deserialize(this, (Map<String, Object>) questConfig);
			this.questDatas.put(questData.getQuestID(), questData);
		}
		for (Map<?, ?> poolConfig : datas.getMapList("pools")) {
			var poolData = PlayerPoolDatasImplementation.deserialize(this, (Map<String, Object>) poolConfig);
			poolDatas.put(poolData.getPoolID(), poolData);
		}
		for (SavableData<?> data : playersManager.getAccountDatas()) {
			if (datas.contains(data.getId())) {
				additionalDatas.put(data, datas.getObject(data.getId(), data.getDataType()));
			}
		}
	}

	@Override
	public void unload() {
		super.unload();
		playersManager.unloadAccount(this);
	}

}
