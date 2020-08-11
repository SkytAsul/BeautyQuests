package fr.skytasul.quests.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public interface QuestObject extends Cloneable {
	
	QuestObjectCreator<? extends QuestObject> getCreator();
	
	String getName();
	
	void attach(Quest quest);
	
	void detach();
	
	QuestObject clone();
	
	default String[] getLore() {
		return new String[] { Lang.Remove.toString() };
	}
	
	ItemStack getItemStack();
	
	void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked);
	
}