package fr.skytasul.quests.gui.creation.stages;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.types.NumberedList;
import fr.skytasul.quests.utils.types.Pair;

public class Line {
	
	Inventory inv;
	private int line = 0;
	
	private int activePage = 0;
	private int maxPage = 1;
	
	NumberedList<Pair<ItemStack, StageRunnable>> items = new NumberedList<>();

	LineData data;
	
	Line(Inventory inv, int line, StagesGUI gui){
		this.inv = inv;
		this.line = line;
		this.data = new LineData(this, gui);
	}
	
	/**
	 * Set an item
	 * @param slot the slot of the item (override if any other item is on the selected slot)
	 * @param is the item
	 * @param click the action when a click on the item (if null nothing happens) <i>(runnable parameter is the player)</i>
	 */
	public void setItem(int slot, ItemStack is, StageRunnable click){
		setItem(slot, is, click, false, true);
	}

	/**
	 * Set an item
	 * @param slot the slot of the item
	 * @param is the item
	 * @param click the action when a click on the item (if null nothing happens) <i>(runnable parameter is the player)</i>
	 * @param override override if any other item is on the selected slot <b>deprecated</b>
	 * @param refresh refresh all items (display this item)
	 */
	public void setItem(int slot, ItemStack is, StageRunnable click, boolean override, boolean refresh){
		Pair<ItemStack, StageRunnable> en = new Pair<>(is, click);
		if (override){
			items.set(slot, en);
		}else {
			if (items.get(slot) != null){
				items.add(en);
			}else items.set(slot, en);
		}
		maxPage = (int) Math.ceil((items.getLast() - 8) * 1.0D / 7.0D) + 1;
		if (maxPage == 0) maxPage = 1;
		if (refresh){
			activePage = 0;
			setItems(activePage);
		}
	}
	
	/**
	 * Set a new instance of ItemStack in the list
	 * @param slot slot of the item that will be changed
	 * @param newItem the new item inserted
	 */
	public void editItem(int slot, ItemStack newItem){
		Pair<ItemStack, StageRunnable> last = items.get(slot);
		if (last == null) return;
		items.set(slot, new Pair<ItemStack, StageRunnable>(newItem, last.getValue()));
	}
	
	/**
	 * Get item from slot
	 * @param slot item slot in the line
	 * @return ItemStack in the gui if showed, or ItemStack stocked
	 */
	public ItemStack getItem(int slot){
		if (line >= data.getGUI().page * 5 && line < (data.getGUI().page+1)*5) return inv.getItem(line*9 - data.getGUI().page*5*9 + (slot - activePage*8));
		return items.get(slot).getKey();
	}
	
	/**
	 * Move all items to a new line
	 * @param newLine the new line (where to move)
	 */
	public void changeLine(int newLine){
		clearLine();
		this.line = newLine;
		clearLine();
		this.activePage = 0;
		setItems(activePage);
	}
	
	/**
	 * Exchange two lines (move all items)
	 * @param other the other line
	 */
	public void exchangeLines(Line other){
		if (other == null || other == this) return;
		int ln = other.line;
		other.changeLine(line);
		this.line = ln;
		this.activePage = 0;
		setItems(activePage);
	}
	
	/**
	 * Set items for selected page
	 * @param page the number of the page to display
	 */
	public void setItems(int page){
		if (!isInGUIPage()) return;
		clearLine();
		this.activePage = page;

		int maxLineCapacity = page == 0 ? 8 : 7;
		int firstID = page == 0 ? 0 : 8 + (page - 1) * 7;

		int slot = page == 0 ? 0 : 1;
		for (int id = firstID; id < firstID + 1 + maxLineCapacity; id++) {
			if (items.contains(id)) {
				Pair<ItemStack, StageRunnable> pair = items.get(id);
				int RSlot = getRSlot(slot);
				inv.setItem(RSlot, pair.getKey());
				pair.setKey(inv.getItem(RSlot));
			}
			slot++;
		}

		if (maxPage > 1){
			if (page < maxPage - 1) RsetItem(8, ItemUtils.itemNextPage);
			if (page > 0) RsetItem(0, ItemUtils.itemLaterPage);
		}
	}
	
	int getActivePage(){
		return activePage;
	}
	
	/**
	 * Get line number (first slot)
	 * @return line number
	 */
	public int getLine(){
		return line;
	}
	
	public boolean isInGUIPage(){
		int gpage = data.getGUI().page;
		return line >= gpage*5 && line < (gpage+1)*5;
	}
	
	public void click(int slot, Player p, ItemStack is){
		slot = slot - (line - data.getGUI().page*5)*9;
		if (slot == 0 && activePage > 0){
			activePage--;
			setItems(activePage);
		}else if (slot == 8){
			if (activePage < maxPage - 1){
				activePage++;
				setItems(activePage);
			}
		}else {
			int item = (activePage == 0 ? 0 : activePage * 7) + slot;
			if (items.get(item) == null) return;
			if (items.get(item).getValue() == null) return;
			items.get(item).getValue().run(p, data, is);
		}
	}
	
	private void clearLine(){
		if (!isInGUIPage()) return;
		for (int i = 0; i < 9; i++){
			RsetItem(i, null);
		}
	}

	private int getRSlot(int lineSlot) {
		return line*9 - data.getGUI().page*5*9 + lineSlot;
	}
	
	/**
	 * Remove all items (and clear the line)
	 */
	public void removeItems(){
		items.clear();
		clearLine();
	}
	
	private void RsetItem(int Rslot, ItemStack is){
		inv.setItem(getRSlot(Rslot), is);
	}
	
}