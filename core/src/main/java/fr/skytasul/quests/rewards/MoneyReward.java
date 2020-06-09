package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
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

	
	protected void save(Map<String, Object> datas){
		datas.put("money", money);
	}

	protected void load(Map<String, Object> savedDatas){
		Object money = savedDatas.get("money");
		if (money instanceof Integer) { // TODO remove on 0.18
			this.money = ((Integer) money).doubleValue();
		}else this.money = (double) money;
	}

	public static class Creator implements RewardCreationRunnables<MoneyReward> {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			Lang.CHOOSE_MONEY_REWARD.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				datas.put("money", (double) obj);
				gui.reopen(p, false);
				ItemUtils.lore(clicked, "Money : " + obj);
			}, new NumberParser(Double.class, false, true)));
		}

		public void edit(Map<String, Object> datas, MoneyReward reward, ItemStack is) {
			datas.put("money", reward.money);
			ItemUtils.lore(is, "Money : " + reward.money);
		}

		public MoneyReward finish(Map<String, Object> datas) {
			return new MoneyReward((double) datas.get("money"));
		}

	}

}
