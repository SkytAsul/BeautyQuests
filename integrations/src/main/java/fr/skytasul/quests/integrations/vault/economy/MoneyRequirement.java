package fr.skytasul.quests.integrations.vault.economy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.integrations.vault.Vault;

public class MoneyRequirement extends AbstractRequirement implements Actionnable {

	public double money = 0;
	
	public MoneyRequirement() {}

	public MoneyRequirement(String customDescription, String customReason, double money) {
		super(customDescription, customReason);
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
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("money", Vault.format(money));
		placeholders.register("money_raw", money);
	}

	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_MONEY.format(this);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Vault.format(money);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new MoneyRequirement(getCustomDescription(), getCustomReason(), money);
	}
	
	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(money));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_MONEY_REQUIRED.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			this.money = obj;
			event.reopenGUI();
		}, NumberParser.DOUBLE_PARSER_STRICT_POSITIVE).start();
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("money", money);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		money = section.getDouble("money");
	}

}
