package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.Message.Sender;

public class Dialog implements Cloneable {

	public List<Message> messages;
	public String npcName;
	
	public Dialog() {
		this(new ArrayList<>());
	}
	
	public Dialog(List<Message> messages) {
		this.messages = messages;
	}

	public String getNPCName(BQNPC defaultNPC) {
		if (npcName != null)
			return npcName;
		if (defaultNPC == null)
			return Lang.Unknown.toString();
		return defaultNPC.getName();
	}
	
	public void add(String msg, Sender sender){
		messages.add(new Message(msg, sender));
	}
	
	public void insert(String msg, Sender sender, int id){
		messages.add(id, new Message(msg, sender));
	}
	
	public void setNPCName(String npcName) {
		this.npcName = npcName;
	}
	
	@Override
	public Dialog clone() {
		Dialog clone = new Dialog();
		for (Message msg : messages) {
			clone.messages.add(msg.clone());
		}
		clone.npcName = npcName;
		return clone;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		List<Map<String, Object>> ls = new ArrayList<>();
		for (int i = 0; i < messages.size(); i++) {
			Map<String, Object> msgm = new HashMap<>();
			msgm.put("id", i);
			msgm.put("message", messages.get(i).serialize());
			ls.add(msgm);
		}
		map.put("msgs", ls);
		if (npcName != null) map.put("npcName", npcName);
		
		return map;
	}
	
	public static Dialog deserialize(Map<String, Object> map){
		
		NumberedList<Message> tmpMessages = new NumberedList<>();
		List<Map<String, Object>> serializedMessages = (List<Map<String, Object>>) map.get("msgs");
		for (Map<String, Object> msg : serializedMessages) {
			int id = (int) msg.get("id");
			tmpMessages.set(id, Message.deserialize((Map<String, Object>) msg.get("message")));
		}
		Dialog di = new Dialog(tmpMessages.toList());
		if (map.containsKey("npcName")) di.npcName = (String) map.get("npcName");
		
		return di;
	}

}
