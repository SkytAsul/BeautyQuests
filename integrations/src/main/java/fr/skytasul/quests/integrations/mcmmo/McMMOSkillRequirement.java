package fr.skytasul.quests.integrations.mcmmo;

import com.gmail.nossr50.api.ExperienceAPI;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
		return ExperienceAPI.getLevel(p, skillName);
	}
	
	@Override
	protected String getPlaceholderName() {
		return "level";
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("skill_name", () -> skillName);
	}

	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_SKILL.format(this);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDSkillLevel.format(this);
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
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription("§8Skill name: §7" + skillName);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_SKILL_REQUIRED.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::cancel, obj -> {
			this.skillName = obj;
			super.itemClick(event);
		}).useStrippedMessage().start();
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
