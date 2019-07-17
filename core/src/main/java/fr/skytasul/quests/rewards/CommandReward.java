package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Command;

public class CommandReward extends AbstractReward {

	public final List<Command> commands = new ArrayList<>();
	
	public CommandReward(){
		super("commandReward");
	}

	public CommandReward(List<Command> list){
		this();
		if (list != null) this.commands.addAll(list);
	}

	public String give(Player p){
		if (commands.isEmpty()) return null;
		for (Command cmd : commands){
			cmd.execute(p);
		}
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("commands", Utils.serializeList(commands, Command::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		if (savedDatas.containsKey("command")){ // TODO: remove (edited since 0.14)
			commands.add(new Command((String) savedDatas.get("command"), (boolean) savedDatas.get("console")));
		}else {
			commands.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("commands"), Command::deserialize));
		}
	}

}
