package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
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
		return new String[] { Lang.optionValue.format(money), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_MONEY_REWARD.send(p);
		new TextEditor<>(p, () -> {
			if (money == 0) gui.remove(this);
			gui.reopen();
		}, obj -> {
			money = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
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
