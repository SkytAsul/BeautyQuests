package fr.skytasul.quests.stages;

import java.util.Collections;
import java.util.Spliterator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.blocks.BQBlock;
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
import fr.skytasul.quests.gui.blocks.SelectBlockGUI;

@LocatableType(types = LocatedType.BLOCK)
public class StageInteractBlock extends AbstractStage implements Locatable.MultipleLocatable {

	private final boolean left;
	private final @NotNull BQBlock block;
	
	public StageInteractBlock(StageController controller, boolean leftClick, BQBlock block) {
		super(controller);
		this.left = leftClick;
		this.block = block;
	}
	
	public BQBlock getBlockType() {
		return block;
	}

	public boolean needLeftClick(){
		return left;
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		if (block == null) return null;
		
		return QuestsAPI.getAPI().getBlocksManager().getNearbyBlocks(fetcher, Collections.singleton(block));
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (MinecraftVersion.MAJOR >= 9 && e.getHand() != EquipmentSlot.HAND) return;
		
		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if (!block.applies(e.getClickedBlock()))
			return;
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (left) e.setCancelled(true);
			finishStage(p);
		}
	}
	
	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.compose(block);
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_INTERACT_MATERIAL.toString();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("leftClick", left);
		section.set("block", block.getAsString());
	}
	
	public static StageInteractBlock deserialize(ConfigurationSection section, StageController controller) {
		BQBlock block;
		if (section.contains("material")) {
			block = QuestsAPI.getAPI().getBlocksManager().createSimple(XMaterial.valueOf(section.getString("material")),
					null);
		}else {
			block = QuestsAPI.getAPI().getBlocksManager().deserialize(section.getString("block"));
		}
		return new StageInteractBlock(controller, section.getBoolean("leftClick"), block);
	}

	public static class Creator extends StageCreation<StageInteractBlock> {
		
		private boolean leftClick = false;
		private BQBlock block;
		
		public Creator(@NotNull StageCreationContext<StageInteractBlock> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(6, ItemUtils.itemSwitch(Lang.leftClick.toString(), leftClick), event -> setLeftClick(!leftClick));
		}
		
		public void setLeftClick(boolean leftClick) {
			if (this.leftClick != leftClick) {
				this.leftClick = leftClick;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, leftClick));
			}
		}
		
		public void setMaterial(BQBlock block) {
			if (this.block == null) {
				getLine().setItem(7, ItemUtils.item(XMaterial.STICK, Lang.blockMaterial.toString()), event -> {
					new SelectBlockGUI(false, (newBlock, __) -> {
						setMaterial(newBlock);
						event.reopen();
					}).open(event.getPlayer());
				});
			}
			getLine().refreshItem(7, item -> ItemUtils.loreOptionValue(item, block.getName()));
			this.block = block;
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new SelectBlockGUI(false, (newBlock, __) -> {
				setMaterial(newBlock);
				context.reopenGui();
			}).open(p);
		}

		@Override
		public void edit(StageInteractBlock stage) {
			super.edit(stage);
			setMaterial(stage.getBlockType());
			setLeftClick(stage.needLeftClick());
		}

		@Override
		public StageInteractBlock finishStage(StageController controller) {
			return new StageInteractBlock(controller, leftClick, block);
		}
		
	}

}
