package fr.skytasul.quests.api.requirements;

import java.text.NumberFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	protected ComparisonMethod comparison;
	protected double target;
	
	protected TargetNumberRequirement(String customDescription, String customReason, double target,
			ComparisonMethod comparison) {
		super(customDescription, customReason);
		this.target = target;
		this.comparison = comparison;
	}
	
	public double getTarget(){
		return target;
	}

	public ComparisonMethod getComparisonMethod() {
		return comparison;
	}

	@Override
	public boolean test(Player p) {
		double diff = getPlayerTarget(p) - target;
		return comparison.test(diff);
	}

	public String getShortFormattedValue() {
		return comparison.getSymbol() + " " + getNumberFormat().format(target);
	}

	public String getFormattedValue() {
		return comparison.getTitle().format(getNumberFormat().format(target));
	}

	protected NumberFormat getNumberFormat() {
		return numberClass() == Integer.class ? NumberFormat.getIntegerInstance() : NumberFormat.getInstance();
	}

	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(getFormattedValue()));
	}

	public abstract double getPlayerTarget(Player p);

	public abstract Class<? extends Number> numberClass();
	
	public abstract void sendHelpString(Player p);
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("comparison", comparison.name());
		section.set("target", target);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		if (section.contains("comparison")) comparison = ComparisonMethod.valueOf(section.getString("comparison"));
		target = section.getDouble("target");
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		sendHelpString(event.getPlayer());
		new TextEditor<>(event.getPlayer(), () -> {
			if (target == 0) event.getGUI().remove(this);
			event.reopenGUI();
		}, number -> {
			target = number.doubleValue();
			Lang.COMPARISON_TYPE.send(event.getPlayer(), ComparisonMethod.getComparisonParser().getNames(), ComparisonMethod.GREATER_OR_EQUAL.name().toLowerCase());
			new TextEditor<>(event.getPlayer(), null, comp -> {
				this.comparison = comp == null ? ComparisonMethod.GREATER_OR_EQUAL : comp;
				event.reopenGUI();
			}, ComparisonMethod.getComparisonParser()).passNullIntoEndConsumer().enter();
		}, event::remove, new NumberParser<>(numberClass(), true)).enter();
	}

}