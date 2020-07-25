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
import fr.skytasul.quests.options.OptionHologramLaunch;
import fr.skytasul.quests.options.OptionHologramLaunchNo;
import fr.skytasul.quests.options.OptionHologramText;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.HolographicDisplays;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;

public class NPCStarter {

	private NPC npc;
	private Set<Quest> quests = new TreeSet<>();
	
	private BukkitTask launcheableTask;
	
	/* Holograms */
	private BukkitTask hologramsTask;
	private boolean hologramsRemoved = true;
	private Hologram hologramText = new Hologram(true, DependenciesManager.holod && !QuestsConfiguration.isTextHologramDisabled(), Lang.HologramText.toString());
	private Hologram hologramLaunch = new Hologram(false, DependenciesManager.holod, QuestsConfiguration.getHoloLaunchItem());
	private Hologram hologramLaunchNo = new Hologram(false, DependenciesManager.holod && HolographicDisplays.hasProtocolLib(), QuestsConfiguration.getHoloLaunchNoItem());
	
	public NPCStarter(NPC npc){
		Validate.notNull(npc, "NPC cannot be null");
		this.npc = npc;
		
		launcheableTask = new BukkitRunnable() {
			private boolean holograms = hologramLaunch.enabled || hologramLaunchNo.enabled;
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
				for (Player p : en.getWorld().getPlayers()){
					if (p instanceof NPCHolder) continue;
					if (lc.distance(p.getLocation()) > QuestsConfiguration.getStartParticleDistance()) continue;
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
				
				if (!holograms || !HolographicDisplays.hasProtocolLib()) return;
				Map<Player, PlayerAccount> players = playersInRadius.stream().collect(Collectors.toMap(x -> x, x -> PlayersManager.getPlayerAccount(x)));
				List<Player> launcheable = new ArrayList<>();
				List<Player> unlauncheable = new ArrayList<>();
				for (Quest qu : quests){
					for (Iterator<Entry<Player, PlayerAccount>> iterator = players.entrySet().iterator(); iterator.hasNext();) {
						Entry<Player, PlayerAccount> player = iterator.next();
						if (qu.hasFinished(player.getValue()) || qu.hasStarted(player.getValue())){
							continue;
						}else if (hologramLaunch.enabled && qu.launcheable.contains(player.getKey())) {
							launcheable.add(player.getKey());
						}else if (hologramLaunchNo.enabled && !qu.launcheable.contains(player.getKey())) {
							unlauncheable.add(player.getKey());
						}
						iterator.remove();
					}
				}
				hologramLaunch.setVisible(launcheable);
				hologramLaunchNo.setVisible(unlauncheable);
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 20L);
		
		
		if (!hologramText.enabled && !hologramLaunch.enabled && !hologramLaunchNo.enabled) return; // no hologram: no need to launch the update task
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
				
				if (hologramText.enabled) hologramText.refresh(en);
				if (hologramLaunch.enabled) hologramLaunch.refresh(en);
				if (hologramLaunchNo.enabled) hologramLaunchNo.refresh(en);
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
		if (quests.contains(quest)) return;
		quests.add(quest);
		if (hologramText.enabled && quest.hasOption(OptionHologramText.class)) hologramText.setText(quest.getOption(OptionHologramText.class).getValue());
		if (hologramLaunch.enabled && quest.hasOption(OptionHologramLaunch.class)) hologramLaunch.item = quest.getOption(OptionHologramLaunch.class).getValue();
		if (hologramLaunchNo.enabled && quest.hasOption(OptionHologramLaunchNo.class)) hologramLaunchNo.item = quest.getOption(OptionHologramLaunchNo.class).getValue();
	}
	
	public boolean removeQuest(Quest quest) {
		boolean b = quests.remove(quest);
		if (quests.isEmpty()) delete();
		return b;
	}
	
	public void removeHolograms(){
		hologramText.delete();
		hologramLaunch.delete();
		hologramLaunchNo.delete();
		hologramsRemoved = true;
	}
	
	public void delete() {
		for (Quest qu : new ArrayList<>(quests)) {
			qu.remove(true);
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
		Object hologram;
		
		String text;
		ItemStack item;
		
		public Hologram(boolean visible, boolean enabled, String text){
			this.visible = visible;
			this.enabled = enabled && !StringUtils.isEmpty(text) && !"none".equals(text);
			this.text = text;
		}
		
		public Hologram(boolean visible, boolean enabled, ItemStack item){
			this.visible = visible;
			this.enabled = enabled && item != null;
			this.item = item;
			if (this.enabled && QuestsConfiguration.isCustomHologramNameShown() && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) this.text = item.getItemMeta().getDisplayName();
		}
		
		public void refresh(LivingEntity en){
			Location lc = Utils.upLocationForEntity(en, item == null ? 0 : 1);
			if (hologram == null){
				create(lc);
			}else HolographicDisplays.teleport(hologram, lc);
		}
		
		public void setVisible(List<Player> players){
			try {
				HolographicDisplays.setPlayersVisible(hologram, players);
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		
		public void setText(String text){
			this.text = text;
			if (item != null) return; // no need to test if none
			enabled = enabled && !StringUtils.isEmpty(text) && !"none".equals(text);
			if (!enabled) delete();
		}
		
		public void create(Location lc){
			if (hologram != null) return;
			hologram = HolographicDisplays.createHologram(lc, visible);
			if (text != null) HolographicDisplays.appendTextLine(hologram, text);
			if (item != null) HolographicDisplays.appendItem(hologram, item);
		}
		
		public void delete(){
			if (hologram == null) return;
			HolographicDisplays.delete(hologram);
			hologram = null;
		}
	}
	
}