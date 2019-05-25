package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Message.Sender;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Dialog{

	private NPC npc;
	
	public NumberedList<Message> messages;
	
	public Dialog(NPC npc){
		this.npc = npc;
		this.messages = new NumberedList<>();
	}
	
	public Dialog(NPC npc, NumberedList<Message> messages){
		this.npc = npc;
		this.messages = messages;
	}
	
	public void send(Player p, int id){
		Message msg = messages.get(id);
		if (msg == null){
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			p.sendMessage("§cMessage with ID " + id + " does not exist. Please report this to an adminstrator. Method caller : " + stack[1].getMethodName() + "." + stack[2].getMethodName());
			return;
		}
		String text = msg.text.replace("{PLAYER}", p.getName());
		switch(msg.sender){
		case PLAYER:
			Utils.sendSelfMessage(p, text, id+1, messages.valuesSize());
			break;
		case NPC:
			Utils.sendNPCMessage(p, text, npc, id+1, messages.valuesSize());
			break;
		case NOSENDER:
			Utils.IsendMessage(p, text, true);
			break;
		}
		if (msg.sound != null) p.playSound(p.getLocation(), msg.sound, 1, 1);
	}
	
	public boolean start(Player p){
		if (!messages.isEmpty()){
			send(p, 0);
			return true;
		}
		return false;
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
			/*if (tmp.containsKey("msg")){ // LATER SYSTEM
				di.messages.put(id,
						new Message((String) tmp.get("msg"), (boolean) map.get("player") ? Sender.PLAYER : Sender.NPC));
			}else */di.messages.set(id, Message.deserialize((Map<String, Object>) tmp.get("message")));
		}
		
		return di;
	}
	
}