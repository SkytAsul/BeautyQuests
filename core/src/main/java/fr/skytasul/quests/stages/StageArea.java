package fr.skytasul.quests.stages;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.WorldGuard;

public class StageArea extends AbstractStage{
	
	private ProtectedRegion region;
	private World world;
	
	public StageArea(QuestBranch branch, String regionName, String worldName){
		super(branch);
		
		World w = Bukkit.getWorld(worldName);
		Validate.notNull(w, "No world with specified name (\"" + worldName + "\")");
		this.world = w;
		
		ProtectedRegion region = WorldGuard.getRegion(regionName, w);
		Validate.notNull(w, "No region with specified name (\"" + regionName + "\")");
		this.region = region;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
		if (hasStarted(e.getPlayer())){
			if (WorldGuard.isInRegion(region, e.getTo())){
				finishStage(e.getPlayer());
			}
		}
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_REG.toString(), region.getId());
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{region.getId()};
	}
	
	public ProtectedRegion getRegion(){
		return region;
	}
	
	public World getWorld(){
		return world;
	}
	

	
	public void serialize(Map<String, Object> map){
		map.put("region", region.getId());
		map.put("world", world.getName());
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		AbstractStage st = new StageArea(branch, (String) map.get("region"), (String) map.get("world"));
		return st;
	}

	public static class Creator implements StageCreationRunnables<StageArea> {
		private static final ItemStack regionName = ItemUtils.item(XMaterial.PAPER, Lang.stageRegion.toString());

		public void start(Player p, LineData datas) {
			StagesGUI sg = datas.getGUI();
			Line line = datas.getLine();
			setItem(line, sg);
			launchRegionEditor(p, line, sg, datas, true);
		}

		private static void launchRegionEditor(Player p, Line line, StagesGUI sg, LineData datas, boolean first) {
			Utils.sendMessage(p, Lang.REGION_NAME.toString() + (first ? "" : "\n" + Lang.TYPE_CANCEL.toString()));
			TextEditor wt = Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				String msg = (String) obj;
				if (WorldGuard.regionExists(msg, p.getWorld())) {
					sg.reopen(p, false);
					ItemUtils.name(line.getItem(6), msg);
					datas.put("region", msg);
					datas.put("world", p.getWorld().getName());
				}else {
					Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
					sg.reopen(p, false);
					if (first) line.click(0, p, line.getItem(0));
				}
			}));
			wt.cancel = () -> {
				sg.reopen(p, false);
				if (first) line.click(0, p, line.getItem(0));
			};
		}

		public static void setItem(Line line, StagesGUI sg) {
			line.setItem(6, regionName.clone(), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					launchRegionEditor(p, line, sg, datas, false);
				}
			}, true, true);
		}

		public StageArea finish(LineData datas, QuestBranch branch) {
			StageArea stage = new StageArea(branch, (String) datas.get("region"), (String) datas.get("world"));
			return stage;
		}

		public void edit(LineData datas, StageArea stage) {
			datas.put("region", stage.getRegion().getId());
			datas.put("world", WorldGuard.getWorld(stage.getRegion().getId()).getName());
			setItem(datas.getLine(), datas.getGUI());
			ItemUtils.name(datas.getLine().getItem(6), stage.getRegion().getId());
		}
	}

}
