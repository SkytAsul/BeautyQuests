package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Message.Sender;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Dialog{

	private NPC npc;
	public NumberedList<Message> messages;
	
	private Map<Player, PlayerStatus> players = new HashMap<>();
	
	public Dialog(NPC npc) {
		this(npc, new NumberedList<>());
	}
	
	public Dialog(NPC npc, NumberedList<Message> messages) {
		this.npc = npc;
		this.messages = messages;
	}
	
	public void send(Player p, final Runnable end) {
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
			p.sendMessage("§cMessage with ID " + id + " does not exist. Please report this to an adminstrator. Method caller : " + DebugUtils.stackTraces(1, 2));
			return;
		}
		String text = null;
		switch(msg.sender){
		case PLAYER:
			text = Utils.finalFormat(p, Lang.SelfText.format(p.getName(), msg.text, id+1, messages.valuesSize()), true);
			break;
		case NPC:
			text = Utils.finalFormat(p, Lang.NpcText.format(npc.getName(), msg.text, id+1, messages.valuesSize()), true);
			break;
		case NOSENDER:
			text = Utils.finalFormat(p, msg.text, true);
			break;
		}
		if (QuestsConfiguration.sendDialogsInActionBar()){
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text.replace("{nl}", " ")));
		}else p.sendMessage(StringUtils.splitByWholeSeparator(text, "{nl}"));
		if (msg.sound != null) p.playSound(p.getLocation(), msg.sound, 1, 1);
		if (msg.getWaitTime() != 0) {
			status.task = new BukkitRunnable() {
				public void run() {
					status.task = null;
					send(p, end);
				}
			}.runTaskLater(BeautyQuests.getInstance(), msg.getWaitTime());
		}
	}
	
	public boolean isInDialog(Player p) {
		return players.containsKey(p);
	}

	public boolean remove(Player player) {
		return players.remove(player) != null;
	}

	public NPC getNPC(){
		return npc;
	}
	
	public void setNPC(NPC npc){
		this.npc = npc;
	}
	
	public void add(String msg, Sender sender){
		messages.add(new Message(msg, sender));
	}
	
	public void insert(String msg, Sender sender, int id){
		messages.insert(id, new Message(msg, sender));
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("npc", npc.getId());
		
		List<Map<String, Object>> ls = new ArrayList<>();
		for (Entry<Integer, Message> en : messages.getOriginalMap().entrySet()){
			Map<String, Object> msgm = new HashMap<String, Object>();
			msgm.put("id", en.getKey());
			msgm.put("message", en.getValue().serialize());
			ls.add(msgm);
		}
		map.put("msgs", ls);
		
		return map;
	}
	
	public static Dialog deserialize(Map<String, Object> map){
		Dialog di = new Dialog(CitizensAPI.getNPCRegistry().getById((int) map.get("npc")));
		
		List<Map<String, Object>> ls = (List<Map<String, Object>>) map.get("msgs");
		for (Map<String, Object> tmp : ls){
			int id = (int) tmp.get("id");
			di.messages.set(id, Message.deserialize((Map<String, Object>) tmp.get("message")));
		}
		
		return di;
	}
	
	class PlayerStatus {
		int lastId;
		BukkitTask task;
	}

}