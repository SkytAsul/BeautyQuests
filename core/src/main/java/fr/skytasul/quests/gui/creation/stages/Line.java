package fr.skytasul.quests.gui.creation.stages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.types.NumberedList;
import fr.skytasul.quests.utils.types.Pair;

public class Line {
	
	public final StagesGUI gui;
	private int line = 0;
	
	private int activePage = 0;
	private int maxPage = 1;
	
	private NumberedList<Pair<ItemStack, StageRunnable>> items = new NumberedList<>();

	private Map<String, Object> data = new HashMap<>();
	public StageCreation<?> creation = null;
	
	Line(int line, StagesGUI gui) {
		this.gui = gui;
		this.line = line;
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
		if (items.getLast() <= 8) {
			maxPage = 1;
		}else maxPage = 1 + (int) Math.ceil((items.getLast() - 7) * 1.0D / 7.0D);
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
		if (line >= gui.page * 5 && line < (gui.page + 1) * 5) return gui.inv.getItem(line * 9 - gui.page * 5 * 9 + (slot - activePage * 8));
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

		int maxLineCapacity = page == 0 || maxPage == 1 ? 8 : 7;
		int firstID = page == 0 ? 0 : 8 + (page - 1) * 7;
		
		int slot = page == 0 ? 0 : 1;
		for (int id = firstID; id <= firstID + maxLineCapacity; id++) {
			if (items.contains(id)) {
				Pair<ItemStack, StageRunnable> pair = items.get(id);
				int RSlot = getRSlot(slot);
				gui.inv.setItem(RSlot, pair.getKey());
				pair.setKey(gui.inv.getItem(RSlot));
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
		return line >= gui.page*5 && line < (gui.page+1)*5;
	}
	
	public void click(int slot, Player p, ItemStack is){
		if (slot == 0 && activePage > 0){
			activePage--;
			setItems(activePage);
		}else if (slot == 8){
			if (activePage < maxPage/* - 1*/) {
				activePage++;
				setItems(activePage);
			}
		}else {
			int item = (activePage == 0 ? 0 : activePage * 7) + slot;
			if (items.get(item) == null) return;
			if (items.get(item).getValue() == null) return;
			execute(item, p, is);
		}
	}
	
	public void execute(int lineSlot, Player p, ItemStack is) {
		items.get(lineSlot).getValue().run(p, is);
	}
	
	private void clearLine(){
		if (!isInGUIPage()) return;
		for (int i = 0; i < 9; i++){
			RsetItem(i, null);
		}
	}

	private int getRSlot(int lineSlot) {
		return line * 9 - gui.page * 5 * 9 + lineSlot;
	}
	
	/**
	 * Remove all items (and clear the line)
	 */
	public void removeItems(){
		items.clear();
		maxPage = 1;
		clearLine();
	}
	
	private void RsetItem(int Rslot, ItemStack is){
		gui.inv.setItem(getRSlot(Rslot), is);
	}
	
}