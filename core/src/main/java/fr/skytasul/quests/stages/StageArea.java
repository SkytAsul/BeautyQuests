package fr.skytasul.quests.stages;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.utils.DebugUtils;
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
	
	public StageArea(StageController controller, String regionName, String worldName, boolean exit) {
		super(controller);
		
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
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("region_id", region.getId());
		placeholders.register("region_world", world.getName());
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_REG.toString();
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
	
	public static StageArea deserialize(ConfigurationSection section, StageController controller) {
		return new StageArea(controller, section.getString("region"), section.getString("world"), section.getBoolean("exit", false));
	}

	public static class Creator extends StageCreation<StageArea> {

		private boolean exit = false;
		private String regionName;
		private String worldName;
		
		public Creator(@NotNull StageCreationContext<StageArea> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			line.setItem(7, ItemUtils.item(XMaterial.PAPER, Lang.stageRegion.toString()),
					event -> launchRegionEditor(event.getPlayer(), false));
			line.setItem(6, ItemUtils.itemSwitch(Lang.stageRegionExit.toString(), exit), event -> setExit(!exit));
		}
		
		public void setRegion(String regionName, String worldName) {
			this.regionName = regionName;
			this.worldName = worldName;
			getLine().refreshItemLore(7, QuestOption.formatNullableValue(regionName + " (" + worldName + ")"));
		}
		
		public void setExit(boolean exit) {
			if (this.exit != exit) {
				this.exit = exit;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, exit));
			}
		}

		private void launchRegionEditor(Player p, boolean first) {
			MessageUtils.sendMessage(p, Lang.REGION_NAME.toString() + (first ? "" : "\n" + Lang.TYPE_CANCEL.toString()),
					MessageType.PREFIXED);
			new TextEditor<String>(p, () -> {
				if (first)
					context.remove();
				context.reopenGui();
			}, obj -> {
				if (BQWorldGuard.getInstance().regionExists(obj, p.getWorld())) {
					setRegion(obj, p.getWorld().getName());
				}else {
					Lang.REGION_DOESNT_EXIST.send(p);
					if (first)
						context.remove();
				}
				context.reopenGui();
			}).useStrippedMessage().start();
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
		public StageArea finishStage(StageController controller) {
			return new StageArea(controller, regionName, worldName, exit);
		}
	}

}
