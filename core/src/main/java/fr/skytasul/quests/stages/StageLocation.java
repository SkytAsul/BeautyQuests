package fr.skytasul.quests.stages;

import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.editors.parsers.PatternParser;
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
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.npc.NpcCreateGUI;
import fr.skytasul.quests.utils.types.BQLocation;

@LocatableType (types = LocatedType.OTHER)
public class StageLocation extends AbstractStage implements Locatable.PreciseLocatable, Listener {

	private final BQLocation lc;
	private final int radius;
	private final int radiusSquared;

	public StageLocation(StageController controller, BQLocation lc, int radius) {
		super(controller);
		this.lc = lc;
		this.radius = radius;
		this.radiusSquared = radius * radius;
	}

	public BQLocation getLocation() {
		return lc;
	}

	@Override
	public Located getLocated() {
		return lc;
	}

	public int getRadius(){
		return radius;
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
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("target_x", lc.getBlockX());
		placeholders.registerIndexed("target_y", lc.getBlockY());
		placeholders.registerIndexed("target_z", lc.getBlockZ());
		placeholders.registerIndexed("target_world", lc.getWorldName());
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_LOCATION.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("location", lc.serialize());
		section.set("radius", radius);
	}

	public static StageLocation deserialize(ConfigurationSection section, StageController controller) {
		return new StageLocation(controller,
				BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)),
				section.getInt("radius"));
	}

	public static class Creator extends StageCreation<StageLocation> {

		private static final int SLOT_RADIUS = 6;
		private static final int SLOT_LOCATION = 7;
		private static final int SLOT_WORLD_PATTERN = 8;

		private Location location;
		private Pattern pattern;
		private int radius;

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
		}

		public void setLocation(Location location) {
			this.location = location;
			getLine().refreshItem(SLOT_LOCATION,
					item -> ItemUtils.loreOptionValue(item, Lang.Location.format(getBQLocation())));
		}

		public void setRadius(int radius) {
			this.radius = radius;
			getLine().refreshItem(SLOT_RADIUS,
					item -> ItemUtils.lore(item, Lang.stageLocationCurrentRadius.quickFormat("radius", radius)));
		}

		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
			getLine().refreshItem(SLOT_WORLD_PATTERN,
					item -> ItemUtils.lore(item, QuestOption.formatDescription(Lang.stageLocationWorldPatternLore.format()),
							"",
							pattern == null ? Lang.NotSet.toString() : QuestOption.formatNullableValue(pattern.pattern())));
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
		}

		@Override
		public StageLocation finishStage(StageController controller) {
			return new StageLocation(controller, getBQLocation(), radius);
		}

	}

}
