package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyReward extends AbstractReward {

	public double money = 0;
	
	public MoneyReward(){
		super("moneyReward");
		if (!Dependencies.vault) throw new MissingDependencyException("Vault");
	}
	
	public MoneyReward(double money) {
		this();
		this.money = money;
	}

	public String give(Player p){
		if (money > 0){
			Vault.depositPlayer(p, money);
		}else Vault.withdrawPlayer(p, Math.abs(money));
		return Vault.format(money);
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("money", money);
	}

	protected void load(Map<String, Object> savedDatas){
		Object money = savedDatas.get("money");
		if (money instanceof Integer) { // TODO remove on 0.18
			this.money = ((Integer) money).doubleValue();
		}else this.money = (double) money;
	}

}
