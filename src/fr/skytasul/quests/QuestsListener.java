package fr.skytasul.quests;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.utils.compatibility.mobs.CompatMobDeathEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class QuestsListener implements Listener{
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCClick(NPCRightClickEvent e){
		if (e.isCancelled()) return;
		Player p = e.getClicker();
		
		if (Inventories.isInSystem(p)) return;
		
		NPC npc = e.getNPC();
		if (BeautyQuests.npcs.containsKey(npc)){
			e.setCancelled(true);
			ChooseQuestGUI ci = new ChooseQuestGUI(p, npc, (obj) -> {
				Inventories.remove(p);
				if (obj == null) return;
				((Quest) obj).launchStartDialog(p);
			}, true);
			if (ci.quests.size() >= 2){
				Inventories.create(p, ci);
			}else if (ci.quests.size() == 1){
				ci.open(p);
			}
		}
	}
	
	@EventHandler
	public void onNPCRemove(NPCRemoveEvent e){
		if (BeautyQuests.npcs.containsKey(e.getNPC())) BeautyQuests.npcs.get(e.getNPC()).delete();
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Inventories.onClose(e);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Inventories.onClick(e);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if (BeautyQuests.firstQuest != null && !e.getPlayer().hasPlayedBefore()) BeautyQuests.firstQuest.start(e.getPlayer());
		PlayersManager.getPlayerAccount(e.getPlayer());
		ScoreboardManager.onJoin(e);
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		if (QuestsConfiguration.getQuestItemLore().isEmpty()) return;
		ItemStack is = e.getItemDrop().getItemStack();
		if (is.getItemMeta().hasLore()){
			if (is.getItemMeta().getLore().contains(QuestsConfiguration.getQuestItemLore())) e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) { // firework damage
		if (e.getDamager().hasMetadata("questFinish")) e.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityKilled(EntityDeathEvent e){
		LivingEntity en = e.getEntity();
		if (en.getKiller() == null) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(en.getType(), en.getKiller(), en));
	}
	
	@EventHandler
	public void onNPCKilled(NPCDeathEvent e){
		LivingEntity en = (LivingEntity) e.getNPC().getEntity();
		if (en.getKiller() == null) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(e.getNPC(), en.getKiller(), en));
	}
	
}
