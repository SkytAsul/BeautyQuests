package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class XPReward extends AbstractReward {

	public int exp = 0;

	public XPReward() {}

	public XPReward(int exp) {
		this.exp = exp;
	}

	@Override
	public List<String> give(Player p) {
		if (DependenciesManager.skapi.isEnabled() && QuestsConfiguration.xpOverridedSkillAPI()) {
			SkillAPI.giveExp(p, exp);
		}else p.giveExp(exp);
		return Arrays.asList(exp + " " + Lang.Exp.toString());
	}

	@Override
	public AbstractReward clone() {
		return new XPReward(exp);
	}
	
	@Override
	public String getDescription(Player p) {
		return exp + " " + Lang.Exp.toString();
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + exp + " " + Lang.Exp.toString(), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Utils.sendMessage(event.getPlayer(), Lang.XP_GAIN.toString(), exp);
		new TextEditor<>(event.getPlayer(), () -> {
			if (exp == 0) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			Utils.sendMessage(event.getPlayer(), Lang.XP_EDITED.toString(), exp, obj);
			exp = obj;
			event.updateItemLore(getLore());
			event.reopenGUI();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("xp", exp);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		exp = (int) savedDatas.get("xp");
	}
	
}
