package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.RunnableObj;
import net.citizensnpcs.api.npc.NPC;

public class ChooseQuestGUI implements CustomInventory{
	
	private RunnableObj run;
	public final List<Quest> quests;
	private boolean ol;
	
	public Inventory inv;
	
	private boolean finish = false;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public ChooseQuestGUI(Player p, NPC npc, RunnableObj run, boolean onlyLauncheable){
		Validate.notNull(npc, "NPC cannot be null");
		Validate.isTrue(BeautyQuests.npcs.containsKey(npc), "NPC specified is not a quest launcher");
		
		Validate.notNull(run, "Runnable cannot be null");
		this.run = run;
		
		this.ol = onlyLauncheable;
		
		List<Quest> qus = QuestsAPI.getQuestsAssigneds(npc);
		quests = new ArrayList<>();
		
		if (ol){
			for (Quest qu : qus){
				if (qu.isInDialog(p)) {
					quests.clear();
					quests.add(qu);
					break;
				}
				if (qu.isLauncheable(p, true)) quests.add(qu);
			}
		}else this.quests.addAll(qus);
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
		finish = true;
		p.closeInventory();
		run.run(c);
	}

	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot > quests.size()) return;
		end(p, quests.get(slot));
	}

	public boolean onClose(Player p, Inventory inv){
		if (!finish){
			new BukkitRunnable() {
				public void run(){
					run.run(null);
				}
			}.runTaskLater(BeautyQuests.getInstance(), 1L);
		}
		return true;
	}

}