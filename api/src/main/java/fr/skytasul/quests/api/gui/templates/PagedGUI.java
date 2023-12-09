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
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.LevenshteinComparator;

/**
 * An inventory with an infinite amount of pages of 35 items (integer limit).
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the inventory
 */
public abstract class PagedGUI<T> extends AbstractGui {

	private static ItemStack itemSearch = ItemUtils.item(XMaterial.COMPASS, Lang.search.toString());

	protected Player player;
	protected int page = 0;
	protected int maxPage;

	private final int columns;
	private final int dataSlots;

	private String name;
	private DyeColor color;
	protected List<T> objects;
	protected Consumer<List<T>> validate;
	private ItemStack validationItem = QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone();
	protected LevenshteinComparator<T> comparator;

	protected PagedGUI(@NotNull String name, @Nullable DyeColor color, @NotNull Collection<T> objects) {
		this(name, color, objects, null, null);
	}

	protected PagedGUI(@NotNull String name, @Nullable DyeColor color, @NotNull Collection<T> objects,
			@Nullable Consumer<List<T>> validate, @Nullable Function<T, String> searchName) {
		this.name = name;
		this.color = color;
		this.objects = new ArrayList<>(objects);
		this.validate = validate;
		if (searchName != null) this.comparator = new LevenshteinComparator<>(searchName);

		columns = QuestsConfiguration.getConfig().getGuiConfig().showVerticalSeparator() ? 7 : 8;
		dataSlots = columns * 5;
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 45, name);
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		this.player = player;
		calcMaxPages();

		setBarItem(0, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getPreviousPage());
		setBarItem(4, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getNextPage());
		if (validate != null) setBarItem(2, validationItem);
		if (comparator != null) setBarItem(3, itemSearch);

		displaySeparator();

		setItems();
	}

	private void displaySeparator() {
		if (color != null && QuestsConfiguration.getConfig().getGuiConfig().showVerticalSeparator()) {
			for (int i = 0; i < 5; i++)
				getInventory().setItem(i * 9 + columns, ItemUtils.itemSeparator(color));
		}
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

	public void setSeparatorColor(@Nullable DyeColor color) {
		this.color = color;
		if (getInventory() != null)
			displaySeparator();
	}

	public void setObjects(@NotNull Collection<T> objects) {
		this.objects = new ArrayList<>(objects);
		page = 0;
		calcMaxPages();
		setItems();
	}

	protected void calcMaxPages() {
		this.maxPage = objects.isEmpty() ? 1 : (int) Math.ceil(objects.size() * 1D / 35D);
	}

	protected void setItems() {
		for (int i = 0; i < dataSlots; i++)
			setMainItem(i, null);
		for (int i = page * dataSlots; i < objects.size(); i++) {
			if (i == (page + 1) * dataSlots)
				break;
			T obj = objects.get(i);
			setMainItem(i - page * dataSlots, getItemStack(obj));
		}
	}

	private int setMainItem(int mainSlot, ItemStack is){
		int line = (int) Math.floor(mainSlot * 1.0 / columns);
		int slot = mainSlot + ((9 - columns) * line);
		setItem(is, slot);
		return slot;
	}

	protected int setBarItem(int barSlot, ItemStack is) {
		int slot = barSlot * 9 + 8; // always at last column so +8
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
		if (index < page * dataSlots || index > (page + 1) * dataSlots)
			return -1;

		int line = (int) Math.floor(index * 1.0 / columns);
		return index + ((9 - columns) * line) - page * dataSlots;
	}


	@Override
	public void onClick(GuiClickEvent event) {
		int column = event.getSlot() % 9;
		if (column == 8) {
			int barSlot = (event.getSlot() - 8) / 9;
			barClick(event, barSlot);
		} else if (!QuestsConfiguration.getConfig().getGuiConfig().showVerticalSeparator() || column != 7) {
			int line = (int) Math.floor(event.getSlot() * 1D / 9D);
			int objectSlot = event.getSlot() - line * (9 - columns) + page * dataSlots;
			click(objects.get(objectSlot), event.getClicked(), event.getClick());
			// inv.setItem(slot, getItemStack(objects.get(objectSlot)));
		}
	}

	protected void barClick(GuiClickEvent event, int barSlot) {
		switch (barSlot) {
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
					objects.sort(comparator.setReference(obj));
					page = 0;
					setItems();
					reopen();
				}).start();
				break;
			}
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
