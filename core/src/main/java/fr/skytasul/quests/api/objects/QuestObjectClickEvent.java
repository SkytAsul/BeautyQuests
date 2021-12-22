package fr.skytasul.quests.api.objects;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectClickEvent {
	
	private final Player player;
	private final QuestObjectGUI<? extends QuestObject> gui;
	private final ItemStack item;
	private final ClickType click;
	private final boolean creation;
	
	public QuestObjectClickEvent(Player player, QuestObjectGUI<? extends QuestObject> gui, ItemStack item, ClickType click, boolean creation) {
		this.player = player;
		this.gui = gui;
		this.item = item;
		this.click = click;
		this.creation = creation;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public QuestObjectGUI<? extends QuestObject> getGUI() {
		return gui;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public ClickType getClick() {
		return click;
	}
	
	public boolean isInCreation() {
		return creation;
	}
	
	public void reopenGUI() {
		gui.reopen();
	}
	
	public void updateItemLore(String... lore) {
		ItemUtils.lore(item, lore);
	}
	
	public void updateItemLore(List<String> lore) {
		ItemUtils.lore(item, lore);
	}
	
}
