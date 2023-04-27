package fr.skytasul.quests.api.gui.layout;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public class ClickEvent {

	private final @NotNull Player player;
	private final @NotNull LayoutedGUI gui;
	private final int slot;
	private final @NotNull ClickType click;

	public ClickEvent(@NotNull Player player, @NotNull LayoutedGUI gui, int slot, @NotNull ClickType click) {
		this.player = player;
		this.gui = gui;
		this.slot = slot;
		this.click = click;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public @NotNull LayoutedGUI getGui() {
		return gui;
	}

	public int getSlot() {
		return slot;
	}

	public @NotNull ClickType getClick() {
		return click;
	}

	public void reopen() {
		gui.reopen(player);
	}

	public void refreshItemReopen() {
		gui.refresh(slot);
		gui.reopen(player);
	}

	public void refreshGuiReopen() {
		gui.reopen(player, true);
	}

	public void close() {
		gui.close(player);
	}

}
