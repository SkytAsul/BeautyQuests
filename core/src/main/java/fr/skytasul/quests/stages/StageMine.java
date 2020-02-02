package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;

public class StageMine extends AbstractCountableStage<XMaterial> {

	private boolean placeCancelled;
	
	public StageMine(QuestBranch branch, Map<Integer, Entry<XMaterial, Integer>> blocks) {
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
	
	@EventHandler
	public void onMine(BlockBreakEvent e){
		if (e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			if (placeCancelled && e.getBlock().hasMetadata("playerInStage")){
				if (e.getBlock().getMetadata("playerInStage").get(0).asString().equals(p.getName())) return;
			}
			event(acc, p, XMaterial.fromMaterial(e.getBlock().getType()), 1);
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		if (!placeCancelled) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!branch.hasStageLaunched(acc, this)) return;
		Map<Integer, Integer> playerBlocks = getPlayerRemainings(acc);
		for (Integer id : playerBlocks.keySet()) {
			if (super.objects.get(id).getKey().isSameMaterial(e.getBlock())) {
				e.getBlock().setMetadata("playerInStage", new FixedMetadataValue(BeautyQuests.getInstance(), p.getName()));
				return;
			}
		}
	}

	protected String getName(XMaterial object) {
		return MinecraftNames.getMaterialName(object);
	}

	protected Object serialize(XMaterial object) {
		return object.name();
	}

	protected XMaterial deserialize(Object object) {
		return XMaterial.valueOf((String) object);
	}

	protected void serialize(Map<String, Object> map){
		super.serialize(map);
		if (placeCancelled) map.put("placeCancelled", placeCancelled);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		Map<Integer, Entry<XMaterial, Integer>> objects = new HashMap<>();

		if (map.containsKey("blocks")) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("blocks");
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> blockData = list.get(i);
				objects.put(i, new AbstractMap.SimpleEntry<>(XMaterial.valueOf((String) map.get("type")), (int) blockData.get("amount")));
			}
		}

		StageMine stage = new StageMine(branch, objects);
		stage.deserialize(map);

		if (map.containsKey("remaining")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			((Map<String, List<Map<String, Object>>>) map.get("remaining")).forEach((acc, blocks) -> {
				Map<XMaterial, Integer> blocksMap = new HashMap<>();
				for (Map<String, Object> block : blocks) {
					blocksMap.put(XMaterial.valueOf((String) block.get("type")), (int) block.get("amount"));
				}
				stage.migrateDatas(migration.getByIndex(acc), blocksMap);
			});
		}

		if (map.containsKey("placeCancelled")) stage.placeCancelled = (boolean) map.get("placeCancelled");
		return stage;
	}

}
