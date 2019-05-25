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
		Message msg = new Message((String) map.get("text"), Sender.fromString((String) map.get("sender")));
		if (map.containsKey("sound")) msg.sound = (String) map.get("sound");
		return msg;
	}

	public static enum Sender{
		PLAYER, NPC, NOSENDER;
		
		public static Sender fromString(String string){
			if (string.equalsIgnoreCase("NOTHING")) return NOSENDER;
			return valueOf(string.toUpperCase());
		}
	}
}