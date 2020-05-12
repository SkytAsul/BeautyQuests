package fr.skytasul.quests.api.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.EnumParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	protected ComparisonMethod comparison = ComparisonMethod.GREATER_OR_EQUAL;
	protected double target;

	public TargetNumberRequirement(String name) {
		super(name);
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

	public abstract double getPlayerTarget(Player p);

	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("comparison", comparison.name());
		datas.put("target", target);
	}

	@Override
	protected void load(Map<String, Object> savedDatas) {
		if (savedDatas.containsKey("comparison")) comparison = ComparisonMethod.valueOf((String) savedDatas.get("comparison"));
		target = Utils.parseDouble(savedDatas.get("target"));
	}

	public static abstract class Creator<T extends TargetNumberRequirement> implements RequirementCreationRunnables {

		private static final EnumParser<ComparisonMethod> COMPARISON_PARSER = new EnumParser<>(ComparisonMethod.class);

		@Override
		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			sendHelpString(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				if (datas.containsKey("target")) datas.remove("target");
				datas.put("target", ((Number) obj).doubleValue());
				Lang.COMPARISON_TYPE.send(p, COMPARISON_PARSER.getNames());
				new TextEditor(p, (comp) -> {
					datas.put("comparison", comp);
					gui.reopen(p, true);
				}, COMPARISON_PARSER, null, () -> {
					datas.put("comparison", ComparisonMethod.GREATER_OR_EQUAL);
					gui.reopen(p, true);
				}).enterOrLeave(p);
			}, new NumberParser(numberClass(), true)));
		}

		public T finish(T requirement, Map<String, Object> datas) {
			requirement.target = (double) datas.get("target");
			requirement.comparison = (ComparisonMethod) datas.get("comparison");
			return requirement;
		}

		public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
			datas.put("target", ((T) requirement).target);
		}

		public abstract Class<? extends Number> numberClass();

		public abstract void sendHelpString(Player p);

	}

}