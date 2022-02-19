package fr.skytasul.quests.stages;

import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.Locatable;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.editors.checkers.PatternParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.GPS;
import fr.skytasul.quests.utils.types.BQLocation;

public class StageLocation extends AbstractStage implements Locatable {

	private final BQLocation lc;
	private final int radius;
	private final int radiusSquared;
	private final boolean gps;
	
	private String descMessage;
	
	public StageLocation(QuestBranch branch, BQLocation lc, int radius, boolean gps) {
		super(branch);
		this.lc = lc;
		this.radius = radius;
		this.radiusSquared = radius * radius;
		this.gps = gps;
		
		this.descMessage = Lang.SCOREBOARD_LOCATION.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorldName());
	}
	
	@Override
	public BQLocation getLocation() {
		return lc;
	}
	
	@Override
	public boolean isShown() {
		return isGPSEnabled();
	}
	
	public int getRadius(){
		return radius;
	}
	
	public boolean isGPSEnabled() {
		return gps;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (!lc.isWorld(e.getTo().getWorld())) return;
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return; // only rotation
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (lc.distanceSquared(e.getTo()) <= radiusSquared) finishStage(p);
		}
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		if (QuestsConfiguration.handleGPS() && gps) GPS.launchCompass(p, lc);
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		if (QuestsConfiguration.handleGPS() && gps) GPS.stopCompass(p);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfiguration.handleGPS() && gps) GPS.launchCompass(p, lc);
		}
	}
	
	@Override
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (QuestsConfiguration.handleGPS() && gps) GPS.stopCompass(p);
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source){
		return descMessage;
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Object[] { lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorld().getName() };
	}

	@Override
	protected void serialize(Map<String, Object> map){
		map.put("location", lc.serialize());
		map.put("radius", radius);
		if (!gps) map.put("gps", false);
	}

	public static StageLocation deserialize(Map<String, Object> map, QuestBranch branch) {
		return new StageLocation(branch, BQLocation.deserialize((Map<String, Object>) map.get("location")), (int) map.get("radius"), (boolean) map.getOrDefault("gps", true));
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
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			});
			line.setItem(SLOT_LOCATION, ItemUtils.item(XMaterial.STICK, Lang.stageLocationLocation.toString()), (p, item) -> {
				Lang.LOCATION_GO.send(p);
				new WaitClick(p, () -> reopenGUI(p, false), NPCGUI.validMove, () -> {
					setLocation(new BQLocation(p.getLocation()));
					reopenGUI(p, false);
				}).enter();
			});
			line.setItem(SLOT_WORLD_PATTERN, ItemUtils.item(XMaterial.NAME_TAG, Lang.stageLocationWorldPattern.toString(), QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.toString())), (p, item) -> {
				Lang.LOCATION_WORLDPATTERN.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, false), pattern -> {
					setPattern(pattern);
					reopenGUI(p, false);
				}, PatternParser.PARSER).passNullIntoEndConsumer().enter();
			});
			
			if (QuestsConfiguration.handleGPS()) line.setItem(SLOT_GPS, ItemUtils.itemSwitch(Lang.stageGPS.toString(), gps), (p, item) -> setGPS(ItemUtils.toggle(item)), true, true);
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
				if (QuestsConfiguration.handleGPS()) line.editItem(SLOT_GPS, ItemUtils.set(line.getItem(SLOT_GPS), gps));
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
			new WaitClick(p, removeAndReopen(p, false), NPCGUI.validMove, () -> {
				setLocation(p.getLocation());
				setRadius(5);
				reopenGUI(p, false);
			}).enter();
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
		public StageLocation finishStage(QuestBranch branch) {
			return new StageLocation(branch, getBQLocation(), radius, gps);
		}
		
	}
	
}
