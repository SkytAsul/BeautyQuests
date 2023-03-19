package fr.skytasul.quests.stages;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;
import fr.skytasul.quests.utils.compatibility.worldguard.WorldGuardEntryEvent;
import fr.skytasul.quests.utils.compatibility.worldguard.WorldGuardExitEvent;

@LocatableType (types = LocatedType.OTHER)
public class StageArea extends AbstractStage implements Locatable.PreciseLocatable {
	
	private static final long REFRESH_CENTER = 60 * 1000L;
	
	private final ProtectedRegion region;
	private final boolean exit;
	private final World world;
	
	private Locatable.Located center = null;
	private long lastCenter = 0;
	
	public StageArea(QuestBranch branch, String regionName, String worldName, boolean exit) {
		super(branch);
		
		World w = Bukkit.getWorld(worldName);
		Validate.notNull(w, "No world with specified name (\"" + worldName + "\")");
		this.world = w;
		
		ProtectedRegion reg = BQWorldGuard.getInstance().getRegion(regionName, w);
		Validate.notNull(reg, "No region with specified name (\"" + regionName + "\")");
		this.region = reg;
		
		this.exit = exit;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (BQWorldGuard.getInstance().doHandleEntry()) return; // on WG 7.0 or higher
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
		if (hasStarted(e.getPlayer()) && canUpdate(e.getPlayer())) {
			if (world.equals(e.getTo().getWorld()) && BQWorldGuard.getInstance().isInRegion(region, e.getTo()) == !exit) {
				finishStage(e.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onRegionEntry(WorldGuardEntryEvent e) {
		if (exit)
			return;
		if (region == null) {
			DebugUtils.printError("No region for " + toString(), "area" + toString(), 5);
			return;
		}
		if (e.getRegionsEntered().stream().anyMatch(eventRegion -> eventRegion.getId().equals(region.getId()))) {
			if (hasStarted(e.getPlayer()) && canUpdate(e.getPlayer())) finishStage(e.getPlayer());
		}
	}

	@EventHandler
	public void onRegionExit(WorldGuardExitEvent e) {
		if (!exit)
			return;
		if (region == null) {
			DebugUtils.printError("No region for " + toString(), "area" + toString(), 5);
			return;
		}
		if (e.getRegionsExited().stream().anyMatch(eventRegion -> eventRegion.getId().equals(region.getId()))) {
			if (hasStarted(e.getPlayer()) && canUpdate(e.getPlayer()))
				finishStage(e.getPlayer());
		}
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Utils.format(Lang.SCOREBOARD_REG.toString(), region.getId());
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{region.getId()};
	}

	@Override
	public Located getLocated() {
		if (region instanceof GlobalProtectedRegion) return null;
		
		if (System.currentTimeMillis() - lastCenter > REFRESH_CENTER) {
			Location centerLoc = BukkitAdapter.adapt(world,
						region.getMaximumPoint()
						.subtract(region.getMinimumPoint())
						.divide(2)
						.add(region.getMinimumPoint())) // midpoint
					.add(0.5, 0.5, 0.5);
			
			center = Locatable.Located.create(centerLoc);
			lastCenter = System.currentTimeMillis();
		}
		return center;
	}
	
	public ProtectedRegion getRegion(){
		return region;
	}
	
	public World getWorld(){
		return world;
	}
	
	@Override
	public void serialize(ConfigurationSection section) {
		section.set("region", region.getId());
		section.set("world", world.getName());
		section.set("exit", exit);
	}
	
	public static StageArea deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageArea(branch, section.getString("region"), section.getString("world"), section.getBoolean("exit", false));
	}

	public static class Creator extends StageCreation<StageArea> {

		private boolean exit = false;
		private String regionName;
		private String worldName;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			line.setItem(7, ItemUtils.item(XMaterial.PAPER, Lang.stageRegion.toString()), (p, item) -> launchRegionEditor(p, false), true, true);
			line.setItem(6, ItemUtils.itemSwitch(Lang.stageRegionExit.toString(), exit), (p, item) -> setExit(ItemUtils.toggle(item)));
		}
		
		public void setRegion(String regionName, String worldName) {
			this.regionName = regionName;
			this.worldName = worldName;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(regionName + " (" + worldName + ")")));
		}
		
		public void setExit(boolean exit) {
			if (this.exit != exit) {
				this.exit = exit;
				line.editItem(6, ItemUtils.set(line.getItem(6), exit));
			}
		}

		private void launchRegionEditor(Player p, boolean first) {
			Utils.sendMessage(p, Lang.REGION_NAME.toString() + (first ? "" : "\n" + Lang.TYPE_CANCEL.toString()));
			new TextEditor<String>(p, () -> {
				if (first) remove();
				reopenGUI(p, false);
			}, obj -> {
				if (BQWorldGuard.getInstance().regionExists(obj, p.getWorld())) {
					setRegion(obj, p.getWorld().getName());
				}else {
					Utils.sendMessage(p, Lang.REGION_DOESNT_EXIST.toString());
					if (first) remove();
				}
				reopenGUI(p, false);
			}).useStrippedMessage().enter();
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			launchRegionEditor(p, true);
		}

		@Override
		public void edit(StageArea stage) {
			super.edit(stage);
			setRegion(stage.getRegion().getId(), BQWorldGuard.getInstance().getWorld(stage.getRegion().getId()).getName());
			setExit(stage.exit);
		}
		
		@Override
		public StageArea finishStage(QuestBranch branch) {
			return new StageArea(branch, regionName, worldName, exit);
		}
	}

}
