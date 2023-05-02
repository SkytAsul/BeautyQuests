package fr.skytasul.quests.stages;

import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
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
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.creation.stages.Line;
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
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		if (QuestsConfigurationImplementation.handleGPS() && gps) GPS.launchCompass(p, lc);
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		if (QuestsConfigurationImplementation.handleGPS() && gps) GPS.stopCompass(p);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfigurationImplementation.handleGPS() && gps) GPS.launchCompass(p, lc);
		}
	}
	
	@Override
	public void ended(PlayerAccount acc) {
		super.ended(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfigurationImplementation.handleGPS() && gps) GPS.stopCompass(p);
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, DescriptionSource source){
		return descMessage;
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new Object[] { lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorldName() };
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("location", lc.serialize());
		section.set("radius", radius);
		if (!gps) section.set("gps", false);
	}

	public static StageLocation deserialize(ConfigurationSection section, StageController controller) {
		return new StageLocation(branch, BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)), section.getInt("radius"), section.getBoolean("gps", true));
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
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(SLOT_RADIUS, ItemUtils.item(XMaterial.REDSTONE, Lang.stageLocationRadius.toString()), (p, item) -> {
				Lang.LOCATION_RADIUS.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, false), x -> {
					setRadius(x);
					reopenGUI(p, false);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			});
			line.setItem(SLOT_LOCATION, ItemUtils.item(XMaterial.STICK, Lang.stageLocationLocation.toString()), (p, item) -> {
				Lang.LOCATION_GO.send(p);
				new WaitClick(p, () -> reopenGUI(p, false), NpcCreateGUI.validMove, () -> {
					setLocation(new BQLocation(p.getLocation()));
					reopenGUI(p, false);
				}).start();
			});
			line.setItem(SLOT_WORLD_PATTERN, ItemUtils.item(XMaterial.NAME_TAG, Lang.stageLocationWorldPattern.toString(), QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.toString())), (p, item) -> {
				Lang.LOCATION_WORLDPATTERN.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, false), pattern -> {
					setPattern(pattern);
					reopenGUI(p, false);
				}, PatternParser.PARSER).passNullIntoEndConsumer().start();
			});
			
			if (QuestsConfigurationImplementation.handleGPS()) line.setItem(SLOT_GPS, ItemUtils.itemSwitch(Lang.stageGPS.toString(), gps), (p, item) -> setGPS(ItemUtils.toggleSwitch(item)), true, true);
		}
		
		public void setLocation(Location location) {
			this.location = location;
			line.editItem(SLOT_LOCATION, ItemUtils.lore(line.getItem(SLOT_LOCATION), QuestOption.formatDescription(Utils.locationToString(location))));
		}
		
		public void setRadius(int radius) {
			this.radius = radius;
			line.editItem(SLOT_RADIUS, ItemUtils.lore(line.getItem(SLOT_RADIUS), Lang.stageLocationCurrentRadius.format(radius)));
		}
		
		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
			line.editItem(SLOT_WORLD_PATTERN, ItemUtils.lore(line.getItem(SLOT_WORLD_PATTERN), QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.format()), "", pattern == null ? Lang.NotSet.toString() : QuestOption.formatNullableValue(pattern.pattern())));
		}
		
		public void setGPS(boolean gps) {
			if (this.gps != gps) {
				this.gps = gps;
				if (QuestsConfigurationImplementation.handleGPS()) line.editItem(SLOT_GPS, ItemUtils.setSwitch(line.getItem(SLOT_GPS), gps));
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
			new WaitClick(p, removeAndReopen(p, false), NpcCreateGUI.validMove, () -> {
				setLocation(p.getLocation());
				setRadius(5);
				reopenGUI(p, false);
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
			return new StageLocation(branch, getBQLocation(), radius, gps);
		}
		
	}
	
}
