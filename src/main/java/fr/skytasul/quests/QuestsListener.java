package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
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
		if (BeautyQuests.getInstance().getNPCs().containsKey(npc)){
			PlayerAccount acc = PlayersManager.getPlayerAccount(p);
			
			List<Quest> quests = QuestsAPI.getQuestsAssigneds(npc);
			quests = quests.stream().filter(qu -> !qu.getStageManager().contains(acc) && (qu.isRepeatable() ? true : !qu.hasFinished(acc))).collect(Collectors.toList());
			if (quests.isEmpty()) return;
			
			List<Quest> launcheable = new ArrayList<>();
			List<Quest> requirements = new ArrayList<>();
			List<Quest> timer = new ArrayList<>();
			for (Quest qu : quests){
				if (!qu.testRequirements(p, acc, false)){
					requirements.add(qu);
				}else if (!qu.testTimer(p, acc, false)){
					timer.add(qu);
				}else launcheable.add(qu);
			}
			
			e.setCancelled(true);
			if (launcheable.isEmpty()){
				if (requirements.isEmpty()){
					timer.get(0).testTimer(p, acc, true);
				}else {
					requirements.get(0).testRequirements(p, acc, true);
				}
				e.setCancelled(false);
			}else {
				ChooseQuestGUI ci = new ChooseQuestGUI(launcheable, (obj) -> {
					if (obj == null) return;
					((Quest) obj).clickNPC(p);
				});
				Inventories.create(p, ci);
			}
		}
	}
	
	@EventHandler
	public void onNPCRemove(NPCRemoveEvent e){
		if (BeautyQuests.getInstance().getNPCs().containsKey(e.getNPC())) BeautyQuests.getInstance().getNPCs().get(e.getNPC()).delete();
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
		if (QuestsConfiguration.firstQuest != null && !e.getPlayer().hasPlayedBefore()) QuestsConfiguration.firstQuest.start(e.getPlayer());
		PlayersManager.getPlayerAccount(e.getPlayer());
		BeautyQuests.getInstance().getScoreboardManager().create(e.getPlayer());
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		String lore = Lang.QuestItemLore.toString();
		if (lore.isEmpty()) return;
		ItemStack is = e.getItemDrop().getItemStack();
		if (is.getItemMeta().hasLore()){
			if (is.getItemMeta().getLore().contains(lore)) e.setCancelled(true);
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
