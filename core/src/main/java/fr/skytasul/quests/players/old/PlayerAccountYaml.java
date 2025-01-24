package fr.skytasul.quests.players.old;

import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.players.PlayerQuesterImplementation;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.questers.QuesterPoolDataImplementation;
import fr.skytasul.quests.questers.AbstractQuesterQuestDataImplementation;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PlayerAccountYaml extends PlayerQuesterImplementation {

	private final @NotNull PlayersManagerYAML playersManager;

	protected PlayerAccountYaml(@NotNull PlayersManagerYAML playersManager, @NotNull AbstractAccount account, int index) {
		super(account, index);
		this.playersManager = playersManager;
	}

	public void load(ConfigurationSection datas) {

	}

	@Override
	public void unload() {
		super.unload();
		playersManager.unloadAccount(this);
	}

	public void serialize(@NotNull ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", questDatas.isEmpty() ? null
				: Utils.serializeList(questDatas.values(), AbstractQuesterQuestDataImplementation::serialize));
		config.set("pools", poolDatas.isEmpty() ? null
				: Utils.serializeList(poolDatas.values(), QuesterPoolDataImplementation::serialize));
		additionalDatas.entrySet().forEach(entry -> {
			config.set(entry.getKey().getId(), entry.getValue());
		});
	}

}
