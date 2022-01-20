package fr.skytasul.quests.structure;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.options.OptionHologramLaunch;
import fr.skytasul.quests.options.OptionHologramLaunchNo;
import fr.skytasul.quests.options.OptionHologramText;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class NPCStarter {

	private BQNPC npc;
	private Set<Quest> quests = new TreeSet<>();
	private Set<QuestPool> pools = new TreeSet<>();
	
	private BukkitTask launcheableTask;
	
	/* Holograms */
	private BukkitTask hologramsTask;
	private boolean hologramsRemoved = true;
	private Hologram hologramText = new Hologram(false, QuestsAPI.hasHologramsManager() && !QuestsConfiguration.isTextHologramDisabled(), Lang.HologramText.toString());
	private Hologram hologramLaunch = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems(), QuestsConfiguration.getHoloLaunchItem());
	private Hologram hologramLaunchNo = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), QuestsConfiguration.getHoloLaunchNoItem());
	private Hologram hologramPool = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), Lang.PoolHologramText.toString()) {
		@Override
		public double getYAdd() {
			return hologramText.visible ? 0.3 : 0;
		}
	};
	
	private List<Entry<Player, Object>> hiddenTickets = new ArrayList<>();
	
	public NPCStarter(BQNPC npc) {
		Validate.notNull(npc, "NPC cannot be null");
		this.npc = npc;
		
		launcheableTask = new BukkitRunnable() {
			private boolean holograms = hologramLaunch.enabled || hologramLaunchNo.enabled || hologramPool.enabled;
			private int timer = 0;
			@Override
			public void run() {
				if (!npc.isSpawned()) return;

				if (timer-- == 0) {
					timer = QuestsConfiguration.getRequirementUpdateTime();
					return;
				}

				LivingEntity en;
				try {
					en = (LivingEntity) npc.getEntity();
				}catch (ClassCastException ex) {return;}
				
				for (Quest quest : quests) quest.launcheable.clear();
				
				Set<Player> playersInRadius = new HashSet<>();
				Location lc = en.getLocation();
				for (Player p : lc.getWorld().getPlayers()) {
					PlayerAccount acc = PlayersManager.getPlayerAccount(p);
					if (acc == null) continue;
					if (lc.distanceSquared(p.getLocation()) > QuestsConfiguration.getStartParticleDistanceSquared()) continue;
					playersInRadius.add(p);
					try{
						for (Quest quest : quests) {
							if (quest.isLauncheable(p, acc, false)) {
								quest.launcheable.add(p);
								break;
							}
						}
					}catch (NullPointerException ex) {}
				}
				for (Quest quest : quests) quest.updateLauncheable(en);
				
				if (!holograms && !quests.isEmpty()) return;
				List<Player> launcheable = new ArrayList<>();
				List<Player> unlauncheable = new ArrayList<>();
				for (Iterator<Player> iterator = playersInRadius.iterator(); iterator.hasNext();) {
					Player player = iterator.next();
					if (hiddenTickets.stream().anyMatch(entry -> entry.getKey() == player)) {
						iterator.remove();
						continue;
					}
					PlayerAccount acc = PlayersManager.getPlayerAccount(player);
					boolean launchYes = false;
					boolean launchNo = false;
					for (Quest qu : quests) {
						if (!qu.hasStarted(acc)) {
							boolean pLauncheable = qu.launcheable.contains(player);
							if (hologramLaunch.enabled && pLauncheable) {
								launchYes = true;
								break; // launcheable take priority over not launcheable
							}else if (hologramLaunchNo.enabled && !pLauncheable) {
								launchNo = true;
							}
						}
					}
					if (launchYes) {
						launcheable.add(player);
						iterator.remove();
					}else if (launchNo) {
						unlauncheable.add(player);
						iterator.remove();
					}
				}
				hologramLaunch.setVisible(launcheable);
				hologramLaunchNo.setVisible(unlauncheable);
				for (Player p : playersInRadius) {
					boolean visible = false;
					for (QuestPool pool : pools) {
						if (pool.canGive(p, PlayersManager.getPlayerAccount(p))) {
							visible = true;
							break;
						}
					}
					hologramPool.setVisible(p, visible);
				}
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 20L);
		
		
		if (!hologramText.enabled && !hologramLaunch.enabled && !hologramLaunchNo.enabled && !hologramPool.enabled) return; // no hologram: no need to launch the update task
		hologramsTask = new BukkitRunnable() {
			@Override
			public void run(){
				LivingEntity en = null; // check if NPC is spawned and living
				if (npc.isSpawned()){
					try {
						en = (LivingEntity) npc.getEntity();
					}catch (ClassCastException ex) {}
				}
				if (en == null){
					if (!hologramsRemoved) removeHolograms(); // if the NPC is not living and holograms have not been already removed before
					return;
				}
				hologramsRemoved = false;
				
				if (hologramText.canAppear && hologramText.visible) hologramText.refresh(en);
				if (hologramLaunch.canAppear) hologramLaunch.refresh(en);
				if (hologramLaunchNo.canAppear) hologramLaunchNo.refresh(en);
				if (hologramPool.canAppear && hologramPool.visible) hologramPool.refresh(en);
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 1L);
	}
	
	public BQNPC getNPC() {
		return npc;
	}
	
	public Set<Quest> getQuests() {
		return quests;
	}
	
	public Hologram getHologramText(){
		return hologramText;
	}
	
	public Hologram getHologramLaunch(){
		return hologramLaunch;
	}
	
	public Hologram getHologramLaunchNo(){
		return hologramLaunchNo;
	}
	
	public void addQuest(Quest quest) {
		if (!quests.add(quest)) return;
		if (hologramText.enabled && quest.hasOption(OptionHologramText.class)) hologramText.setText(quest.getOption(OptionHologramText.class).getValue());
		if (hologramLaunch.enabled && quest.hasOption(OptionHologramLaunch.class)) hologramLaunch.setItem(quest.getOption(OptionHologramLaunch.class).getValue());
		if (hologramLaunchNo.enabled && quest.hasOption(OptionHologramLaunchNo.class)) hologramLaunchNo.setItem(quest.getOption(OptionHologramLaunchNo.class).getValue());
		hologramText.visible = true;
	}
	
	public boolean removeQuest(Quest quest) {
		boolean b = quests.remove(quest);
		if (isEmpty()) {
			delete("Quest remove");
		}else if (quests.isEmpty()) {
			hologramText.visible = false;
			hologramText.delete();
		}
		return b;
	}
	
	public Set<QuestPool> getPools() {
		return pools;
	}
	
	public void addPool(QuestPool pool) {
		if (!pools.add(pool)) return;
		if (hologramPool.enabled && (pool.getHologram() != null)) hologramPool.setText(pool.getHologram());
		hologramPool.visible = true;
	}
	
	public boolean removePool(QuestPool pool) {
		boolean b = pools.remove(pool);
		if (isEmpty()) {
			delete("Pool remove");
		}else if (pools.isEmpty()) {
			hologramPool.visible = false;
			hologramPool.delete();
		}
		return b;
	}
	
	public void hideForPlayer(Player p, Object holder) {
		hiddenTickets.add(new AbstractMap.SimpleEntry<>(p, holder));
	}
	
	public void removeHiddenForPlayer(Player p, Object holder) {
		for (Iterator<Entry<Player, Object>> iterator = hiddenTickets.iterator(); iterator.hasNext();) {
			Entry<Player, Object> entry = iterator.next();
			if (entry.getKey() == p && entry.getValue() == holder) {
				iterator.remove();
				return;
			}
		}
	}
	
	public void removeHolograms(){
		hologramText.delete();
		hologramLaunch.delete();
		hologramLaunchNo.delete();
		hologramPool.delete();
		hologramsRemoved = true;
	}
	
	public boolean isEmpty() {
		return quests.isEmpty() && pools.isEmpty();
	}
	
	public void delete(String cause) {
		DebugUtils.logMessage("Removing NPC Starter " + npc.getId());
		for (Quest qu : quests) {
			BeautyQuests.logger.warning("Starter NPC #" + npc.getId() + " has been removed from quest " + qu.getID() + ". Reason: " + cause);
			qu.removeOption(OptionStarterNPC.class);
		}
		quests = null;
		for (QuestPool pool : pools) {
			BeautyQuests.logger.warning("NPC #" + npc.getId() + " has been removed from pool " + pool.getID() + ". Reason: " + cause);
			pool.unloadStarter();
		}
		BeautyQuests.getInstance().getNPCs().remove(npc);
		launcheableTask.cancel();
		if (hologramsTask != null) hologramsTask.cancel();
		removeHolograms();
	}
	
	public class Hologram {
		boolean visible;
		boolean enabled;
		boolean canAppear;
		AbstractHolograms<?>.BQHologram hologram;
		
		String text;
		ItemStack item;
		
		public Hologram(boolean visible, boolean enabled, String text){
			this.visible = visible;
			this.enabled = enabled;
			setText(text);
		}
		
		public Hologram(boolean visible, boolean enabled, ItemStack item){
			this.visible = visible;
			this.enabled = enabled;
			setItem(item);
		}
		
		public void refresh(LivingEntity en){
			Location lc = Utils.upLocationForEntity(en, getYAdd());
			if (hologram == null){
				create(lc);
			}else hologram.teleport(lc);
		}
		
		public double getYAdd() {
			return item == null ? 0 : 1;
		}
		
		public void setVisible(List<Player> players){
			if (hologram != null) hologram.setPlayersVisible(players);
		}
		
		public void setVisible(Player p, boolean visibility) {
			if (hologram != null) hologram.setPlayerVisibility(p, visibility);
		}
		
		public void setText(String text){
			this.text = text;
			canAppear = enabled && !StringUtils.isEmpty(text) && !"none".equals(text);
			if (!canAppear) delete();
		}
		
		public void setItem(ItemStack item) {
			this.item = item;
			canAppear = enabled && item != null;
			if (canAppear && QuestsConfiguration.isCustomHologramNameShown() && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) this.text = item.getItemMeta().getDisplayName();
			if (!canAppear) delete();
		}
		
		public void create(Location lc){
			if (hologram != null) return;
			hologram = QuestsAPI.getHologramsManager().createHologram(lc, visible);
			if (text != null) hologram.appendTextLine(text);
			if (item != null) hologram.appendItem(item);
		}
		
		public void delete(){
			if (hologram == null) return;
			hologram.delete();
			hologram = null;
		}
	}
	
}
