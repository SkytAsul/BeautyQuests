package fr.skytasul.quests.api.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiClickEvent {

	private final @NotNull Player player;
	private final @NotNull Gui gui;
	private final @Nullable ItemStack clicked;
	private final @Nullable ItemStack cursor;
	private final int slot;
	private final @NotNull ClickType click;

	private boolean cancelled = true;

	public GuiClickEvent(@NotNull Player player, @NotNull Gui gui, @Nullable ItemStack clicked, @Nullable ItemStack cursor,
			int slot, @NotNull ClickType click) {
		this.player = player;
		this.gui = gui;
		this.clicked = clicked;
		this.cursor = cursor;
		this.slot = slot;
		this.click = click;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public @NotNull Gui getGui() {
		return gui;
	}

	public @Nullable ItemStack getClicked() {
		return clicked;
	}

	public @Nullable ItemStack getCursor() {
		return cursor;
	}

	public boolean hasCursor() {
		return cursor != null && cursor.getType() != Material.AIR;
	}

	public int getSlot() {
		return slot;
	}

	public @NotNull ClickType getClick() {
		return click;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void reopen() {
		gui.reopen(player);
	}

	public void close() {
		gui.close(player);
	}

}
