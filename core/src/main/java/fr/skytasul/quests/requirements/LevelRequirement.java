package fr.skytasul.quests.requirements;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;

public class LevelRequirement extends TargetNumberRequirement {
	
	public LevelRequirement() {
		this(0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public LevelRequirement(double target, ComparisonMethod comparison) {
		super(target, comparison);
	}

	@Override
	public double getPlayerTarget(Player p) {
		return p.getLevel();
	}
	
	@Override
	public void sendReason(Player p){
		Lang.REQUIREMENT_LEVEL.send(p, getFormattedValue());
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
	public String getDescription(Player p) {
		return Lang.RDLevel.format(Integer.toString((int) target));
	}
	
	@Override
	public AbstractRequirement clone() {
		return new LevelRequirement(target, comparison);
	}

}
