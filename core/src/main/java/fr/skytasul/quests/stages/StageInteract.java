package fr.skytasul.quests.stages;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.editors.WaitBlockClick;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.SelectBlockGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;
import fr.skytasul.quests.utils.types.BQLocation;

public class StageInteract extends AbstractStage implements Locatable.MultipleLocatable, Locatable.PreciseLocatable {

	private final boolean left;
	private final BQLocation lc;
	private final Located.LocatedBlock locatedBlock;
	private final BQBlock block;
	
	public StageInteract(QuestBranch branch, boolean leftClick, BQLocation location) {
		super(branch);
		this.left = leftClick;
		this.lc = new BQLocation(location.getBlock().getLocation());
		this.locatedBlock = Located.LocatedBlock.create(lc);
		
		this.block = null;
	}
	
	public StageInteract(QuestBranch branch, boolean leftClick, BQBlock block) {
		super(branch);
		this.left = leftClick;
		this.block = block;
		
		this.lc = null;
		this.locatedBlock = null;
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
		return locatedBlock;
	}
	
	@Override
	public Collection<Located> getNearbyLocated(NearbyFetcher fetcher) {
		if (block == null) return null;
		
		return BQBlock.getNearbyBlocks(fetcher, Collections.singleton(block));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (lc != null) {
			if (!lc.equals(e.getClickedBlock().getLocation())) return;
		}else if (!block.applies(e.getClickedBlock())) return;
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (left) e.setCancelled(true);
			finishStage(p);
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source){
		return lc == null ? Lang.SCOREBOARD_INTERACT_MATERIAL.format(block.getName()) : Lang.SCOREBOARD_INTERACT.format(lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("leftClick", left);
		if (lc == null) {
			section.set("block", block.getAsString());
		}else section.set("location", lc.serialize());
	}
	
	public static StageInteract deserialize(ConfigurationSection section, QuestBranch branch) {
		if (section.contains("location")) {
			return new StageInteract(branch, section.getBoolean("leftClick"), BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)));
		}else {
			BQBlock block;
			if (section.contains("material")) {
				block = new BQBlock.BQBlockMaterial(XMaterial.valueOf(section.getString("material")));
			}else {
				block = BQBlock.fromString(section.getString("block"));
			}
			return new StageInteract(branch, section.getBoolean("leftClick"), block);
		}
	}

	public static class Creator extends StageCreation<StageInteract> {
		
		private boolean leftClick = false;
		private Location location;
		private BQBlock block;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);

			line.setItem(6, ItemUtils.itemSwitch(Lang.leftClick.toString(), leftClick), (p, item) -> setLeftClick(ItemUtils.toggle(item)));
		}
		
		public void setLeftClick(boolean leftClick) {
			if (this.leftClick != leftClick) {
				this.leftClick = leftClick;
				line.editItem(6, ItemUtils.set(line.getItem(6), leftClick));
			}
		}

		public void setLocation(Location location) {
			if (this.location == null) {
				line.setItem(7, ItemUtils.item(XMaterial.COMPASS, Lang.blockLocation.toString()), (p, item) -> {
					Lang.CLICK_BLOCK.send(p);
					new WaitBlockClick(p, () -> reopenGUI(p, false), obj -> {
						setLocation(obj);
						reopenGUI(p, false);
					}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enter();
				});
			}
			line.editItem(7, ItemUtils.lore(line.getItem(7), QuestOption.formatDescription(Utils.locationToString(location))));
			this.location = location;
		}
		
		public void setMaterial(BQBlock block) {
			if (this.block == null) {
				line.setItem(7, ItemUtils.item(XMaterial.STICK, Lang.blockMaterial.toString()), (p, item) -> {
					new SelectBlockGUI(false, (newBlock, __) -> {
						setMaterial(newBlock);
						reopenGUI(p, true);
					}).create(p);
				});
			}
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(block.getName())));
			this.block = block;
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Runnable cancel = removeAndReopen(p, true);
			new ChooseActionGUI(cancel, () -> {
				Lang.CLICK_BLOCK.send(p);
				new WaitBlockClick(p, cancel, obj -> {
					setLocation(obj);
					reopenGUI(p, true);
				}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enter();
			}, () -> {
				new SelectBlockGUI(false, (newBlock, __) -> {
					setMaterial(newBlock);
					reopenGUI(p, true);
				}).create(p);
			}).create(p);
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
		public StageInteract finishStage(QuestBranch branch) {
			if (location != null) {
				return new StageInteract(branch, leftClick, new BQLocation(location));
			}else return new StageInteract(branch, leftClick, block);
		}
		
		private class ChooseActionGUI implements CustomInventory {
			
			private Runnable cancel, location, type;
			
			public ChooseActionGUI(Runnable cancel, Runnable location, Runnable type) {
				this.cancel = cancel;
				this.location = location;
				this.type = type;
			}
			
			@Override
			public Inventory open(Player p) {
				Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_BLOCK_ACTION.toString());
				inv.setItem(0, ItemUtils.item(XMaterial.COMPASS, Lang.clickLocation.toString()));
				inv.setItem(1, ItemUtils.item(XMaterial.STICK, Lang.clickMaterial.toString()));
				inv.setItem(4, ItemUtils.itemCancel);
				return p.openInventory(inv).getTopInventory();
			}
			
			@Override
			public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
				if (slot == 0) {
					location.run();
				}else if (slot == 1) {
					type.run();
				}else if (slot == 4) {
					cancel.run();
				}
				return true;
			}
		};

	}

}
