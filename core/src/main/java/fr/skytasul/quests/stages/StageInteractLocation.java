package fr.skytasul.quests.stages;

import fr.skytasul.quests.api.editors.WaitBlockClick;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.utils.types.BQLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

@LocatableType (types = { LocatedType.BLOCK, LocatedType.OTHER })
public class StageInteractLocation extends AbstractStage implements Locatable.PreciseLocatable, Listener {

	private final boolean left;
	private final @NotNull BQLocation lc;

	private Located.LocatedBlock locatedBlock;

	public StageInteractLocation(@NotNull StageController controller, boolean leftClick, @NotNull BQLocation location) {
		super(controller);
		this.left = leftClick;
		this.lc = new BQLocation(location.getWorldName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public @NotNull BQLocation getLocation() {
		return lc;
	}

	public boolean needLeftClick(){
		return left;
	}

	@Override
	public Located getLocated() {
		if (lc == null)
			return null;
		if (locatedBlock == null) {
			Block realBlock = lc.getMatchingBlock();
			if (realBlock != null)
				locatedBlock = Located.LocatedBlock.create(realBlock);
		}
		return locatedBlock;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (MinecraftVersion.MAJOR >= 9 && e.getHand() != EquipmentSlot.HAND) return;

		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		if (!lc.equals(e.getClickedBlock().getLocation()))
			return;

		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			e.setCancelled(true);
			finishStage(p);
		}
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.compose(lc);

		// TODO migration 1.0
		placeholders.registerIndexed("location", lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_INTERACT_LOCATION.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("leftClick", left);
		section.set("location", lc.serialize());
	}

	public static StageInteractLocation deserialize(ConfigurationSection section, StageController controller) {
		return new StageInteractLocation(controller, section.getBoolean("leftClick"),
				BQLocation.deserialize(section.getConfigurationSection("location").getValues(false)));
	}

	public static class Creator extends StageCreation<StageInteractLocation> {

		private boolean leftClick = false;
		private BQLocation location;

		public Creator(@NotNull StageCreationContext<StageInteractLocation> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(6, ItemUtils.itemSwitch(Lang.leftClick.toString(), leftClick), event -> setLeftClick(!leftClick));
			line.setItem(7, ItemUtils.item(XMaterial.COMPASS, Lang.blockLocation.toString()), event -> {
				Lang.CLICK_BLOCK.send(event.getPlayer());
				new WaitBlockClick(event.getPlayer(), event::reopen, obj -> {
					setLocation(obj);
					event.reopen();
				}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
			});
		}

		public void setLeftClick(boolean leftClick) {
			if (this.leftClick != leftClick) {
				this.leftClick = leftClick;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, leftClick));
			}
		}

		public void setLocation(@NotNull Location location) {
			this.location = BQLocation.of(Objects.requireNonNull(location));
			getLine().refreshItemLoreOptionValue(7, Lang.Location.format(this.location));
		}

		@Override
		public void start(Player p) {
			super.start(p);
			Lang.CLICK_BLOCK.send(p);
			new WaitBlockClick(p, context::removeAndReopenGui, obj -> {
				setLocation(obj);
				context.reopenGui();
			}, ItemUtils.item(XMaterial.STICK, Lang.blockLocation.toString())).start();
		}

		@Override
		public void edit(StageInteractLocation stage) {
			super.edit(stage);
			setLocation(stage.getLocation());
			setLeftClick(stage.needLeftClick());
		}

		@Override
		public StageInteractLocation finishStage(StageController controller) {
			return new StageInteractLocation(controller, leftClick, location);
		}

	}

}
