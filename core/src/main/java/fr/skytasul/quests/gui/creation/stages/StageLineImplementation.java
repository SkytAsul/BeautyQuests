package fr.skytasul.quests.gui.creation.stages;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.stages.creation.StageGuiClickHandler;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;

public class StageLineImplementation implements StageGuiLine {

	private final @NotNull StagesGUI.Line line;
	private final @NotNull Map<Integer, LineItem> items = new HashMap<>();
	
	private int page, maxPage;

	public StageLineImplementation(StagesGUI.Line line) {
		this.line = line;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public @Nullable ItemStack getItem(int slot) {
		if (!isSlotShown(slot))
			return items.get(slot).item;

		return getRawItem(getRawLineSlot(slot));
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack item, @Nullable StageGuiClickHandler click) {
		if (items.containsKey(slot))
			throw new IllegalArgumentException("Slot " + slot + " already taken");

		items.put(slot, new LineItem(item, click));

		computeMaxPage();

		if (isSlotShown(slot))
			refresh();
	}

	@Override
	public void refreshItem(int slot, @NotNull ItemStack item) {
		items.get(slot).item = item;

		if (isSlotShown(slot))
			refresh();
	}

	@Override
	public void removeItem(int slot) {
		items.remove(slot);

		computeMaxPage();

		if (isSlotShown(slot))
			refresh();
	}

	public void clearItems() {
		items.clear();
		maxPage = 1;
		page = 0;
		clearLine();
	}

	@Override
	public int getPage() {
		return page;
	}

	@Override
	public void setPage(int page) {
		if (!isShown())
			return;

		clearLine();
		this.page = page;

		int pageFirst = page == 0 ? 0 : 8 + page * 7;
		int pageCapacity = page == 0 ? 8 : 7;
		int rawSlot = page == 0 ? 0 : 1;
		for (int slot = pageFirst; slot < pageFirst + pageCapacity; slot++) {
			LineItem item = items.get(slot);
			if (item != null) {
				setRawItem(rawSlot, item.item);
				item.item = getRawItem(rawSlot);
				rawSlot++;
			}
		}
	}

	@Override
	public StageGuiClickHandler click(int rawSlot) {
		if (rawSlot == 0 && page > 0) {
			setPage(page - 1);
			return null;
		} else if (rawSlot == 8 && page < maxPage - 1) {
			setPage(page + 1);
			return null;
		} else {
			int slot = (page == 0 ? 0 : page * 7) + rawSlot;
			LineItem item = items.get(slot);

			return item == null ? null : item.handler;
		}
	}

	private void computeMaxPage() {
		// first line: 8 slots (0 to 7, 8 is 2nd page button)
		// next lines: 7 slots (1 to 7, 0 is prev page and 8 is next page)

		int last = items.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		if (last < 8) {
			maxPage = 1;
		} else {
			maxPage = (int) (1 + Math.ceil((last - 8D + 1D) / 7D));
		}

		if (page >= maxPage) {
			page = maxPage - 1;
		}
	}

	private boolean isShown() {
		return line.isShown();
	}

	private boolean isSlotShown(int slot) {
		int pageFirst = page == 0 ? 0 : 8 + page * 7;
		int pageCapacity = page == 0 ? 8 : 7;

		return isShown() && slot >= pageFirst && slot < pageFirst + pageCapacity;
	}

	public void refresh() {
		setPage(page);
	}

	private int getRawLineSlot(int slot) {
		if (slot < 8) {
			if (page != 0)
				return -1;

			return slot;
		} else {
			int slotPage = (int) (1 + Math.ceil((slot - 8D + 1D) / 7D));
			if (slotPage != page)
				return -1;

			return slot - 8 - (slotPage - 1) * 7;
		}
	}

	private void clearLine() {
		if (!isShown())
			return;
		for (int i = 0; i < 9; i++)
			setRawItem(i, null);
	}

	private void setRawItem(int rawSlot, ItemStack item) {
		if (!isShown())
			throw new IllegalStateException();
		line.setItem(rawSlot, item);
	}

	private ItemStack getRawItem(int rawSlot) {
		if (!isShown())
			throw new IllegalStateException();
		return line.getItem(rawSlot);
	}

	class LineItem {

		ItemStack item;
		StageGuiClickHandler handler;

		public LineItem(ItemStack item, StageGuiClickHandler handler) {
			this.item = item;
			this.handler = handler;
		}

	}

}
