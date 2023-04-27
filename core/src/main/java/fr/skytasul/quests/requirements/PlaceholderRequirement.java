package fr.skytasul.quests.requirements;

import java.math.BigDecimal;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.utils.compatibility.QuestsPlaceholders;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderRequirement extends AbstractRequirement {

	private String rawPlaceholder;
	
	private PlaceholderExpansion hook;
	private String params;
	
	private String value;
	private ComparisonMethod comparison;
	private boolean parseValue = false;

	public PlaceholderRequirement(){
		this(null, null, null, null, ComparisonMethod.EQUALS);
	}
	
	public PlaceholderRequirement(String customDescription, String customReason, String placeholder, String value,
			ComparisonMethod comparison) {
		super(customDescription, customReason);
		if (placeholder != null) setPlaceholder(placeholder);
		this.value = value;
		this.comparison = comparison;
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
			value = MessageUtils.finalFormat(p, value, true);
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
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(rawPlaceholder));
		loreBuilder.addDescription(comparison.getTitle().format(value));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::cancel, id -> {
			setPlaceholder(id);
			Lang.CHOOSE_PLACEHOLDER_REQUIRED_VALUE.send(event.getPlayer(), id);
			new TextEditor<String>(event.getPlayer(), () -> {
				if (value == null) event.getGUI().remove(this);
				event.reopenGUI();
			}, value -> {
				this.value = value;
				try {
					new BigDecimal(value); // tests if the value is a number
					Lang.COMPARISON_TYPE.send(event.getPlayer(), ComparisonMethod.getComparisonParser().getNames(), ComparisonMethod.EQUALS.name().toLowerCase());
					new TextEditor<>(event.getPlayer(), null, comp -> {
						this.comparison = comp == null ? ComparisonMethod.EQUALS : comp;
						event.reopenGUI();
					}, ComparisonMethod.getComparisonParser()).passNullIntoEndConsumer().start();
				}catch (NumberFormatException __) {
					event.reopenGUI();
				}
			}).start();
		}).useStrippedMessage().start();
	}
	
	@Override
	public AbstractRequirement clone() {
		return new PlaceholderRequirement(getCustomDescription(), getCustomReason(), rawPlaceholder, value, comparison);
	}
	
}
