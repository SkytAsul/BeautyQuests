package fr.skytasul.quests.rewards;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
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

	public XPReward(String customDescription, int exp) {
		super(customDescription);
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
		return new XPReward(getCustomDescription(), exp);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return exp + " " + Lang.Exp.toString();
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(exp + " " + Lang.Exp.toString());
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Utils.sendMessage(event.getPlayer(), Lang.XP_GAIN.toString(), exp);
		new TextEditor<>(event.getPlayer(), event::cancel, obj -> {
			Utils.sendMessage(event.getPlayer(), Lang.XP_EDITED.toString(), exp, obj);
			exp = obj;
			event.reopenGUI();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("xp", exp);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		exp = section.getInt("xp");
	}
	
}
