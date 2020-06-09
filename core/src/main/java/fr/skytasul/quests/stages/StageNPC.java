package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerAccountJoinEvent;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
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
	
	private BukkitTask task;
	
	private List<Player> cached = new ArrayList<>();
	protected Object holo;
	
	public StageNPC(QuestBranch branch, NPC npc){
		super(branch);
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
				for (Player p : cached) {
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
		
		if (!branch.isRegularStage(this)) { // is ending stage
			if (bringBack == null || !bringBack.checkItems(p, false)) { // if just text or don't have items
				for (AbstractStage stage : branch.getEndingStages().keySet()) {
					if (stage instanceof StageBringBack) { // if other ending stage is bring back
						StageBringBack other = (StageBringBack) stage;
						if (other.getNPC() == npc && other.checkItems(p, false)) return; // if same NPC and can start: don't cancel event and stop there
					}
				}
			}
		}

		e.setCancelled(true);

		if (bringBack != null && !bringBack.checkItems(p, true)) return;
		if (di != null){ // dialog exists
			di.send(p, () -> {
				if (bringBack != null) {
					if (!bringBack.checkItems(p, true)) return;
					bringBack.removeItems(p);
				}
				finishStage(p);
			});
			return;
		}else if (bringBack != null){ // no dialog but bringback
			bringBack.removeItems(p);
		}
		finishStage(p);
	}
	
	protected String npcName(){
		return (npc != null) ? npc.getName() : "§c§lerror";
	}
	
	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (branch.hasStageLaunched(e.getPlayerAccount(), this)) {
			cached.add(e.getPlayer());
			if (QuestsConfiguration.handleGPS()) GPS.launchCompass(e.getPlayer(), npc);
		}else cached.remove(e.getPlayer());
	}
	
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cached.add(p);
			if (QuestsConfiguration.handleGPS()) GPS.launchCompass(p, npc);
		}
	}
	
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cached.remove(p);
			if (QuestsConfiguration.handleGPS()) GPS.stopCompass(p);
		}
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
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageNPC st = new StageNPC(branch, CitizensAPI.getNPCRegistry().getById((int) map.get("npcID")));
		st.loadDatas(map);
		return st;
	}

	public static class Creator implements StageCreationRunnables<StageNPC> {
		private static final ItemStack stageText = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString());

		public void start(Player p, LineData datas) {
			StagesGUI sg = datas.getGUI();
			Inventories.create(p, new SelectGUI((npc) -> {
				sg.reopen(p, true);
				npcDone(npc, datas);
			}));
		}

		public static void npcDone(NPC npc, LineData datas) {
			datas.put("npc", npc);
			datas.getLine().setItem(6, stageText.clone(), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					Utils.sendMessage(p, Lang.NPC_TEXT.toString());
					Editor.enterOrLeave(p, new DialogEditor(p, (obj) -> {
						datas.getGUI().reopen(p, false);
						datas.put("npcText", obj);
					}, datas.containsKey("npcText") ? (Dialog) datas.get("npcText") : new Dialog((NPC) datas.get("npc"))));
				}
			}, true, true);

			datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.stageHide.toString(), datas.containsKey("hide") ? (boolean) datas.get("hide") : false), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					datas.put("hide", ItemUtils.toggle(item));
				}
			}, true, true);
		}

		public static void setFinish(StageNPC stage, LineData datas) {
			if (datas.containsKey("npcText")) stage.setDialog(datas.get("npcText"));
			if (datas.containsKey("hide")) stage.setHid((boolean) datas.get("hide"));
		}

		public static void setEdit(StageNPC stage, LineData datas) {
			if (stage.getDialog() != null) datas.put("npcText", new Dialog(stage.getDialog().getNPC(), stage.getDialog().messages.clone()));
			if (stage.isHid()) datas.put("hide", true);
			npcDone(stage.getNPC(), datas);
		}

		public StageNPC finish(LineData datas, QuestBranch branch) {
			StageNPC stage = new StageNPC(branch, (NPC) datas.get("npc"));
			setFinish(stage, datas);
			return stage;
		}

		public void edit(LineData datas, StageNPC stage) {
			setEdit(stage, datas);
		}
	}

}
