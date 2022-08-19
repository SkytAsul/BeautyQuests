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
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
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
		this.creators = creators.stream()
				.filter(creator -> creator.isAllowed(objectLocation))
				.filter(creator -> creator.canBeMultiple() || objects.stream().noneMatch(object -> object.getCreator() == creator))
				.collect(Collectors.toList());
		this.end = end;
	}
	
	@Override
	public ItemStack getObjectItemStack(QuestObject object) {
		return object.getItemStack();
	}
	
	@Override
	protected void removed(T object) {
		if (!object.getCreator().canBeMultiple()) creators.add(object.getCreator());
	}
	
	@Override
	public void createObject(Function<T, ItemStack> callback) {
		new PagedGUI<QuestObjectCreator<T>>(name, DyeColor.CYAN, creators) {
			
			@Override
			public ItemStack getItemStack(QuestObjectCreator<T> object) {
				return object.getItem();
			}
			
			@Override
			public void click(QuestObjectCreator<T> existing, ItemStack item, ClickType clickType) {
				T object = existing.newObject();
				if (!existing.canBeMultiple()) creators.remove(existing);
				object.itemClick(new QuestObjectClickEvent(p, QuestObjectGUI.this, callback.apply(object), clickType, true, object));
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
		existing.itemClick(new QuestObjectClickEvent(p, this, item, clickType, false, existing));
	}
	
	@Override
	public void finish(List<T> objects) {
		end.accept(objects);
	}

	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.getRewards().register(new RewardCreator("commandReward", CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("itemReward", ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("removeItemsReward", RemoveItemsReward.class, ItemUtils.item(XMaterial.CHEST, Lang.rewardRemoveItems.toString()), RemoveItemsReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("textReward", MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("tpReward", TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new, false));
		QuestsAPI.getRewards().register(new RewardCreator("expReward", XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("checkpointReward", CheckpointReward.class, ItemUtils.item(XMaterial.NETHER_STAR, Lang.rewardCheckpoint.toString()), CheckpointReward::new, false, QuestObjectLocation.STAGE));
		QuestsAPI.getRewards().register(new RewardCreator("questStopReward", QuestStopReward.class, ItemUtils.item(XMaterial.BARRIER, Lang.rewardStopQuest.toString()), QuestStopReward::new, false, QuestObjectLocation.STAGE));
		QuestsAPI.getRewards().register(new RewardCreator("requirementDependentReward", RequirementDependentReward.class, ItemUtils.item(XMaterial.REDSTONE, Lang.rewardWithRequirements.toString()), RequirementDependentReward::new, true).setCanBeAsync(true));
		QuestsAPI.getRewards().register(new RewardCreator("randomReward", RandomReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardRandom.toString()), RandomReward::new, true).setCanBeAsync(true));
		QuestsAPI.getRewards().register(new RewardCreator("wait", WaitReward.class, ItemUtils.item(XMaterial.CLOCK, Lang.rewardWait.toString()), WaitReward::new, true).setCanBeAsync(true));
		QuestsAPI.getRewards().register(new RewardCreator("titleReward", TitleReward.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.rewardTitle.toString()), TitleReward::new, false));
		
		DebugUtils.logMessage("Initlializing default requirements.");
		
		QuestsAPI.getRequirements().register(new RequirementCreator("logicalOr", LogicalOrRequirement.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.RLOR.toString()), LogicalOrRequirement::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("questRequired", QuestRequirement.class, ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("levelRequired", LevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("permissionRequired", PermissionsRequirement.class, ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("scoreboardRequired", ScoreboardRequirement.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new));
	}

}