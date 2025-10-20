package fr.skytasul.quests.integrations.vault.economy;

import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardGiveContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.integrations.vault.Vault;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MoneyReward extends AbstractReward {

	public double money = 0;

	public MoneyReward() {}

	public MoneyReward(String customDescription, double money) {
		super(customDescription);
		this.money = money;
	}

	@Override
	public void give(RewardGiveContext context) {
		for (Player player : context.getQuester().getOnlinePlayers()) {
			if (money > 0) {
				Vault.depositPlayer(player, money);
			} else
				Vault.withdrawPlayer(player, Math.abs(money));
			context.addEarning(player, this);
		}
	}

	@Override
	public AbstractReward clone() {
		return new MoneyReward(getCustomDescription(), money);
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.register("money", Vault.format(money));
		placeholders.register("money_raw", money);
	}

	@Override
	public String getDefaultDescription(Player p) {
		return Vault.format(money);
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(money);
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_MONEY_REWARD.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			money = obj;
			event.reopenGUI();
		}, new NumberParser<>(Double.class, false, true)).start();
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
