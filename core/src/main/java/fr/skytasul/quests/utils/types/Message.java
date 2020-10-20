package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import fr.skytasul.quests.QuestsConfiguration;

public class Message implements Cloneable {
	public String text;
	public Sender sender;
	public String sound;
	public int wait = -1;
	
	public Message(String msg, Sender sender){
		this.text = msg;
		this.sender = sender;
	}
	
	public int getWaitTime() {
		return wait == -1 ? QuestsConfiguration.getDialogsDefaultTime() : wait;
	}

	@Override
	public Message clone() {
		Message clone = new Message(text, sender);
		clone.sound = sound;
		clone.wait = wait;
		return clone;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		map.put("text", text);
		map.put("sender", sender.name());
		if (sound != null) map.put("sound", sound);
		if (wait != -1) map.put("wait", wait);
		return map;
	}
	
	public static Message deserialize(Map<String, Object> map){
		Message msg = new Message((String) map.get("text"), Sender.fromString((String) map.get("sender")));
		if (map.containsKey("sound")) msg.sound = (String) map.get("sound");
		if (map.containsKey("wait")) msg.wait = (int) map.get("wait");
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