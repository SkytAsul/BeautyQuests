package fr.skytasul.quests.integrations.skillapi;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class SkillAPIXpReward extends AbstractReward {

	public int exp = 0;

	public SkillAPIXpReward() {}

	public SkillAPIXpReward(String customDescription, int exp) {
		super(customDescription);
		this.exp = exp;
	}

	@Override
	public List<String> give(Player p) {
		SkillAPI.giveExp(p, exp);
		return Arrays.asList(getXpAmountString());
	}

	@Override
	public AbstractReward clone() {
		return new SkillAPIXpReward(getCustomDescription(), exp);
	}

	@Override
	public String getDefaultDescription(Player p) {
		return getXpAmountString();
	}

	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
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
