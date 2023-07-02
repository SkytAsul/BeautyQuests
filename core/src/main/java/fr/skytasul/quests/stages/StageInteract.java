package fr.skytasul.quests.stages;

import java.util.Collections;
import java.util.Spliterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.editors.WaitBlockClick;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.blocks.SelectBlockGUI;
import fr.skytasul.quests.utils.types.BQLocation;

@LocatableType (types = { LocatedType.BLOCK, LocatedType.OTHER })
public class StageInteract extends AbstractStage implements Locatable.MultipleLocatable, Locatable.PreciseLocatable {

	private final boolean left;
	private final BQLocation lc;
	private final BQBlock block;
	
	private Located.LocatedBlock locatedBlock;

	public StageInteract(StageController controller, boolean leftClick, BQLocation location) {
		super(controller);
		this.left = leftClick;
		this.lc = new BQLocation(location.getWorldName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
		
		this.block = null;
	}
	
	public StageInteract(StageController controller, boolean leftClick, BQBlock block) {
		super(controller);
		this.left = leftClick;
		this.block = block;
		
		this.lc = null;
	}

	public BQLocation getLocation() {
		return lc;
	}
	
	public BQBlock getBlockType() {
		return block;
	}

	public boolean needLeftClick(){
		return left;
	}
	
	@Override
	public Located getLocated() {
		if (lc == null)
			return null;
		if (locatedBlock == null) {
			Block realBlock = lc.getMatchingBlock();
			if (realBlock != null)
				locatedBlock = Located.LocatedBlock.create(realBlock);
		}
		return locatedBlock;
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		if (block == null) return null;
		
		return QuestsAPI.getAPI().getBlocksManager().getNearbyBlocks(fetcher, Collections.singleton(block));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (MinecraftVersion.MAJOR >= 9 && e.getHand() != EquipmentSlot.HAND) return;
		
		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if (lc != null) {
			if (!lc.equals(e.getClickedBlock().getLocation())) return;
		}else if (block != null) {
			if (!block.applies(e.getClickedBlock())) return;
		}else {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("No block nor location set for " + toString());
			return;
		}
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (left) e.setCancelled(true);
			finishStage(p);
		}
	}
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return lc == null ? Lang.SCOREBOARD_INTERACT_MATERIAL.format(block.getName()) : Lang.SCOREBOARD_INTERACT.format(lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("leftClick", left);
		if (lc == null) {
			section.set("block", block.getAsString());
		}else section.set("location", lc.serialize());
	}
	
	public static StageInteract deserialize(ConfigurationSection section, StageController controller) {
		if (section.contains("location")) {
			return new StageInteract(controller, section.getBoolean("leftClick"), BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)));
		}else {
			BQBlock block;
			if (section.contains("material")) {
				block = QuestsAPI.getAPI().getBlocksManager().createSimple(XMaterial.valueOf(section.getString("material")),
						null);
			}else {
				block = QuestsAPI.getAPI().getBlocksManager().deserialize(section.getString("block"));
			}
			return new StageInteract(controller, section.getBoolean("leftClick"), block);
		}
	}

	public static class Creator extends StageCreation<StageInteract> {
		
		private boolean leftClick = false;
		private Location location;
		private BQBlock block;
		
		public Creator(@NotNull StageCreationContext<StageInteract> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(6, ItemUtils.itemSwitch(Lang.leftClick.toString(), leftClick), event -> setLeftClick(!leftClick));
		}
		
		public void setLeftClick(boolean leftClick) {
			if (this.leftClick != leftClick) {
				this.leftClick = leftClick;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, leftClick));
			}
		}

		public void setLocation(Location location) {
			if (this.location == null) {
				getLine().setItem(7, ItemUtils.item(XMaterial.COMPASS, Lang.blockLocation.toString()), event -> {
					Lang.CLICK_BLOCK.send(event.getPlayer());
					new WaitBlockClick(event.getPlayer(), event::reopen, obj -> {
						setLocation(obj);
						event.reopen();
					}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
				});
			}
			getLine().refreshItem(7,
					item -> ItemUtils.lore(item, QuestOption.formatDescription(Utils.locationToString(location))));
			this.location = location;
		}
		
		public void setMaterial(BQBlock block) {
			if (this.block == null) {
				getLine().setItem(7, ItemUtils.item(XMaterial.STICK, Lang.blockMaterial.toString()), event -> {
					new SelectBlockGUI(false, (newBlock, __) -> {
						setMaterial(newBlock);
						event.reopen();
					}).open(event.getPlayer());
				});
			}
			getLine().refreshItem(7, item -> ItemUtils.lore(item, Lang.optionValue.format(block.getName())));
			this.block = block;
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			LayoutedGUI.newBuilder()
					.setInventoryType(InventoryType.HOPPER)
					.addButton(0, LayoutedButton.create(ItemUtils.item(XMaterial.COMPASS, Lang.clickLocation.toString()),
							event -> {
								Lang.CLICK_BLOCK.send(p);
								new WaitBlockClick(p, context::removeAndReopenGui, obj -> {
									setLocation(obj);
									context.reopenGui();
								}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
							}))
					.addButton(1,
							LayoutedButton.create(ItemUtils.item(XMaterial.STICK, Lang.clickMaterial.toString()), event -> {
								new SelectBlockGUI(false, (newBlock, __) -> {
									setMaterial(newBlock);
									context.reopenGui();
								}).open(p);
							}))
					.addButton(4, LayoutedButton.create(ItemUtils.itemCancel, __ -> context.removeAndReopenGui()))
					.setCloseBehavior(StandardCloseBehavior.REOPEN)
					.setName(Lang.INVENTORY_BLOCK_ACTION.toString())
					.build().open(p);
		}

		@Override
		public void edit(StageInteract stage) {
			super.edit(stage);
			if (stage.lc != null) {
				setLocation(stage.getLocation());
			}else setMaterial(stage.getBlockType());
			setLeftClick(stage.needLeftClick());
		}

		@Override
		public StageInteract finishStage(StageController controller) {
			if (location != null) {
				return new StageInteract(controller, leftClick, new BQLocation(location));
			}else return new StageInteract(controller, leftClick, block);
		}
		
	}

}
