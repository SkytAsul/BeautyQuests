package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyRequirement extends AbstractRequirement implements Actionnable {

	public double money = 0;

	public MoneyRequirement() {
		super("moneyRequired");
		if (!DependenciesManager.vault.isEnabled()) throw new MissingDependencyException("Vault");
	}

	public MoneyRequirement(double money) {
		this();
		this.money = money;
	}

	@Override
	public boolean test(Player p) {
		return Vault.has(p, money);
	}

	@Override
	public void trigger(Player p) {
		Vault.withdrawPlayer(p, money);
	}

	@Override
	public void sendReason(Player p) {
		Lang.REQUIREMENT_MONEY.send(p, Vault.format(money));
	}
	
	@Override
	public String getDescription(Player p) {
		return Vault.format(money);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new MoneyRequirement(money);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { Lang.optionValue.format(money), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_MONEY_REQUIRED.send(p);
		new TextEditor<>(p, () -> {
			if (money == 0) gui.remove(this);
			gui.reopen();
		}, obj -> {
			this.money = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, new NumberParser<>(Double.class, true, true)).enter();
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
