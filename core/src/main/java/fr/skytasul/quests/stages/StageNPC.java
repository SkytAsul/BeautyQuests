package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
import fr.skytasul.quests.api.events.BQNPCClickEvent;
import fr.skytasul.quests.api.events.DialogSendEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.Locatable;
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

public class StageNPC extends AbstractStage implements Locatable {
	
	private BQNPC npc;
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
			@Override
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
		hologram = QuestsAPI.getHologramsManager().createHologram(npc.getLocation(), false);
		hologram.appendItem(QuestsConfiguration.getHoloTalkItem());
	}
	
	private void removeHoloLaunch(){
		if (hologram == null) return;
		hologram.delete();
		hologram = null;
	}

	public BQNPC getNPC() {
		return npc;
	}
	
	public int getNPCID() {
		return npcID;
	}

	public void setNPC(int npcID) {
		this.npcID = npcID;
		if (npcID >= 0) this.npc = QuestsAPI.getNPCsManager().getById(npcID);
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
	
	@Override
	public Location getLocation() {
		return npc != null && npc.isSpawned() ? npc.getLocation() : null;
	}
	
	@Override
	public boolean isShown() {
		return !hide;
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_NPC.toString(), npcName());
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{npcName()};
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onClick(BQNPCClickEvent e) {
		if (e.isCancelled()) return;
		if (e.getNPC() != npc) return;
		if (!QuestsConfiguration.getNPCClick().applies(e.getClick())) return;
		Player p = e.getPlayer();
		if (!hasStarted(p)) return;
		boolean canUpdate = canUpdate(p, true);
		if (!branch.isRegularStage(this)) { // is ending stage
			if (bringBack == null || !canUpdate || !bringBack.checkItems(p, false)) { // if just text or don't have items
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
		if (!canUpdate) return;
		
		if (dialog != null) { // dialog exists
			Runnable runnable = () -> {
				if (bringBack != null) {
					if (!bringBack.checkItems(p, true)) return;
					bringBack.removeItems(p);
				}
				finishStage(p);
			};
			DialogSendEvent event = new DialogSendEvent(dialog, npc, p, runnable);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
			dialog.send(p, npc, runnable);
			return;
		}else if (bringBack != null){ // no dialog but bringback
			bringBack.removeItems(p);
		}
		finishStage(p);
	}
	
	protected String npcName(){
		return (npc != null) ? (dialog != null && dialog.npcName != null ? dialog.npcName : npc.getName()) : "§c§lunknown NPC " + npcID;
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		cached.add(p);
		if (QuestsConfiguration.handleGPS() && !hide) GPS.launchCompass(p, npc.getLocation());
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		cached.remove(p);
		if (QuestsConfiguration.handleGPS() && !hide) GPS.stopCompass(p);
		if (dialog != null) dialog.remove(p);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cached.add(p);
			if (QuestsConfiguration.handleGPS()) GPS.launchCompass(p, npc.getLocation());
		}
	}
	
	@Override
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (dialog != null) dialog.remove(p);
			cached.remove(p);
			if (QuestsConfiguration.handleGPS() && !hide) GPS.stopCompass(p);
		}
	}
	
	@Override
	public void unload() {
		super.unload();
		if (task != null) task.cancel();
		if (hologram != null) removeHoloLaunch();
		if (QuestsConfiguration.handleGPS() && !hide) cached.forEach(GPS::stopCompass);
		if (dialog != null) dialog.unload();
	}
	
	@Override
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
	
	@Override
	public void serialize(Map<String, Object> map){
		map.put("npcID", npcID);
		if (dialog != null) map.put("msg", dialog.serialize());
		if (hide) map.put("hid", true);
	}
	
	public static StageNPC deserialize(Map<String, Object> map, QuestBranch branch) {
		StageNPC st = new StageNPC(branch);
		st.loadDatas(map);
		return st;
	}

	public abstract static class AbstractCreator<T extends StageNPC> extends StageCreation<T> {
		
		private static final int SLOT_HIDE = 6;
		private static final int SLOT_NPC = 7;
		private static final int SLOT_DIALOG = 8;
		
		private int npcID = -1;
		private Dialog dialog = null;
		private boolean hidden = false;
		
		protected AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(SLOT_NPC, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString()), (p, item) -> {
				new SelectGUI(() -> reopenGUI(p, true), newNPC -> {
					setNPCId(newNPC.getId());
					reopenGUI(p, true);
				}).create(p);
			});
			
			line.setItem(SLOT_DIALOG, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString(), Lang.NotSet.toString()), (p, item) -> {
				Utils.sendMessage(p, Lang.NPC_TEXT.toString());
				new DialogEditor(p, () -> {
					setDialog(dialog);
					reopenGUI(p, false);
				}, dialog == null ? dialog = new Dialog() : dialog).enter();
			}, true, true);
			
			line.setItem(SLOT_HIDE, ItemUtils.itemSwitch(Lang.stageHide.toString(), hidden), (p, item) -> setHidden(ItemUtils.toggle(item)), true, true);
		}
		
		public void setNPCId(int npcID) {
			this.npcID = npcID;
			line.editItem(SLOT_NPC, ItemUtils.lore(line.getItem(SLOT_NPC), QuestOption.formatDescription("ID: §l" + npcID)));
		}
		
		public void setDialog(Dialog dialog) {
			this.dialog = dialog;
			line.editItem(SLOT_DIALOG, ItemUtils.lore(line.getItem(SLOT_DIALOG), dialog == null ? Lang.NotSet.toString() : QuestOption.formatDescription(dialog.messages.valuesSize() + " line(s)")));
		}
		
		public void setHidden(boolean hidden) {
			if (this.hidden != hidden) {
				this.hidden = hidden;
				line.editItem(SLOT_HIDE, ItemUtils.set(line.getItem(SLOT_HIDE), hidden));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			new SelectGUI(removeAndReopen(p, true), newNPC -> {
				setNPCId(newNPC.getId());
				reopenGUI(p, true);
			}).create(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setNPCId(stage.getNPCID());
			setDialog(stage.dialog);
			setHidden(stage.hide);
		}
		
		@Override
		protected final T finishStage(QuestBranch branch) {
			T stage = createStage(branch);
			stage.setNPC(npcID);
			stage.setDialog(dialog);
			stage.setHid(hidden);
			return stage;
		}
		
		protected abstract T createStage(QuestBranch branch);
		
	}
	
	public static class Creator extends AbstractCreator<StageNPC> {
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected StageNPC createStage(QuestBranch branch) {
			return new StageNPC(branch);
		}
		
	}

}
