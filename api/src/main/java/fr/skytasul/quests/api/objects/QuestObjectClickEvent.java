package fr.skytasul.quests.api.objects;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.ItemUtils;

public class QuestObjectClickEvent {
	
	private final @NotNull Player player;
	private final @NotNull QuestObjectGUI gui;
	private final @NotNull ItemStack item;
	private final @NotNull ClickType click;
	private final @NotNull QuestObject clickedObject;
	private final boolean creation;
	
	public QuestObjectClickEvent(@NotNull Player player, @NotNull QuestObjectGUI gui, @NotNull ItemStack item,
			@NotNull ClickType click, boolean creation, @NotNull QuestObject clickedObject) {
		this.player = player;
		this.gui = gui;
		this.item = item;
		this.click = click;
		this.creation = creation;
		this.clickedObject = clickedObject;
	}
	
	public @NotNull Player getPlayer() {
		return player;
	}
	
	public @NotNull QuestObjectGUI getGUI() {
		return gui;
	}
	
	public @NotNull ItemStack getItem() {
		return item;
	}
	
	public @NotNull ClickType getClick() {
		return click;
	}
	
	public boolean isInCreation() {
		return creation;
	}
	
	public void reopenGUI() {
		updateItemLore();
		gui.reopen();
	}
	
	public void cancel() {
		if (creation) gui.remove(clickedObject);
		gui.reopen();
	}
	
	public void remove() {
		gui.remove(clickedObject);
		gui.reopen();
	}
	
	@Deprecated
	public void updateItemLore(String... lore) {
		ItemUtils.lore(item, lore);
	}
	
	public void updateItemLore() {
		ItemUtils.lore(item, clickedObject.getItemLore());
	}
	
}
