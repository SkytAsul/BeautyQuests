package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;

public class StageMine extends AbstractCountableStage<BQBlock> {

	private boolean placeCancelled;
	
	public StageMine(QuestBranch branch, Map<Integer, Entry<BQBlock, Integer>> blocks) {
		super(branch, blocks);
	}
	
	public boolean isPlaceCancelled() {
		return placeCancelled;
	}

	public void setPlaceCancelled(boolean cancelPlaced) {
		this.placeCancelled = cancelPlaced;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_MINE.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMine(BlockBreakEvent e){
		if (e.isCancelled() || e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			if (placeCancelled && e.getBlock().hasMetadata("playerInStage")){
				if (e.getBlock().getMetadata("playerInStage").get(0).asString().equals(p.getName())) return;
			}
			event(acc, p, e.getBlock(), 1);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e){
		if (e.isCancelled() || !placeCancelled) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!branch.hasStageLaunched(acc, this)) return;
		Map<Integer, Integer> playerBlocks = getPlayerRemainings(acc);
		for (Integer id : playerBlocks.keySet()) {
			if (objectApplies(super.objects.get(id).getKey(), e.getBlock())) {
				e.getBlock().setMetadata("playerInStage", new FixedMetadataValue(BeautyQuests.getInstance(), p.getName()));
				return;
			}
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

	protected void serialize(Map<String, Object> map){
		super.serialize(map);
		if (placeCancelled) map.put("placeCancelled", placeCancelled);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		Map<Integer, Entry<BQBlock, Integer>> objects = new HashMap<>();

		if (map.containsKey("blocks")) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("blocks");
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> blockData = list.get(i);
				objects.put(i, new AbstractMap.SimpleEntry<>(new BQBlock(XMaterial.valueOf((String) blockData.get("type"))), (int) blockData.get("amount")));
			}
		}

		StageMine stage = new StageMine(branch, objects);
		stage.deserialize(map);

		if (map.containsKey("remaining")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			((Map<String, List<Map<String, Object>>>) map.get("remaining")).forEach((acc, blocks) -> {
				Map<BQBlock, Integer> blocksMap = new HashMap<>();
				for (Map<String, Object> block : blocks) {
					blocksMap.put(new BQBlock(XMaterial.valueOf((String) block.get("type"))), (int) block.get("amount"));
				}
				stage.migrateDatas(migration.getByIndex(acc), blocksMap);
			});
		}

		if (map.containsKey("placeCancelled")) stage.placeCancelled = (boolean) map.get("placeCancelled");
		return stage;
	}

	public static class Creator extends StageCreation<StageMine> {
		
		private Map<Integer, Entry<BQBlock, Integer>> blocks;
		private boolean prevent = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.STONE_PICKAXE, Lang.editBlocksMine.toString()), (p, item) -> {
				BlocksGUI blocksGUI = Inventories.create(p, new BlocksGUI());
				blocksGUI.setBlocksFromMap(blocks);
				blocksGUI.run = obj -> {
					setBlocks(obj);
					reopenGUI(p, true);
				};
			});
			line.setItem(6, ItemUtils.itemSwitch(Lang.preventBlockPlace.toString(), prevent), (p, item) -> setPrevent(ItemUtils.toggle(item)));
		}
		
		public void setBlocks(Map<Integer, Entry<BQBlock, Integer>> blocks) {
			this.blocks = blocks;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(blocks.size() + " block(s)")));
		}
		
		public void setPrevent(boolean prevent) {
			if (this.prevent != prevent) {
				this.prevent = prevent;
				line.editItem(6, ItemUtils.set(line.getItem(6), prevent));
			}
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			BlocksGUI blocksGUI = Inventories.create(p, new BlocksGUI());
			blocksGUI.run = obj -> {
				setBlocks(obj);
				reopenGUI(p, true);
			};
		}

		@Override
		public void edit(StageMine stage) {
			super.edit(stage);
			setBlocks(stage.cloneObjects());
			setPrevent(stage.isPlaceCancelled());
		}
		
		@Override
		public StageMine finishStage(QuestBranch branch) {
			StageMine stage = new StageMine(branch, blocks);
			stage.setPlaceCancelled(prevent);
			return stage;
		}
	}

}
