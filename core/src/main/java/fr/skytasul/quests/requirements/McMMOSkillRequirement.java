package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.McMMO;

public class McMMOSkillRequirement extends TargetNumberRequirement {

	public String skillName;

	public McMMOSkillRequirement(){
		this(null, null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public McMMOSkillRequirement(String customDescription, String customReason, double target, ComparisonMethod comparison) {
		super(customDescription, customReason, target, comparison);
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McMMO.getLevel(p, skillName);
	}
	
	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_SKILL.format(getFormattedValue(), skillName);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
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
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription("§8Skill name: §7" + skillName);
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
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("skillName", skillName);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		skillName = section.getString("skillName");
	}

	@Override
	public AbstractRequirement clone() {
		return new McMMOSkillRequirement(getCustomDescription(), getCustomReason(), target, comparison);
	}
	
}
