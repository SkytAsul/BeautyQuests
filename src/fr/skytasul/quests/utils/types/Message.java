package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

public class Message{
	public String text;
	public Sender sender;
	public String sound;
	
	public Message(String msg, Sender sender){
		this.text = msg;
		this.sender = sender;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		map.put("text", text);
		map.put("sender", sender.name());
		if (sound != null) map.put("sound", sound);
		return map;
	}
	
	public static Message deserialize(Map<String, Object> map){
		/**Sender sender;
		if (map.containsKey("player")){
			sender = (boolean) map.get("player") ? Sender.PLAYER : Sender.NPC;
		}else sender = Sender.valueOf((String) map.get("sender"));*/
		Message msg = new Message((String) map.get("text"), Sender.valueOf((String) map.get("sender")));
		if (map.containsKey("sound")) msg.sound = (String) map.get("sound");
		return msg;
	}

	public static enum Sender{
		PLAYER, NPC, NOTHING;
	}
}