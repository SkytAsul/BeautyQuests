package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyReward extends AbstractReward {

	public double money = 0;
	
	public MoneyReward() {}
	
	public MoneyReward(double money) {
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
	public void save(ConfigurationSection section) {
		section.set("money", money);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		money = section.getDouble("money");
	}
	
}
