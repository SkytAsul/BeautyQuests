package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyReward extends AbstractReward {

	public double money = 0;
	
	public MoneyReward(){
		super("moneyReward");
		if (!DependenciesManager.vault.isEnabled()) throw new MissingDependencyException("Vault");
	}
	
	public MoneyReward(double money) {
		this();
		this.money = money;
	}

	@Override
	public List<String> give(Player p) {
		if (money > 0){
			Vault.depositPlayer(p, money);
		}else Vault.withdrawPlayer(p, Math.abs(money));
		return Arrays.asList(Vault.format(money));
	}

	@Override
	public AbstractReward clone() {
		return new MoneyReward(money);
	}
	
	@Override
	public String getDescription(Player p) {
		return Vault.format(money);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(money), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_MONEY_REWARD.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), () -> {
			if (money == 0) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			money = obj;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, new NumberParser<>(Double.class, false, true)).enter();
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("money", money);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		money = (double) savedDatas.get("money");
	}
	
}
