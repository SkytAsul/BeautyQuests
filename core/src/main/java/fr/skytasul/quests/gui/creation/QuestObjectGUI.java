package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
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
import fr.skytasul.quests.requirements.RegionRequirement;
import fr.skytasul.quests.requirements.ScoreboardRequirement;
import fr.skytasul.quests.rewards.CommandReward;
import fr.skytasul.quests.rewards.ItemReward;
import fr.skytasul.quests.rewards.MessageReward;
import fr.skytasul.quests.rewards.MoneyReward;
import fr.skytasul.quests.rewards.PermissionReward;
import fr.skytasul.quests.rewards.TeleportationReward;
import fr.skytasul.quests.rewards.XPReward;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;

public class QuestObjectGUI<T extends QuestObject> extends ListGUI<T> {

	private String name;
	private Collection<QuestObjectCreator<T>> creators;
	private Consumer<List<T>> end;

	public QuestObjectGUI(String name, Collection<QuestObjectCreator<T>> creators, Consumer<List<T>> end, List<T> objects) {
		super((List<T>) objects.stream().map(QuestObject::clone).collect(Collectors.toCollection(ArrayList::new)), 18);
		this.name = name;
		this.creators = creators;
		this.end = end;
	}

	public void reopen(Player p) {
		Inventories.put(p, this, inv);
		p.openInventory(inv);
	}

	@Override
	public String name() {
		return name;
	}
	
	@Override
	public ItemStack getItemStack(QuestObject object) {
		return object.getItemStack();
	}
	
	public boolean remove(QuestObject object) {
		return super.remove((T) object);
	}
	
	@Override
	public void click(QuestObject existing, ItemStack item) {
		if (existing == null) {
			new PagedGUI<QuestObjectCreator<T>>(name, DyeColor.CYAN, creators) {
				
				@Override
				public ItemStack getItemStack(QuestObjectCreator<T> object) {
					return object.item;
				}
				
				@Override
				public void click(QuestObjectCreator<T> existing) {
					T object = existing.newObjectSupplier.get();
					object.itemClick(p, QuestObjectGUI.this, finishItem(object));
				}
				
			}.create(p);
		}else existing.itemClick(p, this, item);
	}
	
	@Override
	public void finish() {
		end.accept(objects);
	}


	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.registerReward(CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new);
		QuestsAPI.registerReward(ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new);
		QuestsAPI.registerReward(MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new);
		if (DependenciesManager.vault) QuestsAPI.registerReward(MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), MoneyReward::new);
		if (DependenciesManager.vault) QuestsAPI.registerReward(PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), PermissionReward::new);
		QuestsAPI.registerReward(TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new);
		QuestsAPI.registerReward(XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new);
		
		DebugUtils.logMessage("Initlializing default requirements.");
		
		QuestsAPI.registerRequirement(QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new);
		QuestsAPI.registerRequirement(LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new);
		QuestsAPI.registerRequirement(PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new);
		QuestsAPI.registerRequirement(ScoreboardRequirement.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new);
		if (DependenciesManager.wg) QuestsAPI.registerRequirement(RegionRequirement.class, ItemUtils.item(XMaterial.WOODEN_AXE, Lang.RRegion.toString()), RegionRequirement::new);
		if (DependenciesManager.jobs) QuestsAPI.registerRequirement(JobLevelRequirement.class, ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()), JobLevelRequirement::new);
		if (DependenciesManager.fac) QuestsAPI.registerRequirement(FactionRequirement.class, ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), FactionRequirement::new);
		if (DependenciesManager.skapi) QuestsAPI.registerRequirement(ClassRequirement.class, ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), ClassRequirement::new);
		if (DependenciesManager.papi) QuestsAPI.registerRequirement(PlaceholderRequirement.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), PlaceholderRequirement::new);
		if (DependenciesManager.mmo) QuestsAPI.registerRequirement(McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RSkillLvl.toString()), McMMOSkillRequirement::new);
		if (DependenciesManager.mclvl) QuestsAPI.registerRequirement(McCombatLevelRequirement.class, ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), McCombatLevelRequirement::new);
		if (DependenciesManager.vault) QuestsAPI.registerRequirement(MoneyRequirement.class, ItemUtils.item(XMaterial.EMERALD, Lang.RMoney.toString()), MoneyRequirement::new);
	}

}