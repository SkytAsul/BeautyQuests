package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyReward extends AbstractReward {

	public double money = 0;
	
	public MoneyReward(){
		super("moneyReward");
		if (!DependenciesManager.vault) throw new MissingDependencyException("Vault");
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

	@Override
	public AbstractReward clone() {
		return new MoneyReward(money);
	}
	
	@Override
	protected String[] getLore() {
		return new String[] { Lang.optionValue.format(money), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, RewardsGUI gui, ItemStack clicked) {
		Lang.CHOOSE_MONEY_REWARD.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			money = (double) obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen(p);
		}, new NumberParser(Double.class, false, true)));
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("money", money);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		money = (double) savedDatas.get("money");
	}
	
}
