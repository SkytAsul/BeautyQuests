package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.DialogSendMessageEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.Message.Sender;

public class Dialog implements Cloneable {

	public NumberedList<Message> messages;
	public String npcName;
	
	private Map<Player, PlayerStatus> players = new HashMap<>();
	
	public Dialog() {
		this(new NumberedList<>());
	}
	
	public Dialog(NumberedList<Message> messages) {
		this.messages = messages;
	}
	
	public void send(Player p, BQNPC npc, final Runnable end) {
		if (messages.isEmpty()) {
			end.run();
			return;
		}

		PlayerStatus status;
		if (players.containsKey(p)) {
			status = players.get(p);
			if (status.task != null) {
				if (QuestsConfiguration.isDialogClickDisabled()) return;
				status.task.cancel();
				status.task = null;
			}
		}else {
			status = new PlayerStatus();
			players.put(p, status);
		}
		int id = ++status.lastId;
		if (id == messages.valuesSize()) {
			players.remove(p);
			end.run();
			return;
		}

		Message msg = messages.get(id);
		if (msg == null){
			p.sendMessage("§cMessage with ID " + id + " does not exist. Please report this to an adminstrator. Method caller : " + DebugUtils.stackTraces(2, 3));
			players.remove(p);
			end.run();
			return;
		}
		DialogSendMessageEvent event = new DialogSendMessageEvent(this, msg, npc, p);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled()) msg.sendMessage(p, npcName != null ? npcName : (npc == null ? Lang.Unknown.toString() : npc.getName()), id, messages.valuesSize());
		if (msg.getWaitTime() != 0) {
			status.task = Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
				status.task = null;
				send(p, npc, end);
			}, msg.getWaitTime());
		}
	}
	
	public boolean isInDialog(Player p) {
		return players.containsKey(p);
	}

	public int getPlayerMessage(Player p) {
		return players.get(p).lastId;
	}
	
	public boolean remove(Player player) {
		PlayerStatus status = players.remove(player);
		if (status == null) return false;
		status.cancel();
		return true;
	}
	
	public void unload() {
		players.values().forEach(PlayerStatus::cancel);
		players.clear();
	}
	
	public void add(String msg, Sender sender){
		messages.add(new Message(msg, sender));
	}
	
	public void insert(String msg, Sender sender, int id){
		messages.insert(id, new Message(msg, sender));
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
		for (Entry<Integer, Message> en : messages.getOriginalMap().entrySet()){
			Map<String, Object> msgm = new HashMap<>();
			msgm.put("id", en.getKey());
			msgm.put("message", en.getValue().serialize());
			ls.add(msgm);
		}
		map.put("msgs", ls);
		if (npcName != null) map.put("npcName", npcName);
		
		return map;
	}
	
	public static Dialog deserialize(Map<String, Object> map){
		Dialog di = new Dialog();
		
		List<Map<String, Object>> ls = (List<Map<String, Object>>) map.get("msgs");
		for (Map<String, Object> tmp : ls){
			int id = (int) tmp.get("id");
			di.messages.set(id, Message.deserialize((Map<String, Object>) tmp.get("message")));
		}
		if (map.containsKey("npcName")) di.npcName = (String) map.get("npcName");
		
		return di;
	}
	
	class PlayerStatus {
		int lastId = -1;
		BukkitTask task = null;
		
		void cancel() {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}

}
