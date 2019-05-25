package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreationRunnables;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.TextListEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.requirements.ClassRequirement;
import fr.skytasul.quests.requirements.FactionRequirement;
import fr.skytasul.quests.requirements.JobLevelRequirement;
import fr.skytasul.quests.requirements.LevelRequirement;
import fr.skytasul.quests.requirements.McCombatLevelRequirement;
import fr.skytasul.quests.requirements.McMMOSkillRequirement;
import fr.skytasul.quests.requirements.PermissionsRequirement;
import fr.skytasul.quests.requirements.PlaceholderRequirement;
import fr.skytasul.quests.requirements.QuestRequirement;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Factions;
import fr.skytasul.quests.utils.compatibility.SkillAPI;
import fr.skytasul.quests.utils.types.RunnableObj;
import fr.skytasul.quests.utils.types.RunnableReturn;
import net.citizensnpcs.api.npc.NPC;

public class RequirementsGUI implements CustomInventory {

	private Inventory inv;
	private HashMap<Integer, Map<String, Object>> datas = new HashMap<>();
	
	private RunnableObj end;
	private Map<Class<?>, AbstractRequirement> lastRequirements = new HashMap<>();
	
	public RequirementsGUI(RunnableObj end, List<AbstractRequirement> requirements){
		this.end = end;
		for (AbstractRequirement req : requirements){
			lastRequirements.put(req.getClass(), req);
		}
	}
	
	
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, (int) StrictMath.ceil(RequirementCreator.getCreators().size() * 1.0 / 9) * 9 + 9, Lang.INVENTORY_REQUIREMENTS.toString());
		
		inv.setItem(4, ItemUtils.itemDone());
		LinkedList<RequirementCreator> ls = RequirementCreator.getCreators();
		for (RequirementCreator crea : ls){
			int id = ls.indexOf(crea) + 9;
			inv.setItem(id, ItemUtils.lore(crea.item.clone(), "", Lang.Unused.toString()));
			if (lastRequirements.containsKey(crea.clazz)){
				Map<String, Object> ldatas = initDatas(ls.indexOf(crea));
				datas.put(id, ldatas);
				//DebugUtils.debugMessage(p, "last requirement : clazz " + crea.clazz.getName() + " | " + (ldata == null) + " | for slot " + id);
				crea.runnables.edit(ldatas, lastRequirements.get(crea.clazz));
				usedLore(inv.getItem(id));
			}
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void removeRequirement(Map<String, Object> datas){
		for (Entry<Integer, Map<String, Object>> en : this.datas.entrySet()){
			if (en.getValue() == datas){
				remove(en.getKey());
				return;
			}
		}
	}
	
	public void remove(int slot){
		datas.remove(slot);
		ItemUtils.lore(inv.getItem(slot), "", Lang.Unused.toString());
	}
	
	private void usedLore(ItemStack is){
		ItemUtils.loreAdd(is, "", Lang.Used.toString(), Lang.Remove.toString());
	}
	
	private Map<String, Object> initDatas(int id){
		Map<String, Object> data = new HashMap<>();
		data.put("666DONOTREMOVE-id", id);
		data.put("slot", id + 9);
		return data;
	}
	
	/**
	 * Get the RequirementsGUI, open it for player if specified, and re implement the player in the inventories system if on true
	 * @param p player to open (can be null)
	 * @param reImplement re implement the player in the inventories system
	 * @return this RequirementsGUI
	 */
	public RequirementsGUI reopen(Player p, boolean reImplement){
		if (p != null){
			if (reImplement) Inventories.put(p, this, inv);
			p.openInventory(inv);
		}
		return this;
	}

	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot == 4){
			List<AbstractRequirement> req = new ArrayList<>();
			LinkedList<RequirementCreator> ls = RequirementCreator.getCreators();
			for (Entry<Integer, Map<String, Object>> data : datas.entrySet()){
				req.add(ls.get((int) data.getValue().get("666DONOTREMOVE-id")).runnables.finish(data.getValue()));
			}
			Inventories.closeAndExit(p);
			end.run(req);	
			return true;
		}
		if (!datas.containsKey(slot)){
			datas.put(slot, initDatas(slot - 9));
			ItemUtils.lore(current);
			RequirementCreator.getCreators().get(slot - 9).runnables.itemClick(p, datas.get(slot), this);
			usedLore(current);
		}else {
			if (click == ClickType.MIDDLE){
				remove(slot);
			}
		}
		return true;
	}

	
	

	
	public static void initialize(){
		DebugUtils.broadcastDebugMessage("Initlializing default requirements.");
		
		QuestsAPI.registerRequirement(QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), new QuestR());
		QuestsAPI.registerRequirement(LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), new LevelR());
		if (Dependencies.jobs) QuestsAPI.registerRequirement(JobLevelRequirement.class, ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()), new JobLevelR());
		QuestsAPI.registerRequirement(PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), new PermissionsR());
		if (Dependencies.fac) QuestsAPI.registerRequirement(FactionRequirement.class, ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), new FactionR());
		if (Dependencies.skapi) QuestsAPI.registerRequirement(ClassRequirement.class, ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), new ClassR());
		if (Dependencies.papi) QuestsAPI.registerRequirement(PlaceholderRequirement.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), new PlaceholderR());
		if (Dependencies.mmo) QuestsAPI.registerRequirement(McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RJobLvl.toString()), new SkillLevelR());
		if (Dependencies.mclvl) QuestsAPI.registerRequirement(McCombatLevelRequirement.class, ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), new CombatLevelR());
	}
	
}





/*                         RUNNABLES                    */
class QuestR implements RequirementCreationRunnables{
	
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Utils.sendMessage(p, Lang.CHOOSE_NPC_STARTER.toString());
		Editor.enterOrLeave(p, new SelectNPC(p, new RunnableObj(){
			public void run(Object obj){
				if (obj == null) {
					gui.reopen(p, true);
					gui.removeRequirement(datas);
					return;
				}
				NPC npc = (NPC) obj;
				if (QuestsAPI.isQuestStarter(npc)){
					Inventories.create(p, new ChooseQuestGUI(QuestsAPI.getQuestsAssigneds(npc), (quest) -> {
							if (quest != null){
								if (datas.containsKey("id")) datas.remove("id");
								datas.put("id", ((Quest) quest).getID());
							}else gui.remove((int) datas.get("slot"));
							gui.reopen(p, true);
					}));
				}else {
					Utils.sendMessage(p, Lang.NPC_NOT_QUEST.toString());
					gui.reopen(p, true);
					gui.removeRequirement(datas);
				}
			}
		}));
	}
	
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		QuestRequirement req = new QuestRequirement();
		req.questId = (int) datas.get("id");
		return req;
	}
	
	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("id", ((QuestRequirement) requirement).questId);
	}
}

class LevelR implements RequirementCreationRunnables{
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Lang.CHOOSE_XP_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			if (datas.containsKey("lvl")) datas.remove("lvl");
			datas.put("lvl", (int) obj);
			gui.reopen(p, false);
		}, new NumberParser(Integer.class, true)));
	}
	
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		LevelRequirement req = new LevelRequirement();
		req.level = (int) datas.get("lvl");
		return req;
	}

	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("lvl", ((LevelRequirement) requirement).level);
	}
}

class JobLevelR implements RequirementCreationRunnables{
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Lang.CHOOSE_JOB_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			if (datas.containsKey("job")){
				datas.remove("lvl");
				datas.remove("job");
			}
			datas.put("job", obj);
			Lang.CHOOSE_XP_REQUIRED.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (lvl) -> {
				datas.put("lvl", (int) lvl);
				gui.reopen(p, false);
			}, new NumberParser(Integer.class, true)));
		}));
	}

	
	public AbstractRequirement finish(Map<String, Object> datas) {
		JobLevelRequirement req = new JobLevelRequirement();
		req.level = (int) datas.get("lvl");
		req.jobName = (String) datas.get("job");
		return req;
	}
	
	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("lvl", ((JobLevelRequirement) requirement).level);
		datas.put("job", ((JobLevelRequirement) requirement).jobName);
	}
}

class PermissionsR implements RequirementCreationRunnables{
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		if (!datas.containsKey("perms")) datas.put("perms", new ArrayList<String>());
		Lang.CHOOSE_PERM_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextListEditor(p, (obj) -> {
				Lang.CHOOSE_PERM_REQUIRED_MESSAGE.send(p);
				new TextEditor(p, (text) -> {
					datas.put("msg", text);
					gui.reopen(p, false);
				}, () -> {
					gui.reopen(p, false);
				}, () -> {
					datas.put("msg", null);
					gui.reopen(p, false);
				}).enterOrLeave(p);
		}, (List<String>) datas.get("perms")));
	}
	
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		PermissionsRequirement req = new PermissionsRequirement();
		req.permissions = (List<String>) datas.get("perms");
		req.message = (String) datas.get("msg");
		return req;
	}
	
	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("perms", new ArrayList<>(((PermissionsRequirement) requirement).permissions));
		datas.put("msg", ((PermissionsRequirement) requirement).message);
	}
}

class FactionR implements RequirementCreationRunnables{
	
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		if (!datas.containsKey("factions")) datas.put("factions", new ArrayList<String>());
		Lang.CHOOSE_FAC_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextListEditor(p, (obj) -> {
				gui.reopen(p, false);
		}, (List<String>) datas.get("factions"))).valid = new RunnableReturn<Boolean>() {
			public Boolean run(Object obj) {
				if (!Factions.factionExists((String) obj)){
					Lang.FACTION_DOESNT_EXIST.send(p);
					return false;
				}
				return true;
			}
		};
	}

	public AbstractRequirement finish(Map<String, Object> datas) {
		FactionRequirement req = new FactionRequirement();
		for(String s : (List<String>) datas.get("factions")) req.addFaction(Factions.getFaction(s));
		return req;
	}

	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("factions", new ArrayList<>(((FactionRequirement) requirement).getFactionsName()));
	}
}

class ClassR implements RequirementCreationRunnables{
	
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		if (!datas.containsKey("classes")) datas.put("classes", new ArrayList<String>());
		Lang.CHOOSE_CLASSES_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextListEditor(p, (obj) -> {
			gui.reopen(p, false);
		}, (List<String>) datas.get("classes"))).valid = new RunnableReturn<Boolean>() {
			public Boolean run(Object obj) {
				if (!SkillAPI.classExists((String) obj)){
					Lang.CLASS_DOESNT_EXIST.send(p);
					return false;
				}
				return true;
			}
		};
	}
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		ClassRequirement req = new ClassRequirement();
		for(String s : (List<String>) datas.get("classes")) req.addClass(SkillAPI.getClass(s));
		return req;
	}

	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("classes", new ArrayList<>(((ClassRequirement) requirement).getClassesName()));
	}
}

class PlaceholderR implements RequirementCreationRunnables{
	
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Lang.CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER.send(p);
		new TextEditor(p, (id) -> {
			datas.put("placeholder", id);
			Lang.CHOOSE_PLACEHOLDER_REQUIRED_VALUE.send(p, id);
			new TextEditor(p, (value) -> {
				datas.put("value", value);
				gui.reopen(p, false);
			}).enterOrLeave(p);
		}).enterOrLeave(p);
	}
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		PlaceholderRequirement req = new PlaceholderRequirement();
		req.setPlaceholder((String) datas.get("placeholder"));
		req.setValue((String) datas.get("value"));
		return req;
	}

	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		PlaceholderRequirement req = (PlaceholderRequirement) requirement;
		datas.put("placeholder", req.getPlaceholder());
		datas.put("value", req.getValue());
	}
}

class SkillLevelR implements RequirementCreationRunnables{
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Lang.CHOOSE_SKILL_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			if (datas.containsKey("skill")){
				datas.remove("lvl");
				datas.remove("skill");
			}
			datas.put("skill", obj);
			Lang.CHOOSE_XP_REQUIRED.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (lvl) -> {
				datas.put("lvl", (int) lvl);
				gui.reopen(p, false);
			}, new NumberParser(Integer.class, true)));
		}));
	}

	
	public AbstractRequirement finish(Map<String, Object> datas) {
		McMMOSkillRequirement req = new McMMOSkillRequirement();
		req.level = (int) datas.get("lvl");
		req.skillName = (String) datas.get("skill");
		return req;
	}
	
	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("lvl", ((McMMOSkillRequirement) requirement).level);
		datas.put("skill", ((McMMOSkillRequirement) requirement).skillName);
	}
}

class CombatLevelR implements RequirementCreationRunnables{
	public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
		Lang.CHOOSE_XP_REQUIRED.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			if (datas.containsKey("lvl")) datas.remove("lvl");
			datas.put("lvl", (int) obj);
			gui.reopen(p, false);
		}, new NumberParser(Integer.class, true)));
	}
	
	
	public AbstractRequirement finish(Map<String, Object> datas) {
		McCombatLevelRequirement req = new McCombatLevelRequirement();
		req.level = (int) datas.get("lvl");
		return req;
	}

	
	public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
		datas.put("lvl", ((McCombatLevelRequirement) requirement).level);
	}
}