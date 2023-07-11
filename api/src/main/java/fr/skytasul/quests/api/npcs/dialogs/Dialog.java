package fr.skytasul.quests.api.npcs.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Message.Sender;
import fr.skytasul.quests.utils.types.NumberedList;

public class Dialog implements Cloneable {

	private @NotNull List<Message> messages;
	private @Nullable String npcName = null;
	private @Nullable Boolean skippable = null;
	
	public Dialog() {
		this(new ArrayList<>());
	}
	
	public Dialog(@NotNull List<Message> messages) {
		this.messages = messages;
	}

	public @NotNull List<Message> getMessages() {
		return messages;
	}

	public void setMessages(@NotNull List<Message> messages) {
		this.messages = messages;
	}

	public @NotNull String getNPCName(@Nullable BqNpc defaultNPC) {
		if (npcName != null)
			return npcName;
		if (defaultNPC == null)
			return Lang.Unknown.toString();
		return defaultNPC.getName();
	}
	
	public @Nullable String getNpcName() {
		return npcName;
	}

	public void setNpcName(@Nullable String npcName) {
		this.npcName = npcName;
	}

	public void add(String msg, Sender sender){
		messages.add(new Message(msg, sender));
	}
	
	public void insert(String msg, Sender sender, int id){
		messages.add(id, new Message(msg, sender));
	}
	
	public boolean isSkippable() {
		return skippable == null ? QuestsConfiguration.getDialogsConfig().isSkippableByDefault() : skippable.booleanValue();
	}
	
	public void setSkippable(@Nullable Boolean skippable) {
		this.skippable = skippable;
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
			tmpMessages.setSwitch(id, Message.deserialize((Map<String, Object>) msg.get("message")));
		}
		Dialog di = new Dialog(tmpMessages.toList());
		if (section.contains("npcName")) di.npcName = section.getString("npcName");
		if (section.contains("skippable")) di.skippable = section.getBoolean("skippable");
		
		return di;
	}

}
