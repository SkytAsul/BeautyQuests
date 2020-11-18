package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import fr.skytasul.quests.options.OptionHologramLaunch;
import fr.skytasul.quests.options.OptionHologramLaunchNo;
import fr.skytasul.quests.options.OptionHologramText;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;

public class NPCStarter {

	private NPC npc;
	private Set<Quest> quests = new TreeSet<>();
	private Set<QuestPool> pools = new TreeSet<>();
	
	private BukkitTask launcheableTask;
	
	/* Holograms */
	private BukkitTask hologramsTask;
	private boolean hologramsRemoved = true;
	private Hologram hologramText = new Hologram(false, QuestsAPI.hasHologramsManager() && !QuestsConfiguration.isTextHologramDisabled(), Lang.HologramText.toString());
	private Hologram hologramLaunch = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems(), QuestsConfiguration.getHoloLaunchItem());
	private Hologram hologramLaunchNo = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), QuestsConfiguration.getHoloLaunchNoItem());
	private Hologram hologramPool = new Hologram(false, QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility(), Lang.PoolHologramText.toString());
	
	public NPCStarter(NPC npc){
		Validate.notNull(npc, "NPC cannot be null");
		this.npc = npc;
		
		launcheableTask = new BukkitRunnable() {
			private boolean holograms = hologramLaunch.enabled || hologramLaunchNo.enabled || hologramPool.enabled;
			private int timer = 0;
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
					if (p instanceof NPCHolder) continue;
					if (lc.distanceSquared(p.getLocation()) > QuestsConfiguration.getStartParticleDistanceSquared()) continue;
					playersInRadius.add(p);
					try{
						for (Quest quest : quests) {
							if (quest.isLauncheable(p, false)) {
								quest.launcheable.add(p);
								break;
							}
						}
					}catch (NullPointerException ex){continue;}
				}
				for (Quest quest : quests) quest.updateLauncheable(en);
				
				if (!holograms && !quests.isEmpty()/* || !QuestsAPI.getHologramsManager().supportPerPlayerVisibility()*/) return;
				Map<Player, PlayerAccount> players = playersInRadius.stream().collect(Collectors.toMap(x -> x, PlayersManager::getPlayerAccount));
				List<Player> launcheable = new ArrayList<>();
				List<Player> unlauncheable = new ArrayList<>();
				for (Quest qu : quests){
					for (Iterator<Entry<Player, PlayerAccount>> iterator = players.entrySet().iterator(); iterator.hasNext();) {
						Entry<Player, PlayerAccount> player = iterator.next();
						if (qu.hasFinished(player.getValue()) || qu.hasStarted(player.getValue())){
							continue;
						}else {
							boolean pLauncheable = qu.launcheable.contains(player.getKey());
							if (hologramLaunch.enabled && pLauncheable) {
								launcheable.add(player.getKey());
							}else if (hologramLaunchNo.enabled && !pLauncheable) {
								unlauncheable.add(player.getKey());
							}
						}
						iterator.remove();
					}
				}
				hologramLaunch.setVisible(launcheable);
				hologramLaunchNo.setVisible(unlauncheable);
				for (Player p : players.keySet()) {
					boolean visible = false;
					for (QuestPool pool : pools) {
						if (pool.canGive(p)) {
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
				if (hologramPool.canAppear) hologramPool.refresh(en);
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 1L);
	}
	
	public NPC getNPC() {
		return npc;
	}
	
	public Set<Quest> getQuests() {
		return quests;
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
		if (quests.isEmpty()) hologramText.visible = false;
		if (isEmpty()) delete();
		return b;
	}
	
	public Set<QuestPool> getPools() {
		return pools;
	}
	
	public void addPool(QuestPool pool) {
		if (!pools.add(pool)) return;
		if (hologramPool.enabled && (pool.getHologram() != null)) hologramText.setText(pool.getHologram());
	}
	
	public boolean removePool(QuestPool pool) {
		boolean b = pools.remove(pool);
		if (isEmpty()) delete();
		return b;
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
	
	public void delete() {
		for (Quest qu : quests) {
			BeautyQuests.logger.warning("Starter NPC has been removed from quest " + qu.getID());
			qu.removeOption(OptionStarterNPC.class);
		}
		quests = null;
		BeautyQuests.getInstance().getNPCs().remove(npc);
		launcheableTask.cancel();
		if (hologramsTask != null) hologramsTask.cancel();
		removeHolograms();
	}
	
	class Hologram{
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
			Location lc = Utils.upLocationForEntity(en, item == null ? 0 : 1);
			if (hologram == null){
				create(lc);
			}else hologram.teleport(lc);
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