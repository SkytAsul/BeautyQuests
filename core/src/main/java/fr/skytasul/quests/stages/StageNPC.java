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
	
	private NPC npc;
	private int npcID;
	protected Dialog di = null;
	protected boolean hide = false;
	
	protected StageBringBack bringBack = null;
	
	private BukkitTask task;
	
	private List<Player> cached = new ArrayList<>();
	protected Object holo;
	
	public StageNPC(QuestBranch branch) {
		super(branch);
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

	public void setNPC(int npcID) {
		this.npcID = npcID;
		this.npc = CitizensAPI.getNPCRegistry().getById(npcID);
		if (npc == null) BeautyQuests.logger.warning("The NPC " + npcID + " does not exist for " + debugName());
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
		if (!canUpdate(p)) return;
		
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
			di.send(p, npc, () -> {
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
		return (npc != null) ? npc.getName() : "§c§lunknown NPC " + npcID;
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		cached.add(p);
		if (QuestsConfiguration.handleGPS()) GPS.launchCompass(p, npc.getStoredLocation());
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		cached.remove(p);
		if (QuestsConfiguration.handleGPS()) GPS.stopCompass(p);
	}
	
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cached.add(p);
			if (QuestsConfiguration.handleGPS()) GPS.launchCompass(p, npc.getStoredLocation());
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
		if (task != null) task.cancel();
		if (holo != null) removeHoloLaunch();
		if (QuestsConfiguration.handleGPS()) cached.forEach(GPS::stopCompass);
	}
	
	public void load(){
		super.load();
		if (QuestsConfiguration.showTalkParticles() || QuestsConfiguration.getHoloTalkItem() != null){
			if (!hide) launchRefreshTask();
		}
	}

	protected void loadDatas(Map<String, Object> map) {
		setDialog(map.get("msg"));
		if (map.containsKey("npcID")) {
			setNPC((int) map.get("npcID"));
		}else BeautyQuests.logger.warning("No NPC specified for " + debugName());
		if (map.containsKey("hid")) hide = (boolean) map.get("hid");
	}
	
	public void serialize(Map<String, Object> map){
		map.put("npcID", npcID);
		if (di != null) map.put("msg", di.serialize());
		if (hide) map.put("hid", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageNPC st = new StageNPC(branch);
		st.loadDatas(map);
		return st;
	}

	public static class Creator implements StageCreationRunnables<StageNPC> {
		private static final ItemStack stageText = ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString());
		private static final ItemStack stageNPC = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString());

		public void start(Player p, LineData datas) {
			StagesGUI sg = datas.getGUI();
			Inventories.create(p, new SelectGUI(() -> {
				datas.getGUI().deleteStageLine(datas, p);
				datas.getGUI().reopen(p, true);
			}, npc -> {
				sg.reopen(p, true);
				npcDone(npc.getId(), datas);
			}));
		}

		public static void npcDone(int npcID, LineData datas) {
			datas.put("npcID", npcID);
			datas.getLine().setItem(7, ItemUtils.lore(stageNPC.clone(), Lang.optionValue.format(npcID)), (p, datass, item) -> {
				new SelectGUI(() -> datas.getGUI().reopen(p, true), npc -> {
					ItemUtils.lore(item, Lang.optionValue.format(npc.getId()));
					datas.put("npcID", npc.getId());
					datas.getGUI().reopen(p, true);
				}).create(p);
			});
			
			datas.getLine().setItem(6, stageText.clone(), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					Utils.sendMessage(p, Lang.NPC_TEXT.toString());
					Editor.enterOrLeave(p, new DialogEditor(p, (obj) -> {
						datas.getGUI().reopen(p, false);
						datas.put("npcText", obj);
					}, datas.containsKey("npcText") ? datas.get("npcText") : new Dialog()));
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
			if (datas.containsKey("hide")) stage.setHid(datas.get("hide"));
			if (datas.containsKey("npcID")) stage.setNPC(datas.get("npcID"));
		}

		public static void setEdit(StageNPC stage, LineData datas) {
			if (stage.getDialog() != null) datas.put("npcText", stage.getDialog().clone());
			if (stage.isHid()) datas.put("hide", true);
			npcDone(stage.npcID, datas);
		}

		public StageNPC finish(LineData datas, QuestBranch branch) {
			StageNPC stage = new StageNPC(branch);
			setFinish(stage, datas);
			return stage;
		}

		public void edit(LineData datas, StageNPC stage) {
			setEdit(stage, datas);
		}
	}

}
