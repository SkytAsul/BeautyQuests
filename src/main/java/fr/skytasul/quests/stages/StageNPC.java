package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.GPS;
import fr.skytasul.quests.utils.compatibility.HolographicDisplays;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class StageNPC extends AbstractStage{
	
	protected NPC npc;
	protected Dialog di = null;
	protected boolean hide = false;
	
	protected StageBringBack bringBack = null;
	
	protected Map<Player, Integer> dialogs = new HashMap<>();
	private BukkitTask task;
	
	private List<PlayerAccount> cached = new ArrayList<>();
	protected Object holo;
	
	public StageNPC(StageManager manager, NPC npc){
		super(manager);
		this.npc = npc;
	}
	
	private void launchRefreshTask(){
		if (npc == null) return;
		task = new BukkitRunnable() {
			List<Player> tmp = new ArrayList<>();
			public void run() {
				Entity en = npc.getEntity();
				if (en == null) return;
				if (!en.getType().isAlive()) return;
				Location lc = en.getLocation();
				tmp.clear();
				for (PlayerAccount acc : cached) {
					if (!acc.isCurrent()) continue;
					Player p = acc.getPlayer();
					if (p.getWorld() != lc.getWorld()) continue;
					if (lc.distance(p.getLocation()) > 50) continue;
					tmp.add(p);
				}
				
				if (QuestsConfiguration.getHoloTalkItem() != null && HolographicDisplays.hasProtocolLib()) {
					if (holo == null) createHoloLaunch();
					try {
						HolographicDisplays.setPlayersVisible(holo, tmp);
					}catch (ReflectiveOperationException e) {
						e.printStackTrace();
					}
					HolographicDisplays.teleport(holo, Utils.upLocationForEntity((LivingEntity) en, 1));
				}
				
				if (QuestsConfiguration.showTalkParticles()) {
					if (tmp.isEmpty()) return;
					QuestsConfiguration.getParticleTalk().send((LivingEntity) en, tmp);
				}
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 6L);
	}
	
	private void createHoloLaunch(){
		holo = HolographicDisplays.createHologram(npc.getStoredLocation(), false);
		HolographicDisplays.appendItem(holo, QuestsConfiguration.getHoloTalkItem());
	}
	
	private void removeHoloLaunch(){
		HolographicDisplays.delete(holo);
		holo = null;
	}

	public NPC getNPC(){
		return npc;
	}
	
	public void setDialog(Object obj){
		if (obj == null) return;
		if (obj instanceof Dialog){
			this.di = (Dialog) obj;
		}else {
			setDialog(Dialog.deserialize((Map<String, Object>) obj));
		}
	}
	
	public void setDialog(Dialog dialog){
		this.di = dialog;
	}
	
	public boolean hasDialog(){
		return di != null;
	}
	
	public Dialog getDialog(){
		return di;
	}

	public boolean isHid(){
		return hide;
	}

	public void setHid(boolean hide){
		this.hide = hide;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_NPC.toString(), npcName());
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{npcName()};
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onClick(NPCRightClickEvent e){
		Player p = e.getClicker();
		if (e.isCancelled()) return;
		if (e.getNPC() != npc) return;
		if (!hasStarted(p)) return;
		
		e.setCancelled(true);
		
		if (di != null){ // dialog exists
			if (dialogs.containsKey(p)){ // Already started
				int id = dialogs.get(p) + 1;
				// next dialog
				di.send(p, id);
				dialogs.replace(p, id);
				
				test(p, id);// end
				return;
				
				// dialog not started
			}else if (bringBack != null){ // if bringback has not required items
				if (!bringBack.checkItems(p, true)) return;
				bringBack.removeItems(p);
			}
			if (di.start(p)){
				dialogs.put(p, 0);
				test(p, 0);
				return;
			}
		}else if (bringBack != null){ // no dialog but bringback
			if (!bringBack.checkItems(p, true)){ // not required items
				return;
			}else { // required items present - so remove items
				bringBack.removeItems(p);
			}
		}
		finishStage(p);
	}
	
	private void test(Player p, int id){
		if (id+1 == di.messages.valuesSize()){ // end
			dialogs.remove(p);
			finishStage(p);
		}
	}
	
	protected String npcName(){
		return (npc != null) ? npc.getName() : "§c§lerror";
	}
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		if (!QuestsConfiguration.handleGPS()) return;
		if (!manager.hasStageLaunched(PlayersManager.getPlayerAccount(e.getPlayer()), this)) return;
		GPS.launchCompass(e.getPlayer(), npc);
	}
	
	public void start(PlayerAccount account){
		super.start(account);
		cached.add(account);
	}
	
	public void end(PlayerAccount account){
		super.end(account);
		cached.remove(account);
	}
	
	public void launch(Player p) {
		super.launch(p);
		if (manager.getID(this) == 0 && sendStartMessage() && bringBack != /*? tester*/ null) Utils.sendMessage(p, Lang.TALK_NPC.toString(), npc.getName());
		if (QuestsConfiguration.handleGPS()) GPS.launchCompass(p, npc);
	}
	
	public void finish(Player p){
		super.finish(p);
		if (QuestsConfiguration.handleGPS()) GPS.stopCompass(p);
	}
	
	public void unload() {
		super.unload();
		if (task !=null) task.cancel();
		if (holo != null) removeHoloLaunch();
	}
	
	public void load(){
		super.load();
		if (QuestsConfiguration.showTalkParticles() || QuestsConfiguration.getHoloTalkItem() != null){
			if (!hide) launchRefreshTask();
		}
	}
	
	protected void loadDatas(Map<String, Object> map) {
		setDialog(map.get("msg"));
		if (map.containsKey("hid")) hide = (boolean) map.get("hid");
	}

	
	public void serialize(Map<String, Object> map){
		if (npc != null) map.put("npcID", npc.getId());
		if (di != null) map.put("msg", di.serialize());
		if (hide) map.put("hid", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageNPC st = new StageNPC(manager, CitizensAPI.getNPCRegistry().getById((int) map.get("npcID")));
		st.loadDatas(map);
		return st;
	}

}
