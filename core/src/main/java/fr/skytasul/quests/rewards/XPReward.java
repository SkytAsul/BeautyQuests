package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class XPReward extends AbstractReward {

	public int exp = 0;

	public XPReward() {
		super("expReward");
	}

	public XPReward(int exp) {
		this();
		this.exp = exp;
	}

	public List<String> give(Player p) {
		if (DependenciesManager.skapi && QuestsConfiguration.xpOverridedSkillAPI()) {
			SkillAPI.giveExp(p, exp);
		}else p.giveExp(exp);
		return Arrays.asList(exp + " " + Lang.Exp.toString());
	}

	@Override
	public AbstractReward clone() {
		return new XPReward(exp);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + exp + " " + Lang.Exp.toString(), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Utils.sendMessage(p, Lang.XP_GAIN.toString(), exp);
		new TextEditor<>(p, () -> {
			if (exp == 0) gui.remove(this);
			gui.reopen();
		}, obj -> {
			Utils.sendMessage(p, Lang.XP_EDITED.toString(), exp, obj);
			exp = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("xp", exp);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		exp = (int) savedDatas.get("xp");
	}
	
}
