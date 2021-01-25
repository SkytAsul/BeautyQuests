package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.events.PlayerAccountJoinEvent;
import fr.skytasul.quests.players.events.PlayerAccountLeaveEvent;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class QuestsListener implements Listener{
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCClick(NPCLeftClickEvent e){
		if (e.isCancelled()) return;
		Player p = e.getClicker();
		
		if (Inventories.isInSystem(p)) return;
		
		NPC npc = e.getNPC();
		if (BeautyQuests.getInstance().getNPCs().containsKey(npc)){
			PlayerAccount acc = PlayersManager.getPlayerAccount(p);
			if (acc == null) return;
			
			List<Quest> quests = QuestsAPI.getQuestsAssigneds(npc);
			quests = quests.stream().filter(qu -> !qu.hasStarted(acc) && (qu.isRepeatable() ? true : !qu.hasFinished(acc))).collect(Collectors.toList());
			if (quests.isEmpty()) return;
			
			List<Quest> launcheable = new ArrayList<>();
			List<Quest> requirements = new ArrayList<>();
			List<Quest> timer = new ArrayList<>();
			for (Quest qu : quests){
				if (!qu.testRequirements(p, acc, false)){
					requirements.add(qu);
				}else if (!qu.testTimer(acc, false)) {
					timer.add(qu);
				}else launcheable.add(qu);
			}
			
			e.setCancelled(true);
			if (launcheable.isEmpty()){
				if (requirements.isEmpty()){
					timer.get(0).testTimer(acc, true);
				}else {
					requirements.get(0).testRequirements(p, acc, true);
				}
				e.setCancelled(false);
			}else {
				for (Quest quest : launcheable){
					if (quest.isInDialog(p)){
						quest.clickNPC(p);
						return;
					}
				}
				new ChooseQuestGUI(launcheable, (quest) -> {
					if (quest == null) return;
					quest.clickNPC(p);
				}).create(p);
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
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		Player player = e.getPlayer();
		if (!QuestsConfiguration.hookAccounts()) {
			PlayersManager.loadPlayer(player);
			/*Entry<PlayerAccount, Boolean> acc = PlayersManager.manager.load(player);
			//boolean firstJoin = !PlayersManager.manager.hasAccounts(player);
			Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
				Bukkit.getPluginManager().callEvent(new PlayerAccountJoinEvent(player, acc.getKey(), acc.getValue()));
			}, 2L);*/
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (!QuestsConfiguration.hookAccounts()) {
			PlayersManager.unloadPlayer(e.getPlayer());
		}
	}

	@EventHandler
	public void onAccountJoin(PlayerAccountJoinEvent e) {
		if (QuestsConfiguration.firstQuest != null && e.isFirstJoin()) {
			QuestsConfiguration.firstQuest.start(e.getPlayer());
		}
		BeautyQuests.getInstance().getScoreboardManager().create(e.getPlayer());
	}
	
	@EventHandler
	public void onAccountLeave(PlayerAccountLeaveEvent e) {
		BeautyQuests.getInstance().getScoreboardManager().removePlayerScoreboard(e.getPlayer());
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent e){
		if (Utils.isQuestItem(e.getItemDrop().getItemStack())) {
			e.setCancelled(true);
			Lang.QUEST_ITEM_DROP.send(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) { // firework damage
		if (e.getDamager().hasMetadata("questFinish")) e.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onCraft(CraftItemEvent e) {
		for (ItemStack item : e.getInventory().getMatrix()) {
			if (Utils.isQuestItem(item)) {
				e.setCancelled(true);
				Lang.QUEST_ITEM_CRAFT.send(e.getWhoClicked());
				break;
			}
		}
	}
	
}
