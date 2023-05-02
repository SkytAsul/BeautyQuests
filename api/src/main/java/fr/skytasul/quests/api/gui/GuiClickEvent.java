package fr.skytasul.quests.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiClickEvent {

	private final @NotNull Player player;
	private final @Nullable ItemStack clicked;
	private final @Nullable ItemStack cursor;
	private final int slot;
	private final @NotNull ClickType click;

	private boolean cancelled;

	public GuiClickEvent(@NotNull Player player, @Nullable ItemStack clicked, @Nullable ItemStack cursor, int slot,
			@NotNull ClickType click) {
		this.player = player;
		this.clicked = clicked;
		this.cursor = cursor;
		this.slot = slot;
		this.click = click;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public @Nullable ItemStack getClicked() {
		return clicked;
	}

	public @Nullable ItemStack getCursor() {
		return cursor;
	}

	public boolean hasCursor() {
		return cursor != null;
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

}
