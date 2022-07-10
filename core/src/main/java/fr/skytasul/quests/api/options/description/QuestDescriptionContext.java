package fr.skytasul.quests.api.options.description;

import org.bukkit.entity.Player;

public class QuestDescriptionContext {
	
	private final QuestDescription descriptionOptions;
	private final Player player;
	
	public QuestDescriptionContext(QuestDescription descriptionOptions, Player player) {
		this.descriptionOptions = descriptionOptions;
		this.player = player;
	}
	
	public QuestDescription getDescriptionOptions() {
		return descriptionOptions;
	}
	
	public boolean hasPlayer() {
		return player != null;
	}
	
	public Player getPlayer() {
		return player;
	}
	
}
