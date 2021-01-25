package fr.skytasul.quests.gui.creation;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
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
import fr.skytasul.quests.rewards.CheckpointReward;
import fr.skytasul.quests.rewards.CommandReward;
import fr.skytasul.quests.rewards.ItemReward;
import fr.skytasul.quests.rewards.MessageReward;
import fr.skytasul.quests.rewards.MoneyReward;
import fr.skytasul.quests.rewards.PermissionReward;
import fr.skytasul.quests.rewards.RequirementDependentReward;
import fr.skytasul.quests.rewards.TeleportationReward;
import fr.skytasul.quests.rewards.XPReward;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;

public class QuestObjectGUI<T extends QuestObject> extends ListGUI<T> {

	private String name;
	private Collection<QuestObjectCreator<T>> creators;
	private Consumer<List<T>> end;

	public QuestObjectGUI(String name, QuestObjectLocation objectLocation, Collection<QuestObjectCreator<T>> creators, Consumer<List<T>> end, List<T> objects) {
		super(name, DyeColor.CYAN, (List<T>) objects.stream().map(QuestObject::clone).collect(Collectors.toList()));
		this.name = name;
		this.creators = creators.stream().filter(creator -> creator.isAllowed(objectLocation) && (creator.multiple || !objects.stream().anyMatch(object -> object.getCreator() == creator))).collect(Collectors.toList());
		this.end = end;
	}

	@Deprecated
	public void reopen(Player p) {
		super.reopen();
	}
	
	@Override
	public ItemStack getObjectItemStack(QuestObject object) {
		return object.getItemStack();
	}
	
	@Override
	public boolean remove(QuestObject object) {
		return super.remove((T) object);
	}
	
	@Override
	protected void removed(T object) {
		if (!object.getCreator().multiple) creators.add((QuestObjectCreator<T>) object.getCreator());
	}
	
	@Override
	public void createObject(Function<T, ItemStack> callback) {
		new PagedGUI<QuestObjectCreator<T>>(name, DyeColor.CYAN, creators) {
			
			@Override
			public ItemStack getItemStack(QuestObjectCreator<T> object) {
				return object.item;
			}
			
			@Override
			public void click(QuestObjectCreator<T> existing, ItemStack item, ClickType clickType) {
				T object = existing.newObjectSupplier.get();
				if (!existing.multiple) creators.remove(existing);
				object.itemClick(p, QuestObjectGUI.this, callback.apply(object));
			}
			
			@Override
			public CloseBehavior onClose(Player p, Inventory inv) {
				Utils.runSync(QuestObjectGUI.super::reopen);
				return CloseBehavior.NOTHING;
			}
			
		}.create(p);
	}
	
	@Override
	public void clickObject(QuestObject existing, ItemStack item, ClickType clickType) {
		existing.itemClick(p, this, item);
	}
	
	@Override
	public void finish(List<T> objects) {
		end.accept(objects);
	}

	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.registerReward(new QuestObjectCreator<>(CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new));
		if (DependenciesManager.vault) QuestsAPI.registerReward(new QuestObjectCreator<>(MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), MoneyReward::new));
		if (DependenciesManager.vault) QuestsAPI.registerReward(new QuestObjectCreator<>(PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), PermissionReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new, false));
		QuestsAPI.registerReward(new QuestObjectCreator<>(XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<CheckpointReward>(CheckpointReward.class, ItemUtils.item(XMaterial.NETHER_STAR, Lang.rewardCheckpoint.toString()), CheckpointReward::new, false, QuestObjectLocation.STAGE));
		QuestsAPI.registerReward(new QuestObjectCreator<RequirementDependentReward>(RequirementDependentReward.class, ItemUtils.item(XMaterial.REDSTONE, Lang.rewardWithRequirements.toString()), RequirementDependentReward::new, true));
		
		DebugUtils.logMessage("Initlializing default requirements.");
		
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new));
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new));
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new));
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(ScoreboardRequirement.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new));
		if (DependenciesManager.wg) QuestsAPI.registerRequirement(new QuestObjectCreator<>(RegionRequirement.class, ItemUtils.item(XMaterial.WOODEN_AXE, Lang.RRegion.toString()), RegionRequirement::new));
		if (DependenciesManager.jobs) QuestsAPI.registerRequirement(new QuestObjectCreator<>(JobLevelRequirement.class, ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()), JobLevelRequirement::new));
		if (DependenciesManager.fac) QuestsAPI.registerRequirement(new QuestObjectCreator<>(FactionRequirement.class, ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), FactionRequirement::new));
		if (DependenciesManager.skapi) QuestsAPI.registerRequirement(new QuestObjectCreator<>(ClassRequirement.class, ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), ClassRequirement::new));
		if (DependenciesManager.papi) QuestsAPI.registerRequirement(new QuestObjectCreator<>(PlaceholderRequirement.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), PlaceholderRequirement::new));
		if (DependenciesManager.mmo) QuestsAPI.registerRequirement(new QuestObjectCreator<>(McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RSkillLvl.toString()), McMMOSkillRequirement::new));
		if (DependenciesManager.mclvl) QuestsAPI.registerRequirement(new QuestObjectCreator<>(McCombatLevelRequirement.class, ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), McCombatLevelRequirement::new));
		if (DependenciesManager.vault) QuestsAPI.registerRequirement(new QuestObjectCreator<>(MoneyRequirement.class, ItemUtils.item(XMaterial.EMERALD, Lang.RMoney.toString()), MoneyRequirement::new));
	}

}