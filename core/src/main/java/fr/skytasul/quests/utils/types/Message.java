package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

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

	public void sendMessage(Player p, NPC npc, int id, int size) {
		String text = null;
		switch (sender) {
		case PLAYER:
			text = Utils.finalFormat(p, Lang.SelfText.format(p.getName(), text, id + 1, size), true);
			break;
		case NPC:
			text = Utils.finalFormat(p, Lang.NpcText.format(npc == null ? Lang.Unknown.toString() : npc.getName(), text, id + 1, size), true);
			break;
		case NOSENDER:
			text = Utils.finalFormat(p, text, true);
			break;
		}
		if (QuestsConfiguration.sendDialogsInActionBar()) {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text.replace("{nl}", " ")));
		}else p.sendMessage(StringUtils.splitByWholeSeparator(text, "{nl}"));
		
		if (sound != null) p.playSound(p.getLocation(), sound, 1, 1);
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