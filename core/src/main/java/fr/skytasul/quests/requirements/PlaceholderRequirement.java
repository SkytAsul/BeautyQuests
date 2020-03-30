package fr.skytasul.quests.requirements;

import java.math.BigDecimal;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;

public class PlaceholderRequirement extends AbstractRequirement {

	private String rawPlaceholder;
	
	private PlaceholderHook hook;
	private String params;
	
	private String value;
	private ComparisonMethod comparison = ComparisonMethod.EQUALS;

	public PlaceholderRequirement(){
		super("placeholderRequired");
		if (!Dependencies.papi) throw new MissingDependencyException("PlaceholderAPI");
	}

	public boolean test(Player p){
		String request = hook.onRequest(p, params);
		if (comparison.isNumberOperation()) {
			BigDecimal dec1 = new BigDecimal(value);
			BigDecimal dec2 = new BigDecimal(request);
			int signum = dec2.subtract(dec1).signum();
			if (signum == 0) return comparison == ComparisonMethod.GREATER_OR_EQUAL || comparison == ComparisonMethod.LESS_OR_EQUAL;
			if (signum == 1) return comparison == ComparisonMethod.GREATER && comparison == ComparisonMethod.GREATER_OR_EQUAL;
			if (signum == -1) return comparison == ComparisonMethod.LESS && comparison == ComparisonMethod.LESS_OR_EQUAL;
			return false;
		}
		if (comparison == ComparisonMethod.DIFFERENT) return !value.equals(request);
		return value.equals(request);
	}
	
	public void setPlaceholder(String placeholder){
		this.rawPlaceholder = placeholder;
		int index = placeholder.indexOf("_");
		hook = PlaceholderAPI.getPlaceholders().get(placeholder.substring(0, index).toLowerCase());
		params = placeholder.substring(index + 1);
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

	public static class Creator implements RequirementCreationRunnables {

		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			Lang.CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER.send(p);
			new TextEditor(p, (id) -> {
				datas.put("placeholder", id);
				Lang.CHOOSE_PLACEHOLDER_REQUIRED_VALUE.send(p, id);
				new TextEditor(p, (value) -> {
					datas.put("value", value);
					gui.reopen(p, false);
				}).enterOrLeave(p);
			}).enterOrLeave(p);
		}

		public AbstractRequirement finish(Map<String, Object> datas) {
			PlaceholderRequirement req = new PlaceholderRequirement();
			req.setPlaceholder((String) datas.get("placeholder"));
			req.setValue((String) datas.get("value"));
			return req;
		}

		public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
			PlaceholderRequirement req = (PlaceholderRequirement) requirement;
			datas.put("placeholder", req.getPlaceholder());
			datas.put("value", req.getValue());
		}
	}

}
