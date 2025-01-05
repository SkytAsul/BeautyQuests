package fr.skytasul.quests.players.yaml;

import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.players.PlayerPoolDatasImplementation;
import fr.skytasul.quests.players.PlayerQuestDatasImplementation;
import fr.skytasul.quests.players.PlayerQuesterImplementation;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class PlayerAccountYaml extends PlayerQuesterImplementation {

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

	public void serialize(@NotNull ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", questDatas.isEmpty() ? null
				: Utils.serializeList(questDatas.values(), PlayerQuestDatasImplementation::serialize));
		config.set("pools", poolDatas.isEmpty() ? null
				: Utils.serializeList(poolDatas.values(), PlayerPoolDatasImplementation::serialize));
		additionalDatas.entrySet().forEach(entry -> {
			config.set(entry.getKey().getId(), entry.getValue());
		});
	}

}
