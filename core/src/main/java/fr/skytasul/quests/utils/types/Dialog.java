package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.Message.Sender;

public class Dialog implements Cloneable {

	public List<Message> messages;
	public String npcName = null;
	public Boolean skippable = null;
	
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
	
	public boolean isSkippable() {
		return skippable == null ? QuestsConfiguration.getDialogsConfig().isSkippableByDefault() : skippable.booleanValue();
	}
	
	public String getSkippableStatus() {
		String msg = isSkippable() ? Lang.Enabled.toString() : Lang.Disabled.toString();
		if (skippable == null) msg += " " + Lang.defaultValue.toString();
		return msg;
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
	
	public void serialize(ConfigurationSection section){
		List<Map<String, Object>> ls = new ArrayList<>();
		for (int i = 0; i < messages.size(); i++) {
			Map<String, Object> msgm = new HashMap<>();
			msgm.put("id", i);
			msgm.put("message", messages.get(i).serialize());
			ls.add(msgm);
		}
		section.set("msgs", ls);
		if (npcName != null) section.set("npcName", npcName);
		if (skippable != null) section.set("skippable", skippable);
	}
	
	public static Dialog deserialize(ConfigurationSection section) {
		NumberedList<Message> tmpMessages = new NumberedList<>();
		for (Map<?, ?> msg : section.getMapList("msgs")) {
			int id = (int) msg.get("id");
			tmpMessages.set(id, Message.deserialize((Map<String, Object>) msg.get("message")));
		}
		Dialog di = new Dialog(tmpMessages.toList());
		if (section.contains("npcName")) di.npcName = section.getString("npcName");
		if (section.contains("skippable")) di.skippable = section.getBoolean("skippable");
		
		return di;
	}

}
