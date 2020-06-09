package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.sucy.skill.api.classes.RPGClass;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextListEditor;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class ClassRequirement extends AbstractRequirement {

	public List<RPGClass> classes = new ArrayList<>();
	
	public ClassRequirement() {
		super("classRequired");
		if (!DependenciesManager.skapi) throw new MissingDependencyException("SkillAPI");
	}

	public List<String> getClassesName(){
		List<String> ls = new ArrayList<>();
		for (RPGClass cl : classes) ls.add(cl.getName());
		return ls;
	}
	
	public void addClass(Object classe){
		classes.add((RPGClass) classe);
	}
	
	public boolean test(Player p) {
		if (classes.isEmpty()) return true;
		for (RPGClass classe : classes){
			if (com.sucy.skill.SkillAPI.getPlayerData(p).getMainClass().getData() == classe) return true;
		}
		return false;
	}

	
	protected void save(Map<String, Object> datas) {
		if (classes.isEmpty()) return;
		List<String> ls = new ArrayList<>();
		for (RPGClass cl : classes){
			ls.add(cl.getName());
		}
		datas.put("classes", ls);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		if (!savedDatas.containsKey("classes")){
			BeautyQuests.getInstance().getLogger().warning("ClassRequirement for quest \"" + quest.getName() + "\", ID " + quest.getID() + " is empty");
			return;
		}
		for (String s : (List<String>) savedDatas.get("classes")){
			RPGClass classe = com.sucy.skill.SkillAPI.getClasses().get(s.toLowerCase());
			if (classe == null){
				BeautyQuests.getInstance().getLogger().warning("Class with name " + s + " no longer exists. Quest \"" + quest.getName() + "\", ID " + quest.getID());
				continue;
			}
			classes.add(classe);
		}
	}

	public static class Creator implements RequirementCreationRunnables<ClassRequirement> {

		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			if (!datas.containsKey("classes")) datas.put("classes", new ArrayList<String>());
			Lang.CHOOSE_CLASSES_REQUIRED.send(p);
			Editor.enterOrLeave(p, new TextListEditor(p, (obj) -> {
				gui.reopen(p, false);
			}, (List<String>) datas.get("classes"))).valid = (string) -> {
				if (!SkillAPI.classExists(string)) {
					Lang.CLASS_DOESNT_EXIST.send(p);
					return false;
				}
				return true;
			};
		}

		public ClassRequirement finish(Map<String, Object> datas) {
			ClassRequirement req = new ClassRequirement();
			for (String s : (List<String>) datas.get("classes")) req.addClass(SkillAPI.getClass(s));
			return req;
		}

		public void edit(Map<String, Object> datas, ClassRequirement requirement) {
			datas.put("classes", new ArrayList<>(requirement.getClassesName()));
		}
	}

}
