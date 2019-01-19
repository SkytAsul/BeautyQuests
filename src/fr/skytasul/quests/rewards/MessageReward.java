package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Utils;

public class MessageReward extends AbstractReward {

	public String text;
	
	public MessageReward(){
		super("textReward");
	}
	
	public MessageReward(String text){
		super("textReward");
		this.text = text;
	}

	public String give(Player p){
		Utils.sendOffMessage(p, text);
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("text", text);
	}

	protected void load(Map<String, Object> savedDatas){
		text = (String) savedDatas.get("text");
	}

}
