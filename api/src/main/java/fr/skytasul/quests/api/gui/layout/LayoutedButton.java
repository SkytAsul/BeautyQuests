package fr.skytasul.quests.api.gui.layout;

import java.util.List;
import java.util.function.Supplier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;

public interface LayoutedButton extends LayoutedClickHandler {

	public void place(@NotNull Inventory inventory, int slot);

	interface ItemButton extends LayoutedButton {

		public @Nullable ItemStack getItem();

		@Override
		default void place(@NotNull Inventory inventory, int slot) {
			inventory.setItem(slot, getItem());
		}

	}

	public static @NotNull LayoutedButton create(@NotNull XMaterial material, @Nullable String name, @Nullable List<String> lore,
			@NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(material, name, lore);
			}
			
		};
	}

	public static @NotNull LayoutedButton create(@NotNull XMaterial material, @Nullable String name,
			@NotNull Supplier<@Nullable List<String>> lore, @NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(material, name, lore.get());
			}

		};
	}

	public static @NotNull LayoutedButton create(@NotNull XMaterial material, @NotNull Supplier<@Nullable String> name,
			@NotNull Supplier<@Nullable List<String>> lore, @NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(material, name.get(), lore.get());
			}

		};
	}

	public static @NotNull LayoutedButton create(@NotNull XMaterial material, @NotNull Supplier<@Nullable String> name,
			@Nullable List<String> lore, @NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(material, name.get(), lore);
			}

		};
	}

	public static @NotNull LayoutedButton create(@NotNull Supplier<@NotNull XMaterial> material,
			@NotNull Supplier<@Nullable String> name, @NotNull Supplier<@Nullable List<String>> lore,
			@NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return ItemUtils.item(material.get(), name.get(), lore.get());
			}

		};
	}

	public static @NotNull LayoutedButton create(@NotNull Supplier<@Nullable ItemStack> item, @NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return item.get();
			}
			
		};
	}

	public static @NotNull LayoutedButton create(@Nullable ItemStack item, @NotNull LayoutedClickHandler click) {
		return new ItemButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				click.click(event);
			}

			@Override
			public @Nullable ItemStack getItem() {
				return item;
			}

		};
	}

}
