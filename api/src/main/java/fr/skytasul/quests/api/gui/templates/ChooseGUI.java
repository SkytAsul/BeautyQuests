package fr.skytasul.quests.api.gui.templates;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;

public abstract class ChooseGUI<T> extends Gui {

	private List<T> available;
	
	protected ChooseGUI(List<T> available) {
		this.available = available;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, (int) Math.ceil(available.size() * 1.0 / 9.0) * 9, name());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		for (int i = 0; i < available.size(); i++) {
			inventory.setItem(i, getItemStack(available.get(i)));
		}
	}

	@Override
	public void onClick(GuiClickEvent event) {
		finish(available.get(event.getSlot()));
	}
	
	@Override
	public CloseBehavior onClose(Player player) {
		return StandardCloseBehavior.REOPEN;
	}

	/**
	 * @return Inventory's name
	 */
	public abstract String name();

	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	
	public abstract ItemStack getItemStack(T object);

	/**
	 * Called when the player click on an item
	 */
	public abstract void finish(T object);
	
}
