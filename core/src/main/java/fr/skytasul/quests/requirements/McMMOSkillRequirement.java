package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.McMMO;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class McMMOSkillRequirement extends TargetNumberRequirement {

	public String skillName;

	public McMMOSkillRequirement(){
		this(0);
	}
	
	public McMMOSkillRequirement(double target) {
		super("mcmmoSklillLevelRequired", target);
		if (!DependenciesManager.mmo) throw new MissingDependencyException("mcMMO");
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McMMO.getLevel(p, skillName);
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_SKILL.send(p, getFormattedValue(), skillName);
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
		return new String[] { getValueLore(), "§8> Skill name: §7" + skillName, "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_SKILL_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			this.skillName = (String) obj;
			super.itemClick(p, gui, clicked);
		}));
	}
	
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("skillName", skillName);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		skillName = (String) savedDatas.get("skillName");
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}

	@Override
	public AbstractRequirement clone() {
		return new McCombatLevelRequirement(target);
	}
	
}
