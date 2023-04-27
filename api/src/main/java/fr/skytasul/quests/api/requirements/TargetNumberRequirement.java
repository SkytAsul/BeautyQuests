package fr.skytasul.quests.api.requirements;

import java.text.NumberFormat;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.ComparisonMethod;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	protected ComparisonMethod comparison;
	protected double target;
	
	protected TargetNumberRequirement(@Nullable String customDescription, @Nullable String customReason, double target,
			@NotNull ComparisonMethod comparison) {
		super(customDescription, customReason);
		this.target = target;
		this.comparison = Objects.requireNonNull(comparison);
	}
	
	public double getTarget(){
		return target;
	}

	public @NotNull ComparisonMethod getComparisonMethod() {
		return comparison;
	}

	@Override
	public boolean test(@NotNull Player p) {
		double diff = getPlayerTarget(p) - target;
		return comparison.test(diff);
	}

	public @NotNull String getShortFormattedValue() {
		return comparison.getSymbol() + " " + getNumberFormat().format(target);
	}

	public @NotNull String getFormattedValue() {
		return comparison.getTitle().format(getNumberFormat().format(target));
	}

	protected @NotNull NumberFormat getNumberFormat() {
		return numberClass() == Integer.class ? NumberFormat.getIntegerInstance() : NumberFormat.getInstance();
	}

	@Override
	protected void addLore(@NotNull QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(getFormattedValue()));
	}

	public abstract double getPlayerTarget(@NotNull Player p);

	public abstract @NotNull Class<? extends Number> numberClass();
	
	public abstract void sendHelpString(@NotNull Player p);
	
	@Override
	public void save(@NotNull ConfigurationSection section) {
		super.save(section);
		section.set("comparison", comparison.name());
		section.set("target", target);
	}

	@Override
	public void load(@NotNull ConfigurationSection section) {
		super.load(section);
		if (section.contains("comparison")) comparison = ComparisonMethod.valueOf(section.getString("comparison"));
		target = section.getDouble("target");
	}
	
	@Override
	public void itemClick(@NotNull QuestObjectClickEvent event) {
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
			}, ComparisonMethod.getComparisonParser()).passNullIntoEndConsumer().start();
		}, event::remove, new NumberParser<>(numberClass(), true)).start();
	}

}