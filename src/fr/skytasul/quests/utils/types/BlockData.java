package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import fr.skytasul.quests.utils.XMaterial;

public class BlockData {

	public XMaterial type;
	public int amount;
	
	public BlockData(XMaterial type, int amount){
		this.type = type;
		this.amount = amount;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("amount", amount);
		map.put("type", type.name());
		
		return map;
	}
	
	public static BlockData deserialize(Map<String, Object> map){
		return new BlockData(XMaterial.valueOf((String) map.get("type")), (int) map.get("amount"));
	}
	
}
