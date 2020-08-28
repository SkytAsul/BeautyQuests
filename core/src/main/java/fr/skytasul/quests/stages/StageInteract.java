package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.WaitBlockClick;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class StageInteract extends AbstractStage {

	private Location lc;
	private boolean left;
	
	public StageInteract(QuestBranch branch, Location location, boolean leftClick){
		super(branch);
		this.lc = location.getBlock().getLocation();
		this.left = leftClick;
	}

	public Location getLocation(){
		return lc;
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
		if (!e.getClickedBlock().getLocation().equals(lc)) return;
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (left) e.setCancelled(true);
			finishStage(p);
		}
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_INTERACT.format(lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	protected void serialize(Map<String, Object> map){
		map.put("leftClick", left);
		map.put("location", lc.serialize());
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		return new StageInteract(branch, Location.deserialize((Map<String, Object>) map.get("location")), (boolean) map.get("leftClick"));
	}

	public static class Creator implements StageCreationRunnables<StageInteract> {

		public void start(Player p, LineData datas) {
			Lang.CLICK_BLOCK.send(p);
			new WaitBlockClick(p, () -> {
				datas.getGUI().deleteStageLine(datas, p);
				datas.getGUI().reopen(p, false);
			}, obj -> {
				datas.put("lc", obj);
				datas.getGUI().reopen(p, false);
				setItems(datas);
			}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
		}

		public static void setItems(LineData datas) {
			datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.leftClick.toString(), datas.containsKey("left") ? (boolean) datas.get("left") : false), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					datas.put("left", ItemUtils.toggle(item));
				}
			});
			datas.getLine().setItem(6, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString()), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					Lang.CLICK_BLOCK.send(p);
					new WaitBlockClick(p, () -> datas.getGUI().reopen(p, false), obj -> {
						datas.getGUI().reopen(p, false);
						datas.put("lc", obj);
					}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).enterOrLeave(p);
				}
			});
		}

		public void edit(LineData datas, StageInteract stage) {
			datas.put("lc", stage.getLocation());
			datas.put("left", stage.needLeftClick());
			setItems(datas);
		}

		public StageInteract finish(LineData datas, QuestBranch branch) {
			return new StageInteract(branch, (Location) datas.get("lc"), datas.containsKey("left") ? (boolean) datas.get("left") : false);
		}

	}

}
