package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.requirements.ClassRequirement;
import fr.skytasul.quests.requirements.FactionRequirement;
import fr.skytasul.quests.requirements.JobLevelRequirement;
import fr.skytasul.quests.requirements.LevelRequirement;
import fr.skytasul.quests.requirements.McCombatLevelRequirement;
import fr.skytasul.quests.requirements.McMMOSkillRequirement;
import fr.skytasul.quests.requirements.MoneyRequirement;
import fr.skytasul.quests.requirements.PermissionsRequirement;
import fr.skytasul.quests.requirements.PlaceholderRequirement;
import fr.skytasul.quests.requirements.QuestRequirement;
import fr.skytasul.quests.requirements.ScoreboardRequirement;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;

public class RequirementsGUI implements CustomInventory {

	private Inventory inv;
	private HashMap<Integer, Map<String, Object>> datas = new HashMap<>();
	
	private Consumer<List<AbstractRequirement>> end;
	private Map<Class<?>, AbstractRequirement> lastRequirements = new HashMap<>();
	
	public RequirementsGUI(Consumer<List<AbstractRequirement>> end, List<AbstractRequirement> requirements){
		this.end = end;
		for (AbstractRequirement req : requirements){
			lastRequirements.put(req.getClass(), req);
		}
	}
	
	
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, (int) StrictMath.ceil(RequirementCreator.getCreators().size() * 1.0 / 9) * 9 + 9, Lang.INVENTORY_REQUIREMENTS.toString());
		
		inv.setItem(4, ItemUtils.itemDone);
		LinkedList<RequirementCreator> ls = RequirementCreator.getCreators();
		for (RequirementCreator crea : ls){
			int id = ls.indexOf(crea) + 9;
			inv.setItem(id, crea.item.clone());
			if (lastRequirements.containsKey(crea.clazz)){
				Map<String, Object> ldatas = initDatas(ls.indexOf(crea));
				datas.put(id, ldatas);
				//DebugUtils.debugMessage(p, "last requirement : clazz " + crea.clazz.getName() + " | " + (ldata == null) + " | for slot " + id);
				crea.runnables.edit(ldatas, lastRequirements.get(crea.clazz));
				usedLore(inv.getItem(id));
			}else unusedLore(inv.getItem(id));
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
		unusedLore(inv.getItem(slot));
	}

	private void unusedLore(ItemStack is) {
		ItemUtils.lore(is, "", Lang.Unused.toString());
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
			end.accept(req);	
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
		DebugUtils.logMessage("Initlializing default requirements.");
		
		QuestsAPI.registerRequirement(QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), new QuestRequirement.Creator());
		QuestsAPI.registerRequirement(LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), new LevelRequirement.Creator());
		QuestsAPI.registerRequirement(PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), new PermissionsRequirement.Creator());
		QuestsAPI.registerRequirement(ScoreboardRequirement.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), new ScoreboardRequirement.Creator());
		if (DependenciesManager.jobs) QuestsAPI.registerRequirement(JobLevelRequirement.class, ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()), new JobLevelRequirement.Creator());
		if (DependenciesManager.fac) QuestsAPI.registerRequirement(FactionRequirement.class, ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), new FactionRequirement.Creator());
		if (DependenciesManager.skapi) QuestsAPI.registerRequirement(ClassRequirement.class, ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), new ClassRequirement.Creator());
		if (DependenciesManager.papi) QuestsAPI.registerRequirement(PlaceholderRequirement.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), new PlaceholderRequirement.Creator());
		if (DependenciesManager.mmo) QuestsAPI.registerRequirement(McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RJobLvl.toString()), new McMMOSkillRequirement.Creator());
		if (DependenciesManager.mclvl) QuestsAPI.registerRequirement(McCombatLevelRequirement.class, ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), new McCombatLevelRequirement.Creator());
		if (DependenciesManager.vault) QuestsAPI.registerRequirement(MoneyRequirement.class, ItemUtils.item(XMaterial.EMERALD, Lang.RMoney.toString()), new MoneyRequirement.Creator());
	}
	
}