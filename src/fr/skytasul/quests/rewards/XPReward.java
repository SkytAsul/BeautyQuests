package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class XPReward extends AbstractReward {
	
	public int exp = 0;

	public XPReward(){
		super("expReward");
	}

	public XPReward(int exp){
		this();
		this.exp = exp;
	}

	public String give(Player p){
		if (Dependencies.skapi && QuestsConfiguration.xpOverridedSkillAPI()){
			SkillAPI.giveExp(p, exp);
		}else p.giveExp(exp);
		return exp + " " + Lang.Exp.toString();
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("xp", exp);
	}

	protected void load(Map<String, Object> savedDatas){
		exp = (int) savedDatas.get("xp");
	}

}
