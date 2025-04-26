package fr.skytasul.quests.api.gui.templates;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * An inventory which has up to 54 slots to store items. Each item is linked in a list to an instance of type T.
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the list
 */
public abstract class ListGUI<T> extends PagedGUI<T> {

	private ItemStack create = ItemUtils.item(XMaterial.SLIME_BALL, Lang.addObject.toString());

	protected ListGUI(@NotNull String name, @NotNull DyeColor color, @NotNull Collection<T> objects) {
		super(name, color, objects);
		if (objects.contains(null))
			throw new IllegalArgumentException("Object cannot be null in a list GUI");

		super.objects.add(null);
		super.validate = list -> {
			list.remove(null);
			finish(list);
		};
	}

	@Override
	public final ItemStack getItemStack(T object) {
		return object == null ? create : getObjectItemStack(object);
	}

	@Override
	public final void click(T existing, ItemStack item, ClickType clickType) {
		if (existing != null && clickType == getRemoveClick(existing)) {
			remove(existing);
		}else {
			if (existing == null) {
				createObject(this::finishItem);
			}else clickObject(existing, item, clickType);
		}
	}

	protected ClickType getRemoveClick(@NotNull T object) {
		return ClickType.SHIFT_LEFT;
	}

	protected @NotNull LoreBuilder createLoreBuilder(@NotNull T object) {
		return new LoreBuilder()
				.addClick(getRemoveClick(object), "§c" + Lang.Remove.toString());
	}

	public boolean remove(T object) {
		int index = objects.indexOf(object);
		if (index == -1) return false;
		remove(index);
		return true;
	}

	public void remove(int slot) {
		T removed = objects.remove(slot);
		if (removed == null) return;
		calcMaxPages();
		page = maxPage - 1;
		setItems();
		removed(removed);
	}

	protected void removed(T object) {}

	public void updateObject(T object, T newObject) {
		int index = objects.indexOf(object);
		if (index == -1) return;
		objects.set(index, newObject);
		int slot = getObjectSlot(newObject);
		if (slot != -1)
			getInventory().setItem(slot, getItemStack(newObject));
	}

	@Override
	public CloseBehavior onClose(Player player) {
		return StandardCloseBehavior.REOPEN;
	}

	/**
	 * Call this when an object is ready to be inserted in the list
	 *
	 * @param object Object to put
	 * @return ItemStack created with {@link #getItemStack(Object)}
	 */
	private ItemStack finishItem(T object) {
		Validate.notNull(object);
		objects.add(objects.size() - 1, object);
		calcMaxPages();
		page = maxPage - 1;
		setItems();
		reopen(player);

		int slot = getObjectSlot(object);
		return slot != -1 ? getInventory().getItem(slot) : getObjectItemStack(object);
	}

	/**
	 * Called when the player hit the finish button
	 */
	public abstract void finish(List<T> objects);

	public abstract ItemStack getObjectItemStack(T object);

	public abstract void createObject(Function<T, ItemStack> callback);

	public void clickObject(T object, ItemStack item, ClickType clickType) {}

}
