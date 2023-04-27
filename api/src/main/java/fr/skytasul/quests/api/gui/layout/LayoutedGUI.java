package fr.skytasul.quests.api.gui.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;

public abstract class LayoutedGUI extends CustomInventory {

	protected final @Nullable String name;
	protected final @NotNull Map<Integer, Button> buttons;
	protected final @NotNull CloseBehavior closeBehavior;

	protected LayoutedGUI(@Nullable String name, @NotNull Map<Integer, Button> buttons,
			@NotNull CloseBehavior closeBehavior) {
		this.name = name;
		this.buttons = buttons;
		this.closeBehavior = closeBehavior;
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		buttons.forEach((slot, button) -> button.place(inventory, slot));
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ItemStack current, int slot, @NotNull ClickType click) {
		Button button = buttons.get(slot);
		if (button == null)
			return true;

		ClickEvent event = new ClickEvent(player, this, slot, click);
		button.click(event);
		return true;
	}

	public void refresh(int slot) {
		if (getInventory() == null)
			return;

		Button button = buttons.get(slot);
		if (button == null)
			return;

		button.place(getInventory(), slot);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return closeBehavior;
	}

	public static @NotNull Builder newBuilder() {
		return new Builder();
	}

	public static class LayoutedRowsGUI extends LayoutedGUI {

		private final int rows;

		protected LayoutedRowsGUI(@Nullable String name, @NotNull Map<Integer, Button> buttons,
				@NotNull CloseBehavior closeBehavior, int rows) {
			super(name, buttons, closeBehavior);
			Validate.isTrue(rows >= 1);
			this.rows = rows;
		}

		@Override
		protected Inventory instanciate(@NotNull Player player) {
			return Bukkit.createInventory(null, rows, name);
		}

	}

	public static class LayoutedTypeGUI extends LayoutedGUI {

		private @NotNull InventoryType type;

		protected LayoutedTypeGUI(@Nullable String name, @NotNull Map<Integer, Button> buttons,
				@NotNull CloseBehavior closeBehavior, @NotNull InventoryType type) {
			super(name, buttons, closeBehavior);
			this.type = Objects.requireNonNull(type);
		}

		@Override
		protected Inventory instanciate(@NotNull Player player) {
			return Bukkit.createInventory(null, type, name);
		}

	}

	public static class Builder {

		private final Map<Integer, Button> buttons = new HashMap<>();
		private @Nullable Integer rows = null;
		private @Nullable InventoryType type = null;
		private @Nullable String name = null;
		private @NotNull CloseBehavior closeBehavior = StandardCloseBehavior.CONFIRM;

		private Builder() {}

		public @NotNull Builder addButton(int slot, @NotNull Button button) {
			Validate.isTrue(!buttons.containsKey(slot));
			buttons.put(slot, button);
			return this;
		}

		public @NotNull Builder setRowNumber(int rows) {
			Validate.isTrue(rows >= 1);
			this.rows = rows;
			this.type = null;
			return this;
		}

		public @NotNull Builder setInventoryType(@Nullable InventoryType type) {
			this.type = type;
			this.rows = null;
			return this;
		}

		public @NotNull Builder setName(@Nullable String name) {
			this.name = name;
			return this;
		}

		public @NotNull Builder setCloseBehavior(@NotNull CloseBehavior closeBehavior) {
			this.closeBehavior = closeBehavior;
			return this;
		}

		public @NotNull LayoutedGUI build() {
			if (buttons.isEmpty())
				throw new IllegalArgumentException("Cannot build a layouted GUI with no buttons");
			
			if (type != null)
				return new LayoutedTypeGUI(name, buttons, closeBehavior, type);

			if (rows == null) {
				int maxSlot = buttons.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
				rows = (int) Math.floor(maxSlot / 9D) + 1;
			}

			return new LayoutedRowsGUI(name, buttons, closeBehavior, rows);
		}

	}

}
