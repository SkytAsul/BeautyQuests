package fr.skytasul.quests.requirements;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.McCombatLevel;

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
		return McCombatLevel.getCombatLevel(p);
	}
	
	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_COMBAT_LEVEL.format(getFormattedValue());
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDCombatLevel.format(Integer.toString((int) target));
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
