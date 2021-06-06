package fr.skytasul.quests.requirements;

import java.math.BigDecimal;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.QuestsPlaceholders;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderRequirement extends AbstractRequirement {

	private String rawPlaceholder;
	
	private PlaceholderExpansion hook;
	private String params;
	
	private String value;
	private ComparisonMethod comparison;

	public PlaceholderRequirement(){
		this(null, null, ComparisonMethod.EQUALS);
	}
	
	public PlaceholderRequirement(String placeholder, String value, ComparisonMethod comparison) {
		super("placeholderRequired");
		if (!DependenciesManager.papi.isEnabled()) throw new MissingDependencyException("PlaceholderAPI");
		if (placeholder != null) setPlaceholder(placeholder);
		this.value = value;
		this.comparison = comparison;
	}

	public boolean test(Player p){
		if (hook == null) return false;
		String request = hook.onRequest(p, params);
		if (comparison.isNumberOperation()) {
			BigDecimal dec1 = new BigDecimal(value);
			BigDecimal dec2 = new BigDecimal(request);
			int signum = dec2.subtract(dec1).signum();
			if (signum == 0) return comparison.isEqualOperation();
			if (signum == 1) return comparison == ComparisonMethod.GREATER || comparison == ComparisonMethod.GREATER_OR_EQUAL;
			if (signum == -1) return comparison == ComparisonMethod.LESS || comparison == ComparisonMethod.LESS_OR_EQUAL;
			return false;
		}
		if (comparison == ComparisonMethod.DIFFERENT) return !value.equals(request);
		return value.equals(request);
	}
	
	@Override
	public void sendReason(Player p) {
		if (hook == null) p.sendMessage("§cError: unknown placeholder " + rawPlaceholder);
	}
	
	public void setPlaceholder(String placeholder){
		this.rawPlaceholder = placeholder;
		int index = placeholder.indexOf("_");
		String identifier = placeholder.substring(0, index);
		hook = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
		params = placeholder.substring(index + 1);
		if (hook == null) {
			DebugUtils.logMessage("Cannot find PlaceholderAPI expansion for " + rawPlaceholder);
			QuestsPlaceholders.waitForExpansion(identifier, expansion -> {
				hook = expansion;
				DebugUtils.logMessage("Found " + rawPlaceholder + " from callback");
			});
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
	
	protected void save(Map<String, Object> datas){
		datas.put("placeholder", rawPlaceholder);
		datas.put("value", value);
		datas.put("comparison", comparison.name());
	}

	protected void load(Map<String, Object> savedDatas){
		setPlaceholder((String) savedDatas.get("placeholder"));
		this.value = (String) savedDatas.get("value");
		if (savedDatas.containsKey("comparison")) this.comparison = ComparisonMethod.valueOf((String) savedDatas.get("comparison"));
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + rawPlaceholder, "§8> §7" + comparison.getTitle().format(value), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER.send(p);
		new TextEditor<String>(p, () -> {
			if (rawPlaceholder == null) gui.remove(this);
			gui.reopen();
		}, id -> {
			setPlaceholder(id);
			Lang.CHOOSE_PLACEHOLDER_REQUIRED_VALUE.send(p, id);
			new TextEditor<String>(p, () -> {
				if (value == null) gui.remove(this);
				gui.reopen();
			}, value -> {
				this.value = value;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}).enter();
		}).useStrippedMessage().enter();
	}
	
	@Override
	public AbstractRequirement clone() {
		return new PlaceholderRequirement(rawPlaceholder, value, comparison);
	}
	
}
