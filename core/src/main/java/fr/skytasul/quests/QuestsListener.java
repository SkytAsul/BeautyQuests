package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.BQBlockBreakEvent;
import fr.skytasul.quests.api.events.BQCraftEvent;
import fr.skytasul.quests.api.events.BQNPCClickEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.options.OptionAutoQuest;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Paper;

public class QuestsListener implements Listener{
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onNPCClick(BQNPCClickEvent e) {
		if (e.isCancelled()) return;
		if (!QuestsConfiguration.getNPCClicks().contains(e.getClick())) return;
		
		Player p = e.getPlayer();
		BQNPC npc = e.getNPC();
		
		if (Inventories.isInSystem(p)) return;
		
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (acc == null) return;
		
		Set<Quest> quests = npc.getQuests();
		quests = quests.stream().filter(qu -> !qu.hasStarted(acc) && (qu.isRepeatable() ? true : !qu.hasFinished(acc))).collect(Collectors.toSet());
		if (quests.isEmpty() && npc.getPools().isEmpty()) return;
		
		List<Quest> launcheable = new ArrayList<>();
		List<Quest> requirements = new ArrayList<>();
		List<Quest> timer = new ArrayList<>();
		for (Quest qu : quests) {
			try {
				if (!qu.testRequirements(p, acc, false)) {
					requirements.add(qu);
				}else if (!qu.testTimer(acc, false)) {
					timer.add(qu);
				}else launcheable.add(qu);
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An exception occured when checking requirements on the quest " + qu.getID() + " for player " + p.getName(), ex);
			}
		}
		
		Set<QuestPool> startablePools = npc.getPools().stream().filter(pool -> {
			try {
				return pool.canGive(p, acc);
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An exception occured when checking requirements on the pool " + pool.getID() + " for player " + p.getName(), ex);
				return false;
			}
		}).collect(Collectors.toSet());
		
		e.setCancelled(true);
		if (!launcheable.isEmpty()) {
			for (Quest quest : launcheable) {
				if (quest.isInDialog(p)) {
					quest.clickNPC(p);
					return;
				}
			}
			ChooseQuestGUI gui = new ChooseQuestGUI(launcheable, (quest) -> {
				if (quest == null) return;
				quest.clickNPC(p);
			});
			gui.setValidate(__ -> {
				new PlayerListGUI(acc).create(p);
			}, ItemUtils.item(XMaterial.BOOKSHELF, Lang.questMenu.toString(), QuestOption.formatDescription(Lang.questMenuLore.toString())));
			gui.create(p);
		}else if (!startablePools.isEmpty()) {
			QuestPool pool = startablePools.iterator().next();
			DebugUtils.logMessage("NPC " + npc.getId() + ": " + startablePools.size() + " pools, result: " + pool.give(p));
		}else {
			if (!timer.isEmpty()) {
				timer.get(0).testTimer(acc, true);
			}else if (!requirements.isEmpty()) {
				requirements.get(0).testRequirements(p, acc, true);
			}else {
				Utils.sendMessage(p, npc.getPools().iterator().next().give(p));
			}
			e.setCancelled(false);
		}
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
	public void onDrag(InventoryDragEvent e) {
		Inventories.onDrag(e);
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onOpen(InventoryOpenEvent e) {
		Inventories.onOpen(e);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		Player player = e.getPlayer();
		DebugUtils.logMessage(player.getName() + " joined the server"); // for timing purpose
		if (BeautyQuests.loaded && !QuestsConfiguration.hookAccounts()) {
			PlayersManager.loadPlayer(player);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		DebugUtils.logMessage(player.getName() + " left the server"); // for timing purpose
		if (!QuestsConfiguration.hookAccounts()) {
			PlayersManager.unloadPlayer(player);
		}
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onAccountJoin(PlayerAccountJoinEvent e) {
		if (e.isFirstJoin()) {
			QuestsAPI.getQuests().getQuests().stream().filter(qu -> qu.getOptionValueOrDef(OptionAutoQuest.class)).forEach(qu -> qu.start(e.getPlayer()));
		}
	}
	
	@EventHandler
	public void onAccountLeave(PlayerAccountLeaveEvent e) {
		QuestsAPI.getQuests().forEach(x -> x.leave(e.getPlayer()));
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
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onEat(PlayerItemConsumeEvent e) {
		if (Utils.isQuestItem(e.getItem())) {
			e.setCancelled(true);
			Lang.QUEST_ITEM_EAT.send(e.getPlayer());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onDeath(PlayerDeathEvent e) {
		if (BeautyQuests.getInstance().isRunningPaper()) Paper.handleDeathItems(e, Utils::isQuestItem);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		if (e.isCancelled()) return;
		if (e.getPlayer() == null) return;
		Bukkit.getPluginManager().callEvent(new BQBlockBreakEvent(e.getPlayer(), Arrays.asList(e.getBlock())));
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftMonitor(CraftItemEvent e){
		if (e.getInventory().getResult() == null) return;

		int resultCount = e.getInventory().getResult().getAmount();
		int materialCount = Integer.MAX_VALUE;

		for (ItemStack is : e.getInventory().getMatrix())
			if (is != null && is.getAmount() < materialCount)
				materialCount = is.getAmount();

		int maxCraftAmount = resultCount * materialCount;
		
		ItemStack item = e.getRecipe().getResult();
		if (item.getType() == Material.AIR && e.getRecipe() instanceof ComplexRecipe) {
			String key = ((ComplexRecipe) e.getRecipe()).getKey().toString();
			if (key.equals("minecraft:suspicious_stew")) {
				item = XMaterial.SUSPICIOUS_STEW.parseItem();
			}
		}
		
		Bukkit.getPluginManager().callEvent(new BQCraftEvent(e, item, maxCraftAmount));
	}
	
}
