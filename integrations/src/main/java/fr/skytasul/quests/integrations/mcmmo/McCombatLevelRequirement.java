package fr.skytasul.quests.integrations.mcmmo;

import org.bukkit.entity.Player;
import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;

public class McCombatLevelRequirement extends TargetNumberRequirement {

	public McCombatLevelRequirement(){
		this(null, null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public McCombatLevelRequirement(String customDescription, String customReason, double target,
			ComparisonMethod comparison) {
		super(customDescription, customReason, target, comparison);
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McCombatLevel.getPlugin(McCombatLevel.class).getLevel(p).orElse(0);
	}
	
	@Override
	protected String getPlaceholderName() {
		return "level";
	}

	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_COMBAT_LEVEL.format(this);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDCombatLevel.format(this);
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
	public AbstractRequirement clone() {
		return new McCombatLevelRequirement(getCustomDescription(), getCustomReason(), target, comparison);
	}

}
