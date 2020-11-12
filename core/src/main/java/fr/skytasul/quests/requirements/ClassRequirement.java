package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.sucy.skill.api.classes.RPGClass;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class ClassRequirement extends AbstractRequirement {

	public List<RPGClass> classes;
	
	public ClassRequirement() {
		this(new ArrayList<>());
	}
	
	public ClassRequirement(List<RPGClass> classes) {
		super("classRequired");
		if (!DependenciesManager.skapi) throw new MissingDependencyException("SkillAPI");
		this.classes = classes;
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

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + classes.size() + " classes", "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new ListGUI<RPGClass>(classes, 9) {
			
			@Override
			public String name() {
				return Lang.INVENTORY_CLASSES_REQUIRED.toString();
			}
			
			@Override
			public ItemStack getItemStack(RPGClass object) {
				return ItemUtils.loreAdd(object.getIcon(), "", Lang.Remove.toString());
			}
			
			@Override
			public void click(RPGClass existing, ItemStack item) {
				if (existing == null) {
					new PagedGUI<RPGClass>(Lang.INVENTORY_CLASSES_LIST.toString(), DyeColor.PURPLE, SkillAPI.getClasses()) {
						
						@Override
						public ItemStack getItemStack(RPGClass object) {
							return object.getIcon();
						}
						
						@Override
						public void click(RPGClass existing, ClickType click) {
							finishItem(existing);
						}
					}.create(p);
				}
			}
			
			@Override
			public void finish() {
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		}.create(p);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new ClassRequirement(new ArrayList<>(classes));
	}
	
	protected void save(Map<String, Object> datas) {
		if (classes.isEmpty()) return;
		List<String> ls = new ArrayList<>();
		for (RPGClass cl : classes) {
			ls.add(cl.getName());
		}
		datas.put("classes", ls);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		if (!savedDatas.containsKey("classes")) return;
		for (String s : (List<String>) savedDatas.get("classes")) {
			RPGClass classe = com.sucy.skill.SkillAPI.getClasses().get(s.toLowerCase());
			if (classe == null) {
				BeautyQuests.getInstance().getLogger().warning("Class with name " + s + " no longer exists.");
				continue;
			}
			classes.add(classe);
		}
	}
	
}
