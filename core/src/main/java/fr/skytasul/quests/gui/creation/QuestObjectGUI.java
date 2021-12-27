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
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.requirements.LevelRequirement;
import fr.skytasul.quests.requirements.PermissionsRequirement;
import fr.skytasul.quests.requirements.QuestRequirement;
import fr.skytasul.quests.requirements.ScoreboardRequirement;
import fr.skytasul.quests.requirements.logical.LogicalOrRequirement;
import fr.skytasul.quests.rewards.*;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

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
		if (!object.getCreator().multiple) creators.add(object.getCreator());
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
				object.itemClick(new QuestObjectClickEvent(p, QuestObjectGUI.this, callback.apply(object), clickType, true));
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
		existing.itemClick(new QuestObjectClickEvent(p, this, item, clickType, false));
	}
	
	@Override
	public void finish(List<T> objects) {
		end.accept(objects);
	}

	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.getRewards().register(new QuestObjectCreator<>("commandReward", CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("itemReward", ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("removeItemsReward", RemoveItemsReward.class, ItemUtils.item(XMaterial.CHEST, Lang.rewardRemoveItems.toString()), RemoveItemsReward::new));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("textReward", MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("tpReward", TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new, false));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("expReward", XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("checkpointReward", CheckpointReward.class, ItemUtils.item(XMaterial.NETHER_STAR, Lang.rewardCheckpoint.toString()), CheckpointReward::new, false, QuestObjectLocation.STAGE));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("questStopReward", QuestStopReward.class, ItemUtils.item(XMaterial.BARRIER, Lang.rewardStopQuest.toString()), QuestStopReward::new, false, QuestObjectLocation.STAGE));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("requirementDependentReward", RequirementDependentReward.class, ItemUtils.item(XMaterial.REDSTONE, Lang.rewardWithRequirements.toString()), RequirementDependentReward::new, true));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("wait", WaitReward.class, ItemUtils.item(XMaterial.CLOCK, Lang.rewardWait.toString()), WaitReward::new, true));
		QuestsAPI.getRewards().register(new QuestObjectCreator<>("titleReward", TitleReward.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.rewardTitle.toString()), TitleReward::new, false));
		
		DebugUtils.logMessage("Initlializing default requirements.");
		
		QuestsAPI.getRequirements().register(new QuestObjectCreator<>("logicalOr", LogicalOrRequirement.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.RLOR.toString()), LogicalOrRequirement::new));
		QuestsAPI.getRequirements().register(new QuestObjectCreator<>("questRequired", QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new));
		QuestsAPI.getRequirements().register(new QuestObjectCreator<>("levelRequired", LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new));
		QuestsAPI.getRequirements().register(new QuestObjectCreator<>("permissionRequired", PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new));
		QuestsAPI.getRequirements().register(new QuestObjectCreator<>("scoreboardRequired", ScoreboardRequirement.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new));
	}

}