package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.McMMO;

public class McMMOSkillRequirement extends TargetNumberRequirement {

	public String skillName;

	public McMMOSkillRequirement(){
		this(0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public McMMOSkillRequirement(double target, ComparisonMethod comparison) {
		super(target, comparison);
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McMMO.getLevel(p, skillName);
	}
	
	@Override
	public void sendReason(Player p){
		Lang.REQUIREMENT_SKILL.send(p, getFormattedValue(), skillName);
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDSkillLevel.format(Integer.toString((int) target), skillName);
	}
	
	@Override
	public Class<? extends Number> numberClass() {
		return Integer.class;
	}
	
	@Override
	public void sendHelpString(Player p) {
		Lang.CHOOSE_XP_REQUIRED.send(p);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { getValueLore(), "§8> Skill name: §7" + skillName, "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_SKILL_REQUIRED.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), () -> {
			if (skillName == null) event.getGUI().remove(this);
			event.reopenGUI();
		}, (obj) -> {
			this.skillName = obj;
			super.itemClick(event);
		}).useStrippedMessage().enter();
	}
	
	@Override
	protected void save(ConfigurationSection section) {
		super.save(section);
		section.set("skillName", skillName);
	}
	
	@Override
	protected void load(ConfigurationSection section) {
		super.load(section);
		skillName = section.getString("skillName");
	}

	@Override
	public AbstractRequirement clone() {
		return new McMMOSkillRequirement(target, comparison);
	}
	
}
