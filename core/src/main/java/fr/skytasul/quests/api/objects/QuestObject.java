package fr.skytasul.quests.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public abstract class QuestObject extends SerializableObject implements Cloneable {
	
	private Quest quest;
	
	protected QuestObject(QuestObjectsRegistry registry) {
		super(registry);
	}
	
	@Override
	public QuestObjectCreator getCreator() {
		return (QuestObjectCreator) super.getCreator();
	}
	
	public void attach(Quest quest) {
		this.quest = quest;
	}
	
	public void detach() {
		this.quest = null;
	}
	
	public Quest getAttachedQuest() {
		return quest;
	}
	
	public String debugName() {
		return getClass().getSimpleName() + (quest == null ? ", unknown quest" : (", quest " + quest.getID()));
	}
	
	@Override
	public abstract QuestObject clone();
	
	public String getDescription(Player p) { // will maybe eventually be abstract (and therefore needs to be implemented)
		return null;
	}
	
	public String[] getLore() {
		return new String[] { Lang.RemoveMid.toString() };
	}
	
	public ItemStack getItemStack() {
		return ItemUtils.lore(getCreator().item.clone(), getLore());
	}
	
	public abstract void itemClick(QuestObjectClickEvent event);
	
}