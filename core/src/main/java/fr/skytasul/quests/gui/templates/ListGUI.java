package fr.skytasul.quests.gui.templates;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

/**
 * An inventory which has up to 54 slots to store items. Each item is linked in a list to an instance of type T.
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the list
 */
public abstract class ListGUI<T> extends PagedGUI<T> {
	
	private ItemStack create = ItemUtils.item(XMaterial.SLIME_BALL, Lang.addObject.toString());
	
	public ListGUI(String name, DyeColor color, List<T> objects) {
		super(name, color, objects);
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
		if (clickType == ClickType.MIDDLE) {
			remove(existing);
		}else {
			if (existing == null) {
				createObject(this::finishItem);
			}else clickObject(existing, item, clickType);
		}
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
		if (slot != -1) inv.setItem(slot, getItemStack(newObject));
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
	}
	
	/**
	 * Call this when an object is ready to be inserted in the list
	 * @param object Object to put
	 * @return ItemStack created with {@link #getItemStack(Object)}
	 */
	private ItemStack finishItem(T object) {
		Validate.notNull(object);
		objects.add(objects.size() - 1, object);
		calcMaxPages();
		page = maxPage - 1;
		setItems();
		reopen();
		return inv.getItem(getObjectSlot(object));
	}
	
	/**
	 * Called when the player hit the finish button
	 */
	public abstract void finish(List<T> objects);
	
	public abstract ItemStack getObjectItemStack(T object);
	
	public abstract void createObject(Function<T, ItemStack> callback);
	
	public void clickObject(T object, ItemStack item, ClickType clickType) {}
	
}
