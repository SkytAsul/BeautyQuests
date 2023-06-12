package fr.skytasul.quests.stages;

import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.editors.checkers.PatternParser;
import fr.skytasul.quests.api.gui.ItemUtils;
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
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.npc.NpcCreateGUI;
import fr.skytasul.quests.utils.compatibility.GPS;
import fr.skytasul.quests.utils.types.BQLocation;

@LocatableType (types = LocatedType.OTHER)
public class StageLocation extends AbstractStage implements Locatable.PreciseLocatable {

	private final BQLocation lc;
	private final int radius;
	private final int radiusSquared;
	private final boolean gps;
	
	private String descMessage;
	
	public StageLocation(StageController controller, BQLocation lc, int radius, boolean gps) {
		super(controller);
		this.lc = lc;
		this.radius = radius;
		this.radiusSquared = radius * radius;
		this.gps = gps;
		
		this.descMessage = Lang.SCOREBOARD_LOCATION.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorldName());
	}
	
	public BQLocation getLocation() {
		return lc;
	}
	
	@Override
	public boolean isShown(Player player) {
		return isGPSEnabled();
	}
	
	@Override
	public Located getLocated() {
		return lc;
	}
	
	public int getRadius(){
		return radius;
	}
	
	public boolean isGPSEnabled() {
		return gps;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY()
				&& e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return; // only rotation
		if (!lc.isWorld(e.getTo().getWorld())) return;
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (lc.distanceSquared(e.getTo()) <= radiusSquared) finishStage(p);
		}
	}
	
	@Override
	public void joined(Player p) {
		super.joined(p);
		if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && gps)
			GPS.launchCompass(p, lc);
	}
	
	@Override
	public void left(Player p) {
		super.left(p);
		if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && gps)
			GPS.stopCompass(p);
	}
	
	@Override
	public void started(PlayerAccount acc) {
		super.started(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && gps)
				GPS.launchCompass(p, lc);
		}
	}
	
	@Override
	public void ended(PlayerAccount acc) {
		super.ended(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfigurationImplementation.getConfiguration().handleGPS() && gps)
				GPS.stopCompass(p);
		}
	}
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return descMessage;
	}
	
	@Override
	public Object[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new Object[] { lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorldName() };
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("location", lc.serialize());
		section.set("radius", radius);
		if (!gps) section.set("gps", false);
	}

	public static StageLocation deserialize(ConfigurationSection section, StageController controller) {
		return new StageLocation(controller, BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)), section.getInt("radius"), section.getBoolean("gps", true));
	}
	
	public static class Creator extends StageCreation<StageLocation> {
		
		private static final int SLOT_RADIUS = 6;
		private static final int SLOT_LOCATION = 7;
		private static final int SLOT_WORLD_PATTERN = 8;
		private static final int SLOT_GPS = 9;
		
		private Location location;
		private Pattern pattern;
		private int radius;
		private boolean gps = true;
		
		public Creator(@NotNull StageCreationContext<StageLocation> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(SLOT_RADIUS, ItemUtils.item(XMaterial.REDSTONE, Lang.stageLocationRadius.toString()), event -> {
				Lang.LOCATION_RADIUS.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, x -> {
					setRadius(x);
					event.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			});
			line.setItem(SLOT_LOCATION, ItemUtils.item(XMaterial.STICK, Lang.stageLocationLocation.toString()), event -> {
				Lang.LOCATION_GO.send(event.getPlayer());
				new WaitClick(event.getPlayer(), event::reopen, NpcCreateGUI.validMove, () -> {
					setLocation(new BQLocation(event.getPlayer().getLocation()));
					event.reopen();
				}).start();
			});
			line.setItem(SLOT_WORLD_PATTERN, ItemUtils.item(XMaterial.NAME_TAG, Lang.stageLocationWorldPattern.toString(), QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.toString())), event -> {
				Lang.LOCATION_WORLDPATTERN.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, pattern -> {
					setPattern(pattern);
					event.reopen();
				}, PatternParser.PARSER).passNullIntoEndConsumer().start();
			});
			
			if (QuestsConfigurationImplementation.getConfiguration().handleGPS())
				line.setItem(SLOT_GPS, ItemUtils.itemSwitch(Lang.stageGPS.toString(), gps), event -> setGPS(!gps));
		}
		
		public void setLocation(Location location) {
			this.location = location;
			getLine().refreshItem(SLOT_LOCATION,
					item -> ItemUtils.lore(item, QuestOption.formatDescription(Utils.locationToString(location))));
		}
		
		public void setRadius(int radius) {
			this.radius = radius;
			getLine().refreshItem(SLOT_RADIUS, item -> ItemUtils.lore(item, Lang.stageLocationCurrentRadius.format(radius)));
		}
		
		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
			getLine().refreshItem(SLOT_WORLD_PATTERN,
					item -> ItemUtils.lore(item, QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.format()),
							"",
							pattern == null ? Lang.NotSet.toString() : QuestOption.formatNullableValue(pattern.pattern())));
		}
		
		public void setGPS(boolean gps) {
			if (this.gps != gps) {
				this.gps = gps;
				if (QuestsConfigurationImplementation.getConfiguration().handleGPS())
					getLine().refreshItem(SLOT_GPS, item -> ItemUtils.setSwitch(item, gps));
			}
		}
		
		private BQLocation getBQLocation() {
			BQLocation loc = new BQLocation(location);
			if (pattern != null) loc.setWorldPattern(pattern);
			return loc;
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Lang.LOCATION_GO.send(p);
			new WaitClick(p, context::removeAndReopenGui, NpcCreateGUI.validMove, () -> {
				setLocation(p.getLocation());
				setRadius(5);
				context.reopenGui();
			}).start();
		}

		@Override
		public void edit(StageLocation stage) {
			super.edit(stage);
			setLocation(stage.getLocation());
			setRadius(stage.getRadius());
			setPattern(stage.getLocation().getWorldPattern());
			setGPS(stage.isGPSEnabled());
		}
		
		@Override
		public StageLocation finishStage(StageController controller) {
			return new StageLocation(controller, getBQLocation(), radius, gps);
		}
		
	}
	
}
