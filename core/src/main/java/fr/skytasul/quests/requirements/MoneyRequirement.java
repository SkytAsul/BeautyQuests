package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyRequirement extends AbstractRequirement implements Actionnable {

	public double money = 0;

	public MoneyRequirement() {
		super("moneyRequired");
		if (!Dependencies.vault) throw new MissingDependencyException("Vault");
	}

	public MoneyRequirement(double money) {
		this();
		this.money = money;
	}

	public boolean test(Player p) {
		return Vault.has(p, money);
	}

	public void trigger(Player p) {
		Vault.withdrawPlayer(p, money);
	}

	public void sendReason(Player p) {
		Lang.REQUIREMENT_MONEY.send(p, Vault.format(money));
	}

	protected void save(Map<String, Object> datas) {
		datas.put("money", money);
	}

	protected void load(Map<String, Object> savedDatas) {
		money = (double) savedDatas.get("money");
	}

}
