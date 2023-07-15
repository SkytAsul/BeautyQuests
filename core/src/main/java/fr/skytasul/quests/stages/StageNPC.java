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
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.DialogEditor;
import fr.skytasul.quests.api.events.internal.BQNPCClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Dialogable;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.utils.compatibility.GPS;
import fr.skytasul.quests.utils.types.DialogRunnerImplementation;

@LocatableType (types = LocatedType.ENTITY)
public class StageNPC extends AbstractStage implements Locatable.PreciseLocatable, Dialogable {
	
	private BqNpc npc;
	private int npcID;
	protected Dialog dialog = null;
	protected DialogRunnerImplementation dialogRunner = null;
	protected boolean hide = false;
	
	private BukkitTask task;
	
	private List<Player> cached = new ArrayList<>();
	protected AbstractHolograms<?>.BQHologram hologram;
	
	public StageNPC(StageController controller) {
		super(controller);
	}
	
	private void launchRefreshTask(){
		if (npc == null) return;
		task = new BukkitRunnable() {
			List<Player> tmp = new ArrayList<>();
			@Override
			public void run() {
				Entity en = npc.getNpc().getEntity();
				if (en == null) return;
				if (!en.getType().isAlive()) return;
				Location lc = en.getLocation();
				tmp.clear();
				for (Player p : cached) {
					if (p.getWorld() != lc.getWorld()) continue;
					if (lc.distance(p.getLocation()) > 50) continue;
					tmp.add(p);
				}
				
				if (QuestsConfigurationImplementation.getConfiguration().getHoloTalkItem() != null
						&& QuestsAPI.getAPI().hasHologramsManager()
						&& QuestsAPI.getAPI().getHologramsManager().supportItems()
						&& QuestsAPI.getAPI().getHologramsManager().supportPerPlayerVisibility()) {
					if (hologram == null) createHoloLaunch();
					hologram.setPlayersVisible(tmp);
					hologram.teleport(Utils.upLocationForEntity((LivingEntity) en, 1));
				}
				
				if (QuestsConfigurationImplementation.getConfiguration().showTalkParticles()) {
					if (tmp.isEmpty()) return;
					QuestsConfigurationImplementation.getConfiguration().getParticleTalk().send(en, tmp);
				}
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 20L, 6L);
	}
	
	private void createHoloLaunch(){
		ItemStack item = QuestsConfigurationImplementation.getConfiguration().getHoloTalkItem();
		hologram = QuestsAPI.getAPI().getHologramsManager().createHologram(npc.getNpc().getLocation(), false);
		if (QuestsConfigurationImplementation.getConfiguration().isCustomHologramNameShown() && item.hasItemMeta()
				&& item.getItemMeta().hasDisplayName())
			hologram.appendTextLine(item.getItemMeta().getDisplayName());
		hologram.appendItem(item);
	}
	
	private void removeHoloLaunch(){
		if (hologram == null) return;
		hologram.delete();
		hologram = null;
	}

	@Override
	public BqNpc getNPC() {
		return npc;
	}
	
	public int getNPCID() {
		return npcID;
	}

	public void setNPC(int npcID) {
		this.npcID = npcID;
		if (npcID >= 0)
			this.npc = QuestsPlugin.getPlugin().getNpcManager().getById(npcID);
		if (npc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("The NPC " + npcID + " does not exist for " + toString());
		}else {
			initDialogRunner();
		}
	}
	
	public void setDialog(Dialog dialog){
		this.dialog = dialog;
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
	public boolean isShown(Player player) {
		return !hide;
	}

	@Override
	public Located getLocated() {
		return npc;
	}
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source){
		return MessageUtils.format(Lang.SCOREBOARD_NPC.toString(), descriptionFormat(acc, source));
	}
	
	@Override
	public Object[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new String[] { npcName() };
	}
	
	protected void initDialogRunner() {
		if (dialogRunner != null) throw new IllegalStateException("Dialog runner already initialized");
		
		dialogRunner = new DialogRunnerImplementation(dialog, npc);
		dialogRunner.addTest(super::hasStarted);
		dialogRunner.addTestCancelling(p -> canUpdate(p, true));
		
		dialogRunner.addEndAction(this::finishStage);
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onClick(BQNPCClickEvent e) {
		if (e.isCancelled()) return;
		if (e.getNPC() != npc) return;
		if (!QuestsConfiguration.getConfig().getQuestsConfig().getNpcClicks().contains(e.getClick()))
			return;
		Player p = e.getPlayer();

		e.setCancelled(dialogRunner.onClick(p).shouldCancel());
	}
	
	protected String npcName(){
		if (npc == null)
			return "§c§lunknown NPC " + npcID;
		if (dialog != null && dialog.getNpcName() != null)
			return dialog.getNpcName();
		return npc.getNpc().getName();
	}
	
	@Override
	public void joined(Player p) {
		super.joined(p);
		cachePlayer(p);
		if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && !hide)
			GPS.launchCompass(p, npc.getLocation());
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
		if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && !hide)
			cached.forEach(GPS::stopCompass);
		if (npc != null) cached.forEach(p -> npc.removeHiddenForPlayer(p, this));
	}
	
	@Override
	public void left(Player p) {
		super.left(p);
		uncachePlayer(p);
		if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && !hide)
			GPS.stopCompass(p);
		if (dialogRunner != null) dialogRunner.removePlayer(p);
	}
	
	@Override
	public void started(PlayerAccount acc) {
		super.started(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			cachePlayer(p);
			if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && npc != null)
				GPS.launchCompass(p, npc.getLocation());
		}
	}
	
	@Override
	public void ended(PlayerAccount acc) {
		super.ended(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (dialogRunner != null) dialogRunner.removePlayer(p);
			uncachePlayer(p);
			if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && !hide)
				GPS.stopCompass(p);
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
		if (QuestsConfigurationImplementation.getConfiguration().showTalkParticles()
				|| QuestsConfigurationImplementation.getConfiguration().getHoloTalkItem() != null) {
			if (!hide) launchRefreshTask();
		}
	}

	protected void loadDatas(ConfigurationSection section) {
		if (section.contains("msg")) setDialog(Dialog.deserialize(section.getConfigurationSection("msg")));
		if (section.contains("npcID")) {
			setNPC(section.getInt("npcID"));
		}else QuestsPlugin.getPlugin().getLoggerExpanded().warning("No NPC specified for " + toString());
		if (section.contains("hid")) hide = section.getBoolean("hid");
	}
	
	@Override
	public void serialize(ConfigurationSection section) {
		section.set("npcID", npcID);
		if (dialog != null) dialog.serialize(section.createSection("msg"));
		if (hide) section.set("hid", true);
	}
	
	public static StageNPC deserialize(ConfigurationSection section, StageController controller) {
		StageNPC st = new StageNPC(controller);
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
		
		protected AbstractCreator(@NotNull StageCreationContext<T> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(SLOT_NPC, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString()), event -> {
				QuestsPlugin.getPlugin().getGuiManager().getFactory().createNpcSelection(event::reopen, newNPC -> {
					setNPCId(newNPC.getNpc().getId());
					event.reopen();
				}, false).open(event.getPlayer());
			});
			
			line.setItem(SLOT_DIALOG, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.stageText.toString(), Lang.NotSet.toString()), event -> {
				Lang.NPC_TEXT.send(event.getPlayer());
				new DialogEditor(event.getPlayer(), () -> {
					setDialog(dialog);
					event.reopen();
				}, dialog == null ? dialog = new Dialog() : dialog).start();
			});
			
			line.setItem(SLOT_HIDE, ItemUtils.itemSwitch(Lang.stageHide.toString(), hidden), event -> setHidden(!hidden));
		}
		
		public void setNPCId(int npcID) {
			this.npcID = npcID;
			getLine().refreshItem(SLOT_NPC, item -> ItemUtils.lore(item, QuestOption.formatDescription("ID: §l" + npcID)));
		}
		
		public void setDialog(Dialog dialog) {
			this.dialog = dialog;
			getLine().refreshItem(SLOT_DIALOG, item -> ItemUtils.lore(item, dialog == null ? Lang.NotSet.toString()
					: QuestOption.formatDescription(Lang.dialogLines.format(dialog.getMessages().size()))));
		}
		
		public void setHidden(boolean hidden) {
			if (this.hidden != hidden) {
				this.hidden = hidden;
				getLine().refreshItem(SLOT_HIDE, item -> ItemUtils.setSwitch(item, hidden));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createNpcSelection(context::removeAndReopenGui, newNPC -> {
				setNPCId(newNPC.getNpc().getId());
				context.reopenGui();
			}, false).open(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setNPCId(stage.getNPCID());
			setDialog(stage.dialog);
			setHidden(stage.hide);
		}
		
		@Override
		protected final T finishStage(StageController controller) {
			T stage = createStage(controller);
			stage.setDialog(dialog);
			stage.setNPC(npcID);
			stage.setHid(hidden);
			return stage;
		}
		
		protected abstract T createStage(StageController controller);

	}
	
	public static class Creator extends AbstractCreator<StageNPC> {
		
		public Creator(@NotNull StageCreationContext<StageNPC> context) {
			super(context);
		}

		@Override
		protected @NotNull StageNPC createStage(@NotNull StageController controller) {
			return new StageNPC(controller);
		}
		
	}

}
