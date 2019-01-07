package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;

public class QuestsListGUI implements CustomInventory {

	public RunnableObj run;
	public PlayerAccount acc;
	public boolean started = true;
	public boolean notStarted = true;
	public boolean finished = true;
	
	private Inventory inv;
	private List<Quest> quests;
	
	private int page = 0;
	
	
	public Inventory open(Player p) {
		if (acc != null){
			quests = new ArrayList<>();
			if (started) quests.addAll(QuestsAPI.getQuestsStarteds(acc));
			if (notStarted) quests.addAll(QuestsAPI.getQuestsUnstarted(acc, false));
			if (finished) quests.addAll(QuestsAPI.getQuestsFinished(acc));
		}else quests = QuestsAPI.getQuests();
		
		inv = Bukkit.createInventory(null, 45, Lang.INVENTORY_QUESTS_LIST.toString());

		setBarItem(0, ItemUtils.itemLaterPage());
		setBarItem(4, ItemUtils.itemNextPage());

		for (int i = 0; i < 5; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(DyeColor.CYAN));
		
		setItems();

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void setItems(){
		for (int i = 0; i < 35; i++) setMainItem(i, null);
		for (int i = page * 35; i < quests.size(); i++){
			if (i == (page + 1) * 35) break;
			Quest qu = quests.get(i);
			setMainItem(i - page * 35, ItemUtils.item(QuestsConfiguration.getItemMaterial(), "§6§l§o" + qu.getName() + "    §r§e#" + qu.getID(), Utils.format(Lang.TALK_NPC.toString(), qu.getStarter().getName())));
		}
	}
	
	private int setMainItem(int mainSlot, ItemStack is){
		int line = (int) Math.floor(mainSlot * 1.0 / 7.0);
		//int lineStart = (int) (mainSlot - line * 9);
		int slot = mainSlot + (2 * line);
		inv.setItem(slot, is);
		return slot;
	}
	
	private int setBarItem(int barSlot, ItemStack is){
		int slot = barSlot * 9 + 8;
		inv.setItem(slot, is);
		return slot;
	}

	
	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
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
				page++;
				setItems();
				//p.sendMessage("page " + page);
				break;
			}
			break;
			
		case 7:
			break;
			
		default:
			if (run == null) return;
			int line = (int) Math.floor(slot * 1D / 9D);
			p.closeInventory();
			run.run(quests.get(slot - line*2 + page*35));
		}
	}

}
