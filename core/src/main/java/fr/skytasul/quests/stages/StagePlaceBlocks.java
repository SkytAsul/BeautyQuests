package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;

public class StagePlaceBlocks extends AbstractCountableStage<BQBlock> {
	
	public StagePlaceBlocks(QuestBranch branch, Map<Integer, Entry<BQBlock, Integer>> blocks) {
		super(branch, blocks);
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_PLACE.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMine(BlockPlaceEvent e) {
		if (e.isCancelled() || e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			event(acc, p, e.getBlock(), 1);
		}
	}
	
	@Override
	protected boolean objectApplies(BQBlock object, Object other) {
		if (other instanceof Block) return object.applies((Block) other);
		return super.objectApplies(object, other);
	}
	
	protected String getName(BQBlock object) {
		return MinecraftNames.getMaterialName(object.getMaterial());
	}

	protected Object serialize(BQBlock object) {
		return object.getAsString();
	}

	protected BQBlock deserialize(Object object) {
		return BQBlock.fromString((String) object);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		Map<Integer, Entry<BQBlock, Integer>> objects = new HashMap<>();

		StagePlaceBlocks stage = new StagePlaceBlocks(branch, objects);
		stage.deserialize(map);
		
		return stage;
	}

	public static class Creator implements StageCreationRunnables<StagePlaceBlocks> {
		public void start(Player p, LineData datas) {
			StagesGUI sg = datas.getGUI();
			BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
			blocks.run = (obj) -> {
				sg.reopen(p, true);
				datas.put("blocks", obj);
				setItems(datas.getLine(), datas);
			};
		}

		public StagePlaceBlocks finish(LineData datas, QuestBranch branch) {
			return new StagePlaceBlocks(branch, datas.get("blocks"));
		}

		public void edit(LineData datas, StagePlaceBlocks stage) {
			datas.put("blocks", stage.cloneObjects());
			setItems(datas.getLine(), datas);
		}

		public static void setItems(Line line, LineData datas) {
			line.setItem(6, ItemUtils.item(XMaterial.STONE, Lang.editBlocksPlace.toString()), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
					blocks.setBlocksFromMap(blocks.inv, datas.get("blocks"));
					blocks.run = (obj) -> {
						datas.getGUI().reopen(p, true);
						datas.put("blocks", obj);
					};
				}
			});
		}
	}

}
