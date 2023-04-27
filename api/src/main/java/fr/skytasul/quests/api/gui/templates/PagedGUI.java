package fr.skytasul.quests.api.gui.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.LevenshteinComparator;

/**
 * An inventory with an infinite amount of pages of 35 items (integer limit).
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the inventory
 */
public abstract class PagedGUI<T> extends CustomInventory {

	private static ItemStack itemSearch = ItemUtils.item(XMaterial.COMPASS, Lang.search.toString());

	protected Player player;
	protected int page = 0;
	protected int maxPage;
	
	private String name;
	private DyeColor color;
	protected List<T> objects;
	protected Consumer<List<T>> validate;
	private ItemStack validationItem = ItemUtils.itemDone;
	protected LevenshteinComparator<T> comparator;
	
	protected PagedGUI(String name, DyeColor color, Collection<T> objects) {
		this(name, color, objects, null, null);
	}
	
	protected PagedGUI(String name, DyeColor color, Collection<T> objects, Consumer<List<T>> validate, Function<T, String> searchName) {
		this.name = name;
		this.color = color;
		this.objects = new ArrayList<>(objects);
		this.validate = validate;
		if (searchName != null) this.comparator = new LevenshteinComparator<>(searchName);
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 45, name);
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		this.player = player;
		calcMaxPages();
		
		setBarItem(0, ItemUtils.itemLaterPage);
		setBarItem(4, ItemUtils.itemNextPage);
		if (validate != null) setBarItem(2, validationItem);
		if (comparator != null) setBarItem(3, itemSearch);

		for (int i = 0; i < 5; i++)
			inventory.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
		
		setItems();
	}
	
	public PagedGUI<T> setValidate(Consumer<List<T>> validate, ItemStack validationItem) {
		if (this.validate != null) throw new IllegalStateException("A validation has already be added.");
		if (this.getInventory() != null)
			throw new IllegalStateException("Cannot add a validation after inventory opening.");
		if (validationItem == null) throw new IllegalArgumentException("Cannot set a null validation item.");
		this.validate = validate;
		this.validationItem = validationItem;
		return this;
	}
	
	public PagedGUI<T> sortValuesByName() {
		Validate.notNull(comparator);
		sortValues(comparator.getFunction());
		return this;
	}
	
	public <C extends Comparable<C>> PagedGUI<T> sortValues(Function<T, C> mapper) {
		objects.sort((o1, o2) -> {
			C map1;
			if (o1 == null || (map1 = mapper.apply(o1)) == null) return 1;
			C map2;
			if (o2 == null || (map2 = mapper.apply(o2)) == null) return -1;
			return map1.compareTo(map2);
		});
		return this;
	}
	
	protected void calcMaxPages() {
		this.maxPage = objects.isEmpty() ? 1 : (int) Math.ceil(objects.size() * 1D / 35D);
	}
	
	protected void setItems() {
		for (int i = 0; i < 35; i++) setMainItem(i, null);
		for (int i = page * 35; i < objects.size(); i++){
			if (i == (page + 1) * 35) break;
			T obj = objects.get(i);
			setMainItem(i - page * 35, getItemStack(obj));
		}
	}
	
	private int setMainItem(int mainSlot, ItemStack is){
		int line = (int) Math.floor(mainSlot * 1.0 / 7.0);
		int slot = mainSlot + (2 * line);
		setItem(is, slot);
		return slot;
	}
	
	private int setBarItem(int barSlot, ItemStack is){
		int slot = barSlot * 9 + 8;
		setItem(is, slot);
		return slot;
	}

	private void setItem(ItemStack is, int rawSlot) {
		getInventory().setItem(rawSlot, is);

		if (is != null && is.getType() != Material.AIR) {
			ItemStack invItem = getInventory().getItem(rawSlot);
			if (invItem == null || invItem.getType() == Material.AIR) {
				// means the item was a material that cannot be put in an inventory:
				// fire, water block, crops...
				is = is.clone();
				is.setType(Material.STONE);
				getInventory().setItem(rawSlot, is);
			}
		}
	}
	
	/**
	 * @param object T object to get the slot from
	 * @return slot in the inventory, -1 if the object is on another page
	 */
	public int getObjectSlot(T object){
		int index = objects.indexOf(object);
		if (index < page*35 || index > (page + 1)*35) return -1;
		
		int line = (int) Math.floor(index * 1.0 / 7.0);
		return index + (2 * line);
	}

	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		switch (slot % 9){
		case 8:
			int barSlot = (slot - 8) / 9;
			switch (barSlot){
			case 0:
				if (page == 0) break;
				page--;
				setItems();
				break;
			case 4:
				if (page+1 == maxPage) break;
				page++;
				setItems();
				break;
				
			case 2:
				validate.accept(objects);
				break;

			case 3:
				new TextEditor<String>(player, this::reopen, obj -> {
					//objects.stream().filter(x -> getName(x).contains((String) obj));
					objects.sort(comparator.setReference(obj));
					page = 0;
					setItems();
					reopen();
				}).start();
				break;
			}
			break;
			
		case 7:
			break;
			
		default:
			int line = (int) Math.floor(slot * 1D / 9D);
			int objectSlot = slot - line*2 + page*35;
			click(objects.get(objectSlot), current, click);
			//inv.setItem(slot, getItemStack(objects.get(objectSlot)));
		}
		return true;
	}
	
	public final void reopen() {
		reopen(player);
	}

	public final void close() {
		close(player);
	}

	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	public abstract @NotNull ItemStack getItemStack(@NotNull T object);

	/**
	 * Called when an object is clicked
	 * @param existing clicked object
	 * @param item item clicked
	 * @param clickType click type
	 */
	public abstract void click(@NotNull T existing, @NotNull ItemStack item, @NotNull ClickType clickType);

}
