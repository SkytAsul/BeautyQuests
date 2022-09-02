package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Vault;

public class MoneyRequirement extends AbstractRequirement implements Actionnable {

	public double money = 0;
	
	public MoneyRequirement() {}

	public MoneyRequirement(double money) {
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
		return new String[] { Lang.optionValue.format(money), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_MONEY_REQUIRED.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			this.money = obj;
			event.reopenGUI();
		}, new NumberParser<>(Double.class, true, true)).enter();
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
