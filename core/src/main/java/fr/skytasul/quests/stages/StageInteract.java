package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitBlockClick;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XBlock;
import fr.skytasul.quests.utils.XMaterial;

public class StageInteract extends AbstractStage {

	private boolean left;
	private Location lc;
	private XMaterial material;
	
	public StageInteract(QuestBranch branch, boolean leftClick, Location location) {
		super(branch);
		this.left = leftClick;
		this.lc = location.getBlock().getLocation();
	}
	
	public StageInteract(QuestBranch branch, boolean leftClick, XMaterial material) {
		super(branch);
		this.left = leftClick;
		this.material = material;
	}

	public Location getLocation(){
		return lc;
	}
	
	public XMaterial getMaterial() {
		return material;
	}

	public boolean needLeftClick(){
		return left;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (lc != null) {
			if (!e.getClickedBlock().getLocation().equals(lc)) return;
		}else if (!XBlock.isType(e.getClickedBlock(), material)) return;
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (left) e.setCancelled(true);
			finishStage(p);
		}
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return lc == null ? Lang.SCOREBOARD_INTERACT_MATERIAL.format(MinecraftNames.getMaterialName(material)) : Lang.SCOREBOARD_INTERACT.format(lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	protected void serialize(Map<String, Object> map){
		map.put("leftClick", left);
		if (lc == null) {
			map.put("material", material.name());
		}else map.put("location", lc.serialize());
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		if (map.containsKey("location")) {
			return new StageInteract(branch, (boolean) map.get("leftClick"), Location.deserialize((Map<String, Object>) map.get("location")));
		}else return new StageInteract(branch, (boolean) map.get("leftClick"), XMaterial.valueOf((String) map.get("material")));
	}

	public static class Creator implements StageCreationRunnables<StageInteract> {

		public void start(Player p, LineData datas) {
			datas.put("left", false);
			Runnable cancel = () -> {
				datas.getGUI().deleteStageLine(datas, p);
				datas.getGUI().reopen(p, true);
			};
			new ChooseActionGUI(cancel, () -> {
				Lang.CLICK_BLOCK.send(p);
				new WaitBlockClick(p, cancel, obj -> {
					datas.put("lc", obj);
					datas.getGUI().reopen(p, true);
					setItems(datas);
				}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
			}, () -> {
				Lang.BLOCK_NAME.send(p);
				new TextEditor<>(p, cancel, material -> {
					datas.put("material", material);
					datas.getGUI().reopen(p, true);
					setItems(datas);
				}, new MaterialParser(false, true)).enterOrLeave(p);
			}).create(p);
		}

		public static void setItems(LineData datas) {
			datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.leftClick.toString(), datas.get("left")), (p, datass, item) -> datas.put("left", ItemUtils.toggle(item)));
			if (datas.containsKey("lc")) {
				datas.getLine().setItem(6, ItemUtils.item(XMaterial.COMPASS, Lang.blockLocation.toString()), (p, datass, item) -> {
					Lang.CLICK_BLOCK.send(p);
					new WaitBlockClick(p, () -> datas.getGUI().reopen(p, false), obj -> {
						datas.put("lc", obj);
						datas.getGUI().reopen(p, false);
					}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
				});
			}else {
				datas.getLine().setItem(6, ItemUtils.item(XMaterial.STICK, Lang.blockMaterial.toString(), Lang.optionValue.format(datas.<XMaterial>get("material").toString())), (p, datass, item) -> {
					Lang.BLOCK_NAME.send(p);
					new TextEditor<>(p, () -> datas.getGUI().reopen(p, false), material -> {
						ItemUtils.lore(item, Lang.optionValue.format(material.name()));
						datas.put("material", material);
						datas.getGUI().reopen(p, false);
					}, new MaterialParser(false, true)).enterOrLeave(p);
				});
			}
		}

		public void edit(LineData datas, StageInteract stage) {
			if (stage.lc != null) {
				datas.put("lc", stage.getLocation());
			}else datas.put("material", stage.material);
			datas.put("left", stage.needLeftClick());
			setItems(datas);
		}

		public StageInteract finish(LineData datas, QuestBranch branch) {
			if (datas.containsKey("lc")) {
				return new StageInteract(branch, datas.get("left"), datas.<Location>get("lc"));
			}else return new StageInteract(branch, datas.get("left"), datas.<XMaterial>get("material"));
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
