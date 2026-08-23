package fr.skytasul.quests.api.requirements;

import java.text.NumberFormat;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.ComparisonMethod;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	protected ComparisonMethod comparison;
	protected double target;
	private @NotNull PlaceholderRegistry placeholders;
	
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

	protected @NotNull ComparisonMethod getDefaultComparisonMethod() {
		return ComparisonMethod.GREATER_OR_EQUAL;
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
		return comparison.getTitle().format(PlaceholderRegistry.of("number", getNumberFormat().format(target)));
	}

	protected @NotNull NumberFormat getNumberFormat() {
		return numberClass() == Integer.class ? NumberFormat.getIntegerInstance() : NumberFormat.getInstance();
	}

	protected @NotNull String getRawNumberAsString() {
		return numberClass() == Integer.class ? Integer.toString((int) target) : Double.toString(target);
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders
				.registerIndexed("short_" + getPlaceholderName(), this::getShortFormattedValue)
				.register("long_" + getPlaceholderName(), this::getFormattedValue)
				.register("raw_" + getPlaceholderName(), () -> getNumberFormat().format(target))
				.register(getPlaceholderName() + "_comparison", () -> Character.toString(comparison.getSymbol()));
	}

	@Override
	protected void addLore(@NotNull LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(getFormattedValue()));
	}

	protected abstract String getPlaceholderName();

	public abstract double getPlayerTarget(@NotNull Player p);

	public abstract @NotNull Class<? extends Number> numberClass();
	
	public abstract String getNumberIndication(@NotNull Player p);
	
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
	
	private void openTargetNumberEditor(@NotNull QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(event.getPlayer(),
				new NumberParser<>(numberClass(), true), () -> {
					if (target == 0)
						event.getGUI().remove(this);
					event.reopenGUI();
				}, number -> {
					this.target = number.doubleValue();
					openComparisonEditor(event);
				}).setIndication(getNumberIndication(event.getPlayer()))
				.setInitialString(event.isInCreation() ? null : getRawNumberAsString()).build().start();
	}

	private void openComparisonEditor(@NotNull QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderParser(event.getPlayer(), ComparisonMethod.getComparisonParser(),
						event::reopenGUI, comparison -> {
							this.comparison = comparison == null ? getDefaultComparisonMethod()
									: comparison;
							event.reopenGUI();
						})
				.setInitialValue(comparison).allowEmpty()
				.setIndication(
						Lang.COMPARISON_TYPE.quickFormat("default", getDefaultComparisonMethod().name()))
				.build().start();
	}

	@Override
	public void itemClick(@NotNull QuestObjectClickEvent event) {
		openTargetNumberEditor(event);
	}

}