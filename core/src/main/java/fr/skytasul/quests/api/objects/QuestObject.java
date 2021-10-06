package fr.skytasul.quests.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public interface QuestObject extends Cloneable {
	
	QuestObjectCreator<? extends QuestObject> getCreator();
	
	String getName();
	
	void attach(Quest quest);
	
	void detach();
	
	Quest getAttachedQuest();
	
	QuestObject clone();
	
	default String getDescription() { // will maybe eventually be abstract (and therefore needs to be implemented)
		return null;
	}
	
	default String[] getLore() {
		return new String[] { Lang.Remove.toString() };
	}
	
	default ItemStack getItemStack() {
		return ItemUtils.lore(getCreator().item.clone(), getLore());
	}
	
	void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked);
	
}