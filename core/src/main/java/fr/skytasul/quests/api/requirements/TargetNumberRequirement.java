package fr.skytasul.quests.api.requirements;

import java.text.NumberFormat;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.EnumParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class TargetNumberRequirement extends AbstractRequirement {

	private static final EnumParser<ComparisonMethod> COMPARISON_PARSER = new EnumParser<>(ComparisonMethod.class);
	
	private static NumberFormat numberFormat = NumberFormat.getInstance();

	protected ComparisonMethod comparison = ComparisonMethod.GREATER_OR_EQUAL;
	protected double target;
	
	public TargetNumberRequirement(String name, double target) {
		super(name);
		this.target = target;
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
		return new String[] { getValueLore(), "", Lang.Remove.toString() };
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
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		sendHelpString(p);
		Editor.enterOrLeave(p, new TextEditor<>(p, () -> {
			if (target == 0) gui.remove(this);
			gui.reopen();
		}, number -> {
			target = number.doubleValue();
			Lang.COMPARISON_TYPE.send(p, COMPARISON_PARSER.getNames());
			new TextEditor<>(p, null, comp -> {
				this.comparison = comp;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}, () -> {
				this.comparison = ComparisonMethod.GREATER_OR_EQUAL;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}, COMPARISON_PARSER).enterOrLeave(p);
		}, () -> {
			gui.remove(this);
			gui.reopen();
		}, new NumberParser<>(numberClass(), true)));
	}

}