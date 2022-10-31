package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.PlayerClass;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.SkillAPI;

public class ClassRequirement extends AbstractRequirement {

	public List<RPGClass> classes;
	
	public ClassRequirement() {
		this(new ArrayList<>());
	}
	
	public ClassRequirement(List<RPGClass> classes) {
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
	
	@Override
	public boolean test(Player p) {
		if (classes.isEmpty()) return true;
		for (RPGClass classe : classes){
			PlayerClass mainClass = com.sucy.skill.SkillAPI.getPlayerData(p).getMainClass();
			if (mainClass != null && mainClass.getData() == classe) return true;
		}
		return false;
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDClass.format(String.join(" " + Lang.Or.toString() + " ", (Iterable<String>) () -> classes.stream().map(RPGClass::getName).iterator()));
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + classes.size() + " classes", "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ListGUI<RPGClass>(Lang.INVENTORY_CLASSES_REQUIRED.toString(), DyeColor.GREEN, classes) {
			
			@Override
			public ItemStack getObjectItemStack(RPGClass object) {
				return ItemUtils.loreAdd(object.getIcon(), "", Lang.RemoveMid.toString());
			}
			
			@Override
			public void createObject(Function<RPGClass, ItemStack> callback) {
				new PagedGUI<RPGClass>(Lang.INVENTORY_CLASSES_LIST.toString(), DyeColor.PURPLE, SkillAPI.getClasses()) {
					
					@Override
					public ItemStack getItemStack(RPGClass object) {
						return object.getIcon();
					}
					
					@Override
					public void click(RPGClass existing, ItemStack item, ClickType clickType) {
						callback.apply(existing);
					}
				}.create(p);
			}
			
			@Override
			public void finish(List<RPGClass> objects) {
				classes = objects;
				event.reopenGUI();
			}
			
		}.create(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new ClassRequirement(new ArrayList<>(classes));
	}
	
	@Override
	public void save(ConfigurationSection section) {
		if (!classes.isEmpty())
			section.set("classes", classes.stream().map(RPGClass::getName).collect(Collectors.toList()));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		if (!section.contains("classes")) return;
		for (String s : section.getStringList("classes")) {
			RPGClass classe = com.sucy.skill.SkillAPI.getClasses().get(s.toLowerCase());
			if (classe == null) {
				BeautyQuests.getInstance().getLogger().warning("Class with name " + s + " no longer exists.");
				continue;
			}
			classes.add(classe);
		}
	}
	
}
