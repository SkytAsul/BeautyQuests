package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.BlockData;

public class StageMine extends AbstractStage {

	private List<BlockData> blocks = new ArrayList<>();
	private Map<PlayerAccount, List<BlockData>> remaining = new HashMap<>();
	
	public StageMine(StageManager manager, List<BlockData> blocks) {
		super(manager);
		if (blocks != null) this.blocks = blocks;
	}

	public List<BlockData> getBlocks(){
		return blocks;
	}
	
	public String descriptionLine(PlayerAccount acc){
		String[] str = new String[remaining.get(acc).size()];
		List<BlockData> list = remaining.get(acc);
		for (int i = 0; i < list.size(); i++){
			BlockData b = list.get(i);
			str[i] = QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(MinecraftNames.getMaterialName(b.type), QuestsConfiguration.getItemAmountColor(), b.amount);
		}
		return Lang.SCOREBOARD_MINE.format((str.length != 0) ? Utils.itemsToFormattedString(str, QuestsConfiguration.getItemAmountColor()) : Lang.Unknown.toString());
	}
	
	protected String descriptionMenu(PlayerAccount acc){
		String str = "";
		List<BlockData> list = remaining.get(acc);
		for (int i = 0; i < list.size(); i++){
			BlockData b = list.get(i);
			str = str + "\\n" + QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(MinecraftNames.getMaterialName(b.type), QuestsConfiguration.getItemAmountColor(), b.amount);
		}
		return Lang.SCOREBOARD_MINE.format(list.isEmpty() ? Lang.Unknown.toString() : "") + str;
	}
	
	@EventHandler
	public void onMine(BlockBreakEvent e){
		if (e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (manager.hasStageLaunched(acc, this)){
			List<BlockData> playerBlocks = remaining.get(acc);
			if (playerBlocks == null) {
				Utils.sendMessage(p, "Error : playerBlocks is null");
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
	
	private boolean remainingAdd(PlayerAccount acc){
		if (blocks.isEmpty() && acc.isCurrent()){
			Player p = acc.getPlayer();
			Utils.sendMessage(p, "§cThis stage doesn't need any blocks to mine... prevent an administrator :-)");
			manager.next(p);
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

	protected Map<String, Object> serialize(Map<String, Object> map){
		map.put("blocks", fromBlocks(blocks));
		
		Map<String, List<Map<String, Object>>> re = new HashMap<>();
		for (Entry<PlayerAccount, List<BlockData>> b : remaining.entrySet()){
			re.put(b.getKey().getIndex(), fromBlocks(b.getValue()));
		}
		map.put("remaining", re);
		return map;
	}
	
	private static List<Map<String, Object>> fromBlocks(List<BlockData> blocks){
		List<Map<String, Object>> sblocks = new ArrayList<>();
		for (BlockData b : blocks){
			sblocks.add(b.serialize());
		}
		return sblocks;
	}
	
	private static List<BlockData> fromMapList(List<Map<String, Object>> ls){
		List<BlockData> t = new ArrayList<>();
		for (Map<String, Object> b : ls){
			BlockData block = BlockData.deserialize(b);
			if (block != null) t.add(block);
		}
		return t;
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		StageMine st = new StageMine(manager, fromMapList((List<Map<String, Object>>) map.get("blocks")));
		
		Map<String, List<Map<String, Object>>> re = (Map<String, List<Map<String, Object>>>) map.get("remaining");
		if (re != null){
			for (Entry<String, List<Map<String, Object>>> en : re.entrySet()){
				st.remaining.put(PlayersManager.getByIndex(en.getKey()), fromMapList(en.getValue()));
			}
		}
		
		return st;
	}

}
