package fr.skytasul.quests.api.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;

public abstract class AbstractRequirement extends QuestObject {
	
	protected AbstractRequirement() {
		super(QuestsAPI.getRequirements());
	}
	
	/**
	 * Called when the plugin has to check if a player can start a quest with this requirement
	 * @param p Player to test
	 * @return if the player fills conditions of this requirement
	 */
	public abstract boolean test(Player p);
	
	/**
	 * Called if the condition if not filled and if the plugin allows to send a message to the player
	 * @param p Player to send the reason
	 */
	public void sendReason(Player p) {}
	
	@Override
	public abstract AbstractRequirement clone();
	
	public static AbstractRequirement deserialize(Map<String, Object> map) {
		return QuestObject.deserialize(map, QuestsAPI.getRequirements());
	}
	
}
