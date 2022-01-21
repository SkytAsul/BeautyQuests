package fr.skytasul.quests.gui.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.LevenshteinComparator;
import fr.skytasul.quests.utils.XMaterial;

/**
 * An inventory with an infinite amount of pages of 35 items (integer limit).
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the inventory
 */
public abstract class PagedGUI<T> implements CustomInventory {

	private static ItemStack itemSearch = ItemUtils.item(XMaterial.COMPASS, Lang.search.toString());

	protected Player p;
	protected Inventory inv;
	protected int page = 0;
	protected int maxPage;
	
	private String name;
	private DyeColor color;
	protected List<T> objects;
	protected Consumer<List<T>> validate;
	private ItemStack validationItem = ItemUtils.itemDone;
	private LevenshteinComparator<T> comparator;
	
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
	public Inventory open(Player p) {
		this.p = p;
		calcMaxPages();
		
		inv = Bukkit.createInventory(null, 45, name);

		setBarItem(0, ItemUtils.itemLaterPage);
		setBarItem(4, ItemUtils.itemNextPage);
		if (validate != null) setBarItem(2, validationItem);
		if (comparator != null) setBarItem(3, itemSearch);

		for (int i = 0; i < 5; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
		
		setItems();

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void setValidate(Consumer<List<T>> validate, ItemStack validationItem) {
		if (this.validate != null) throw new IllegalStateException("A validation has already be added.");
		if (this.inv != null) throw new IllegalStateException("Cannot add a validation after inventory opening.");
		if (validationItem == null) throw new IllegalArgumentException("Cannot set a null validation item.");
		this.validate = validate;
		this.validationItem = validationItem;
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
		inv.setItem(slot, is);
		return slot;
	}
	
	private int setBarItem(int barSlot, ItemStack is){
		int slot = barSlot * 9 + 8;
		inv.setItem(slot, is);
		return slot;
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
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
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
				new TextEditor<String>(p, () -> p.openInventory(inv), (obj) -> {
					//objects.stream().filter(x -> getName(x).contains((String) obj));
					objects.sort(comparator.setReference(obj));
					setItems();
					p.openInventory(inv);
				}).enter();
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
	
	public void reopen() {
		Inventories.closeWithoutExit(p);
		Inventories.put(p, this, inv);
		inv = p.openInventory(inv).getTopInventory();
	}
	
	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	public abstract ItemStack getItemStack(T object);

	/**
	 * Called when an object is clicked
	 * @param existing clicked object
	 * @param item item clicked
	 * @param clickType click type
	 */
	public abstract void click(T existing, ItemStack item, ClickType clickType);

}
