package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;

public class NPCStarter {

	private NPC npc;
	private List<Quest> quests = new ArrayList<>();
	
	private BukkitTask refreshTask;
	
	NPCStarter(NPC npc){
		Validate.notNull(npc, "NPC cannot be null");
		this.npc = npc;
		
		refreshTask = new BukkitRunnable() {
			public void run() {
				if (!npc.isSpawned()) return;
				LivingEntity en;
				try {
					en = (LivingEntity) npc.getEntity();
				}catch (ClassCastException ex) {return;}
				
				for (Quest quest : quests) quest.launcheable.clear();
				
				Location lc = en.getLocation();
				for (Player p : en.getWorld().getPlayers()){
					if (p instanceof NPCHolder) continue;
					if (lc.distance(p.getLocation()) > 50) continue;
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
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 20L);
	}
	
	public NPC getNPC() {
		return npc;
	}
	
	public List<Quest> getQuests(){
		return quests;
	}
	
	public boolean addQuest(Quest quest) {
		if (!quests.contains(quest)) return quests.add(quest);
		return false;
	}
	
	public boolean removeQuest(Quest quest) {
		return quests.remove(quest);
	}
	
	public void delete() {
		for (Iterator<Quest> iterator = quests.iterator(); iterator.hasNext();) {
			iterator.next().remove(true);
		}
		quests = null;
		BeautyQuests.getInstance().getNPCs().remove(npc);
		refreshTask.cancel();
	}
	
}