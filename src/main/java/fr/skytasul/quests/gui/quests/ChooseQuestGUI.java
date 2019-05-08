package fr.skytasul.quests.gui.quests;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.RunnableObj;

public class ChooseQuestGUI implements CustomInventory{
	
	private RunnableObj run;
	public final List<Quest> quests;
	
	public Inventory inv;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public ChooseQuestGUI(List<Quest> quests, RunnableObj run){
		Validate.notNull(quests, "Quests cannot be null");
		Validate.notNull(run, "Runnable cannot be null");
		
		this.run = run;
		this.quests = quests;
	}
	
	public Inventory open(Player p){
		if (quests.size() == 0){
			end(p, null);
			return null;
		}else if (quests.size() == 1){
			end(p, quests.get(0));
			return null;
		}
		
		inv = Bukkit.createInventory(null, (int) Math.ceil((quests.size() * 1.0) / 9.0) * 9, Lang.INVENTORY_CHOOSE.toString());

		for (int i = 0; i < quests.size(); i++){
			inv.setItem(i, ItemUtils.item(QuestsConfiguration.getItemMaterial(), ChatColor.YELLOW + quests.get(i).getName()));
		}
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private void end(Player p, Quest c){
		if (inv != null) Inventories.closeAndExit(p);
		run.run(c);
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot < quests.size()) end(p, quests.get(slot));
		return true;
	}

}