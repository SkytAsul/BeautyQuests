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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.GPS;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class StageNPC extends AbstractStage{
	
	private NPC npc;
	private int npcID;
	protected Dialog dialog = null;
	protected boolean hide = false;
	
	protected StageBringBack bringBack = null;
	
	private BukkitTask task;
	
	private List<Player> cached = new ArrayList<>();
	protected AbstractHolograms<?>.BQHologram hologram;
	
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
				
				if (QuestsConfiguration.getHoloTalkItem() != null && QuestsAPI.getHologramsManager().supportItems() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility()) {
					if (hologram == null) createHoloLaunch();
					hologram.setPlayersVisible(tmp);
					hologram.teleport(Utils.upLocationForEntity((LivingEntity) en, 1));
				}
				
				if (QuestsConfiguration.showTalkParticles()) {
					if (tmp.isEmpty()) return;
					QuestsConfiguration.getParticleTalk().send((LivingEntity) en, tmp);
				}
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 6L);
	}
	
	private void createHoloLaunch(){
		hologram = QuestsAPI.getHologramsManager().createHologram(npc.getStoredLocation(), false);
		hologram.appendItem(QuestsConfiguration.getHoloTalkItem());
	}
	
	private void removeHoloLaunch(){
		if (hologram == null) return;
		hologram.delete();
		hologram = null;
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
			this.dialog = (Dialog) obj;
		}else {
			setDialog(Dialog.deserialize((Map<String, Object>) obj));
		}
	}
	
	public void setDialog(Dialog dialog){
		this.dialog = dialog;
	}
	
	public boolean hasDialog(){
		return dialog != null;
	}
	
	public Dialog getDialog(){
		return dialog;
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
		if (dialog != null) { // dialog exists
			dialog.send(p, npc, () -> {
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
		if (hologram != null) removeHoloLaunch();
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
		if (dialog != null) map.put("msg", dialog.serialize());
		if (hide) map.put("hid", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageNPC st = new StageNPC(branch);
		st.loadDatas(map);
		return st;
	}

	public static class Creator extends StageCreation<StageNPC> {
		
		private int npcID = -1;
		private Dialog dialog = null;
		private boolean hidden = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString()), (p, item) -> {
				new SelectGUI(() -> reopenGUI(p, true), npc -> {
					setNPCId(npc.getId());
					reopenGUI(p, true);
				}).create(p);
			});
			
			line.setItem(6, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString()), (p, item) -> {
				Utils.sendMessage(p, Lang.NPC_TEXT.toString());
				new DialogEditor(p, (obj) -> {
					setDialog(dialog);
					reopenGUI(p, false);
				}, dialog == null ? dialog = new Dialog() : dialog).enterOrLeave(p);
			}, true, true);
			
			line.setItem(5, ItemUtils.itemSwitch(Lang.stageHide.toString(), false), (p, item) -> setHidden(ItemUtils.toggle(item)), true, true);
		}
		
		public void setNPCId(int npcID) {
			this.npcID = npcID;
			line.editItem(7, ItemUtils.lore(line.getItem(7), QuestOption.formatDescription("ID: §l" + npcID)));
		}
		
		public void setDialog(Dialog dialog) {
			this.dialog = dialog;
		}
		
		public void setHidden(boolean hidden) {
			if (this.hidden != hidden) {
				this.hidden = hidden;
				line.editItem(5, ItemUtils.set(line.getItem(5), hidden));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			new SelectGUI(() -> {
				remove();
				reopenGUI(p, true);
			}, npc -> {
				setNPCId(npcID);
				reopenGUI(p, true);
			}).create(p);
		}
		
		@Override
		public void edit(StageNPC stage) {
			super.edit(stage);
			setNPCId(stage.npcID);
			setDialog(stage.dialog);
			setHidden(stage.hide);
		}
		
		@Override
		protected StageNPC finishStage(QuestBranch branch) {
			return setFinish(new StageNPC(branch));
		}
		
		protected StageNPC setFinish(StageNPC stage) {
			stage.setNPC(npcID);
			stage.setDialog(dialog);
			stage.setHid(hidden);
			return stage;
		}
	}

}
