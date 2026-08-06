package fr.skytasul.quests.integrations.placeholders;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.math.BigDecimal;

public class PlaceholderRequirement extends AbstractRequirement {

	private static final ComparisonMethod DEFAULT_COMPARISON_METHOD = ComparisonMethod.EQUALS;

	private String rawPlaceholder;

	private PlaceholderExpansion hook;
	private String params;

	private String value;
	private ComparisonMethod comparison;
	private boolean parseValue = false;

	public PlaceholderRequirement(){
		this(null, null, null, null, DEFAULT_COMPARISON_METHOD);
	}

	public PlaceholderRequirement(String customDescription, String customReason, String placeholder, String value,
			ComparisonMethod comparison) {
		super(customDescription, customReason);
		if (placeholder != null) setPlaceholder(placeholder);
		this.value = value;
		this.comparison = comparison;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("placeholder", () -> rawPlaceholder);
		placeholders.register("target_value", () -> value);
		placeholders.register("comparison", () -> Character.toString(comparison.getSymbol()));
		placeholders.register("parse_value", () -> Boolean.toString(parseValue));
	}

	@Override
	public boolean test(Player p){
		if (hook == null) return false;
		String request = hook.onRequest(p, params);
		if (comparison.isNumberOperation()) {
			BigDecimal dec1 = new BigDecimal(value);
			try {
				BigDecimal dec2 = new BigDecimal(request);
				int signum = dec2.subtract(dec1).signum();
				if (signum == 0) return comparison.isEqualOperation();
				if (signum == 1) return comparison == ComparisonMethod.GREATER || comparison == ComparisonMethod.GREATER_OR_EQUAL;
				if (signum == -1) return comparison == ComparisonMethod.LESS || comparison == ComparisonMethod.LESS_OR_EQUAL;
			}catch (NumberFormatException e) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot parse placeholder " + rawPlaceholder + " for player " + p.getName() + ": got " + request + ", which is not a number. (" + debugName() + ")");
			}
			return false;
		}
		if (comparison == ComparisonMethod.DIFFERENT) return !value.equals(request);
		String value = this.value;
		if (parseValue)
			value = MessageUtils.finalFormat(value, null, PlaceholdersContext.of(p, true, null));
		return value.equals(request);
	}

	@Override
	public boolean isValid() {
		return hook != null;
	}

	@Override
	protected String getInvalidReason() {
		return "unknown placeholder " + rawPlaceholder;
	}

	public void setPlaceholder(String placeholder){
		this.rawPlaceholder = placeholder;
		int index = placeholder.indexOf("_");
		if (index == -1) {
			hook = null;
			params = placeholder;
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Usage of invalid placeholder " + placeholder);
		}else {
			String identifier = placeholder.substring(0, index);
			hook = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
			params = placeholder.substring(index + 1);
			if (hook == null) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot find PlaceholderAPI expansion for " + rawPlaceholder);
				QuestsPlaceholders.waitForExpansion(identifier, expansion -> {
					hook = expansion;
					QuestsPlugin.getPlugin().getLoggerExpanded().debug("Found " + rawPlaceholder + " from callback");
				});
			}
		}
	}

	public void setValue(String value){
		this.value = value;
	}

	public String getPlaceholder(){
		return rawPlaceholder;
	}

	public String getValue(){
		return value;
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("placeholder", rawPlaceholder);
		section.set("value", value);
		section.set("comparison", comparison.name());
		section.set("parseValue", parseValue);
	}

	@Override
	public void load(ConfigurationSection section){
		super.load(section);
		setPlaceholder(section.getString("placeholder"));
		this.value = section.getString("value");
		if (section.contains("comparison")) this.comparison = ComparisonMethod.valueOf(section.getString("comparison"));
		if (section.contains("parseValue")) this.parseValue = section.getBoolean("parseValue");
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(rawPlaceholder));
		loreBuilder.addDescription(comparison.getTitle().quickFormat("number", value));
	}

	private void openIdentifierEditor(@NotNull QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderString(event.getPlayer(), event::cancel, id -> {
					setPlaceholder(id);
					openTargetValueEditor(event);
				}).setIndication(Lang.CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER.toString())
				.setInitialString(rawPlaceholder).build().start();
		// XXX: no stripped message support here
	}

	private void openTargetValueEditor(@NotNull QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderString(event.getPlayer(), event::cancel, value -> {
					this.value = value;
					try {
						new BigDecimal(value); // tests if the value is a number
						openComparisonEditor(event);
					} catch (NumberFormatException __) {
						event.reopenGUI();
					}
				}).setIndication(Lang.CHOOSE_PLACEHOLDER_REQUIRED_VALUE.toString())
				.setInitialString(value).build().start();
	}

	private void openComparisonEditor(@NotNull QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderParser(event.getPlayer(), ComparisonMethod.getComparisonParser(),
						event::reopenGUI, comparison -> {
							this.comparison = comparison == null ? DEFAULT_COMPARISON_METHOD : comparison;
							event.reopenGUI();
						})
				.setInitialString(comparison.name()).allowEmpty()
				.setIndication(Lang.COMPARISON_TYPE.quickFormat("default", DEFAULT_COMPARISON_METHOD.name()))
				.build().start();
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		openIdentifierEditor(event);
	}

	@Override
	public AbstractRequirement clone() {
		return new PlaceholderRequirement(getCustomDescription(), getCustomReason(), rawPlaceholder, value, comparison);
	}

}
