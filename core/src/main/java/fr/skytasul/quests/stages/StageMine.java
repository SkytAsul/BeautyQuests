package fr.skytasul.quests.stages;

import java.util.ArrayList;
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
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.BlockData;

public class StageMine extends AbstractStage {

	private List<BlockData> blocks = new ArrayList<>();
	private Map<PlayerAccount, List<BlockData>> remaining = new HashMap<>();
	private boolean placeCancelled;
	
	public StageMine(QuestBranch branch, List<BlockData> blocks) {
		super(branch);
		if (blocks != null) this.blocks = blocks;
	}

	public List<BlockData> getBlocks(){
		return blocks;
	}
	
	public boolean isPlaceCancelled() {
		return placeCancelled;
	}

	public void setPlaceCancelled(boolean cancelPlaced) {
		this.placeCancelled = cancelPlaced;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		String[] str = buildRemainingArray(acc, source);
		return Lang.SCOREBOARD_MINE.format(Utils.descriptionLines(source, str));
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		String[] str = buildRemainingArray(acc, source);
		return new String[]{str.length == 0 ? Lang.Unknown.toString() : Utils.itemsToFormattedString(str, QuestsConfiguration.getItemAmountColor())};
	}
	
	private String[] buildRemainingArray(PlayerAccount acc, Source source){
		String[] str = new String[remaining.get(acc).size()];
		List<BlockData> list = remaining.get(acc);
		for (int i = 0; i < list.size(); i++){
			BlockData b = list.get(i);
			str[i] = QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(MinecraftNames.getMaterialName(b.type), QuestsConfiguration.getItemAmountColor(), b.amount, QuestsConfiguration.showDescriptionItemsXOne(source));
		}
		return str;
	}
	
	@EventHandler
	public void onMine(BlockBreakEvent e){
		if (e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			if (placeCancelled && e.getBlock().hasMetadata("playerInStage")){
				System.out.println("metadata");
				if (e.getBlock().getMetadata("playerInStage").get(0).asString().equals(p.getName())) return;
			}
			List<BlockData> playerBlocks = remaining.get(acc);
			if (playerBlocks == null) {
				Lang.ERROR_OCCURED.send(p, "playerBlocks is null");
				return;
			}
			for (BlockData b : playerBlocks){
				if (b.type.isSameMaterial(e.getBlock())){
					b.amount--;
					if (b.amount == 0){
						playerBlocks.remove(b);
						break;
					}
				}
			}
			if (playerBlocks.isEmpty()){
				remaining.remove(acc);
				finishStage(p);
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		if (!placeCancelled) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!branch.hasStageLaunched(acc, this)) return;
		List<BlockData> playerBlocks = remaining.get(acc);
		if (playerBlocks == null) {
			Lang.ERROR_OCCURED.send(p, "playerBlocks is null");
			return;
		}
		for (BlockData b : playerBlocks){
			if (b.type.isSameMaterial(e.getBlock())){
				e.getBlock().setMetadata("playerInStage", new FixedMetadataValue(BeautyQuests.getInstance(), p.getName()));
				return;
			}
		}
	}
	
	private boolean remainingAdd(PlayerAccount acc){
		if (blocks.isEmpty() && acc.isCurrent()){
			Player p = acc.getPlayer();
			Utils.sendMessage(p, "§cThis stage doesn't need any blocks to mine... prevent an administrator :-)");
			finishStage(p);
			return false;
		}
		List<BlockData> list = new ArrayList<>();
		for (BlockData blc : blocks) {
			list.add(new BlockData(blc.type, blc.amount));
		}
		remaining.put(acc, list);
		return true;
	}
	
	public void start(PlayerAccount acc){
		if (!remaining.containsKey(acc)){
			if (acc.getOfflinePlayer().isOnline()) remainingAdd(acc);
		}
	}

	protected void serialize(Map<String, Object> map){
		map.put("blocks", Utils.serializeList(blocks, BlockData::serialize));
		
		Map<String, List<Map<String, Object>>> re = new HashMap<>();
		for (Entry<PlayerAccount, List<BlockData>> b : remaining.entrySet()){
			re.put(b.getKey().getIndex(), Utils.serializeList(blocks, BlockData::serialize));
		}
		map.put("remaining", re);
		if (placeCancelled) map.put("placeCancelled", placeCancelled);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		StageMine st = new StageMine(branch, Utils.deserializeList((List<Map<String, Object>>) map.get("blocks"), BlockData::deserialize));
		
		Utils.deserializeAccountsMap((Map<String, List<Map<String, Object>>>) map.get("remaining"), st.remaining, n -> Utils.deserializeList(n, BlockData::deserialize));
		
		if (map.containsKey("placeCancelled")) st.placeCancelled = (boolean) map.get("placeCancelled");
		
		return st;
	}

}
