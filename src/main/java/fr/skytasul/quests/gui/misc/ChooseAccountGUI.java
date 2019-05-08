package fr.skytasul.quests.gui.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class ChooseAccountGUI implements CustomInventory {

	private Inventory inv;
	
	private RunnableObj run;
	private UUID target;
	
	private List<PlayerAccount> accounts;
	
	public ChooseAccountGUI(UUID target, RunnableObj run){
		this.run = run;
		this.target = target;
	}
	
	
	public Inventory open(Player p){
		accounts = new ArrayList<>(PlayersManager.getPlayerAccounts(target));
		
		if (accounts.size() == 0) return null;
		if (accounts.size() == 1){
			run.run(accounts.get(0));
			return null;
		}
		
		inv = Bukkit.createInventory(null, (int) (Math.ceil(accounts.size()*1.0 / 9D)*9), Lang.INVENTORY_CHOOSEACCOUNT.toString());
		
		int color = 0;
		for (int i = 0; i < accounts.size(); i++){
			PlayerAccount acc = accounts.get(i);
			String identifier = "§b";
			String[] args = acc.abstractAcc.getIdentifier().split("\\|");
			for (int ii = 0; ii < args.length; ii++){
				identifier = identifier + args[ii] + (ii + 1 == args.length ? "" : "§a|§b");
			}
			inv.setItem(i, ItemUtils.item(XMaterial.requestXMaterial("STAINED_GLASS_PANE", (byte) color), "§9All-ID: §b" + PlayersManager.getAccountIndex(acc), "§9Identifier:", identifier));
			color++;
			if (color == 16) color = 0;
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		Inventories.closeAndExit(p);
		run.run(accounts.get(slot));
		return true;
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

}
