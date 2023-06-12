package fr.skytasul.quests.api.gui.layout;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.gui.GuiClickEvent;

public class LayoutedClickEvent extends GuiClickEvent {

	private @NotNull LayoutedGUI gui;

	public LayoutedClickEvent(@NotNull Player player, @NotNull LayoutedGUI gui, @Nullable ItemStack clicked,
			@Nullable ItemStack cursor, int slot, @NotNull ClickType click) {
		super(player, gui, clicked, cursor, slot, click);
		this.gui = gui;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull LayoutedGUI getGui() {
		return gui;
	}

	public void refreshItem() {
		gui.refresh(getSlot());
	}

	public void refreshItemReopen() {
		gui.refresh(getSlot());
		gui.reopen(getPlayer());
	}

	public void refreshGuiReopen() {
		gui.reopen(getPlayer(), true);
	}

}
