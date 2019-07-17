package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;

public class ClassRequirement extends AbstractRequirement {

	public List<RPGClass> classes = new ArrayList<>();
	
	public ClassRequirement() {
		super("classRequired");
		if (!Dependencies.skapi) throw new MissingDependencyException("SkillAPI");
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
			if (SkillAPI.getPlayerData(p).getMainClass().getData() == classe) return true;
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
			RPGClass classe = SkillAPI.getClasses().get(s.toLowerCase());
			if (classe == null){
				BeautyQuests.getInstance().getLogger().warning("Class with name " + s + " no longer exists. Quest \"" + quest.getName() + "\", ID " + quest.getID());
				continue;
			}
			classes.add(classe);
		}
	}

}
