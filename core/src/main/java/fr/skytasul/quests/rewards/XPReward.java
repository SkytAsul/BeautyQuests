package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardGiveContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class XPReward extends AbstractReward {

	public int exp = 0;

	public XPReward() {}

	public XPReward(String customDescription, int exp) {
		super(customDescription);
		this.exp = exp;
	}

	@Override
	public void give(RewardGiveContext context) {
		for (Player player : context.getQuester().getOnlinePlayers())
			player.giveExp(exp);
		context.addEarning(this);
	}

	@Override
	public AbstractReward clone() {
		return new XPReward(getCustomDescription(), exp);
	}

	@Override
	public String getDefaultDescription(Player p) {
		return getXpAmountString();
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(getXpAmountString());
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("xp_amount", this::getXpAmountString);
	}

	private @NotNull String getXpAmountString() {
		return Lang.AmountXp.quickFormat("xp_amount", exp);
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.XP_GAIN.send(event.getPlayer(), this);
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			int old = exp;
			exp = obj;
			Lang.XP_EDITED.send(event.getPlayer(), PlaceholderRegistry.of("old_xp_amount", old).with(this));
			event.reopenGUI();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("xp", exp);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		exp = section.getInt("xp");
	}

}
