package fr.skytasul.quests.api.requirements;

import java.text.NumberFormat;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.EnumParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	private static final EnumParser<ComparisonMethod> COMPARISON_PARSER = new EnumParser<>(ComparisonMethod.class);
	
	private static NumberFormat numberFormat = NumberFormat.getInstance();

	protected ComparisonMethod comparison;
	protected double target;
	
	protected TargetNumberRequirement(String name, double target, ComparisonMethod comparison) {
		super(name);
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

	public String getFormattedValue() {
		return comparison.getTitle().format(numberFormat.format(target));
	}
	
	protected String getValueLore() {
		return "§8> §7" + getFormattedValue();
	}
	
	@Override
	public String[] getLore() {
		return new String[] { getValueLore(), "", Lang.RemoveMid.toString() };
	}

	public abstract double getPlayerTarget(Player p);

	public abstract Class<? extends Number> numberClass();
	
	public abstract void sendHelpString(Player p);
	
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
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		sendHelpString(event.getPlayer());
		new TextEditor<>(event.getPlayer(), () -> {
			if (target == 0) event.getGUI().remove(this);
			event.reopenGUI();
		}, number -> {
			target = number.doubleValue();
			Lang.COMPARISON_TYPE.send(event.getPlayer(), COMPARISON_PARSER.getNames());
			new TextEditor<>(event.getPlayer(), null, comp -> {
				this.comparison = comp;
				event.updateItemLore(getLore());
				event.reopenGUI();
			}, () -> {
				this.comparison = ComparisonMethod.GREATER_OR_EQUAL;
				event.updateItemLore(getLore());
				event.reopenGUI();
			}, COMPARISON_PARSER).enter();
		}, () -> {
			event.getGUI().remove(this);
			event.reopenGUI();
		}, new NumberParser<>(numberClass(), true)).enter();
	}

}