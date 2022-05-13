package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
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
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.BQNPCClickEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.Dialogable;
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
import fr.skytasul.quests.utils.types.DialogRunner;

public class StageNPC extends AbstractStage implements Locatable, Dialogable {
	
	private BQNPC npc;
	private int npcID;
	protected Dialog dialog = null;
	protected DialogRunner dialogRunner = null;
	protected boolean hide = false;
	
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
				
				if (QuestsConfiguration.getHoloTalkItem() != null && QuestsAPI.hasHologramsManager() && QuestsAPI.getHologramsManager().supportItems() && QuestsAPI.getHologramsManager().supportPerPlayerVisibility()) {
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
		ItemStack item = QuestsConfiguration.getHoloTalkItem();
		hologram = QuestsAPI.getHologramsManager().createHologram(npc.getLocation(), false);
		if (QuestsConfiguration.isCustomHologramNameShown() && item.hasItemMeta() && item.getItemMeta().hasDisplayName())
			hologram.appendTextLine(item.getItemMeta().getDisplayName());
		hologram.appendItem(item);
	}
	
	private void removeHoloLaunch(){
		if (hologram == null) return;
		hologram.delete();
		hologram = null;
	}

	@Override
	public BQNPC getNPC() {
		return npc;
	}
	
	public int getNPCID() {
		return npcID;
	}

	public void setNPC(int npcID) {
		this.npcID = npcID;
		if (npcID >= 0) this.npc = QuestsAPI.getNPCsManager().getById(npcID);
		if (npc == null) {
			BeautyQuests.logger.warning("The NPC " + npcID + " does not exist for " + toString());
		}else {
			initDialogRunner();
		}
	}
	
	public void setDialog(Dialog dialog){
		this.dialog = dialog;
	}
	
	@Override
	public boolean hasDialog(){
		return dialog != null && !dialog.messages.isEmpty();
	}
	
	@Override
	public Dialog getDialog(){
		return dialog;
	}
	
	@Override
	public DialogRunner getDialogRunner() {
		return dialogRunner;
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
		return Utils.format(Lang.SCOREBOARD_NPC.toString(), descriptionFormat(acc, source));
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new String[] { npcName() };
	}
	
	protected void initDialogRunner() {
		if (dialogRunner != null) throw new IllegalStateException("Dialog runner already initialized");
		
		dialogRunner = new DialogRunner(dialog, npc);
		dialogRunner.addTest(super::hasStarted);
		dialogRunner.addTestCancelling(p -> canUpdate(p, true));
		
		dialogRunner.addEndAction(this::finishStage);
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onClick(BQNPCClickEvent e) {
		if (e.isCancelled()) return;
		if (e.getNPC() != npc) return;
		if (!QuestsConfiguration.getNPCClick().applies(e.getClick())) return;
		Player p = e.getPlayer();

		e.setCancelled(dialogRunner.onClick(p).shouldCancel());
	}
	
	protected String npcName(){
		if (npc == null)
			return "§c§lunknown NPC " + npcID;
		if (dialog != null && dialog.npcName != null)
			return dialog.npcName;
		return npc.getName();
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		cachePlayer(p);
		if (QuestsConfiguration.handleGPS() && !hide) GPS.launchCompass(p, npc.getLocation());
	}

	private void cachePlayer(Player p) {
		cached.add(p);
		if (npc != null) npc.hideForPlayer(p, this);
	}
	
	private void uncachePlayer(Player p) {
		cached.remove(p);
		if (npc != null) npc.removeHiddenForPlayer(p, this);
	}
	
	private void uncacheAll() {
		if (QuestsConfiguration.handleGPS() && !hide) cached.forEach(GPS::stopCompass);
		if (npc != null) cached.forEach(p -> npc.removeHiddenForPlayer(p, this));
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		uncachePlayer(p);
		if (QuestsConfiguration.handleGPS() && !hide) GPS.stopCompass(p);
		if (dialogRunner != null) dialogRunner.removePlayer(p);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cachePlayer(p);
			if (QuestsConfiguration.handleGPS() && npc != null) GPS.launchCompass(p, npc.getLocation());
		}
	}
	
	@Override
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (dialogRunner != null) dialogRunner.removePlayer(p);
			uncachePlayer(p);
			if (QuestsConfiguration.handleGPS() && !hide) GPS.stopCompass(p);
		}
	}
	
	@Override
	public void unload() {
		super.unload();
		if (task != null) task.cancel();
		if (dialogRunner != null) dialogRunner.unload();
		removeHoloLaunch();
		uncacheAll();
	}
	
	@Override
	public void load(){
		super.load();
		if (QuestsConfiguration.showTalkParticles() || QuestsConfiguration.getHoloTalkItem() != null){
			if (!hide) launchRefreshTask();
		}
	}

	protected void loadDatas(ConfigurationSection section) {
		if (section.contains("msg")) setDialog(Dialog.deserialize(section.getConfigurationSection("msg")));
		if (section.contains("npcID")) {
			setNPC(section.getInt("npcID"));
		}else BeautyQuests.logger.warning("No NPC specified for " + toString());
		if (section.contains("hid")) hide = section.getBoolean("hid");
	}
	
	@Override
	public void serialize(ConfigurationSection section) {
		section.set("npcID", npcID);
		if (dialog != null) dialog.serialize(section.createSection("msg"));
		if (hide) section.set("hid", true);
	}
	
	public static StageNPC deserialize(ConfigurationSection section, QuestBranch branch) {
		StageNPC st = new StageNPC(branch);
		st.loadDatas(section);
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
			line.editItem(SLOT_DIALOG, ItemUtils.lore(line.getItem(SLOT_DIALOG), dialog == null ? Lang.NotSet.toString() : QuestOption.formatDescription(Lang.dialogLines.format(dialog.messages.size()))));
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
			stage.setDialog(dialog);
			stage.setNPC(npcID);
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
