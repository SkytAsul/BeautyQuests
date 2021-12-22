package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class RequirementDependentReward extends AbstractReward {
	
	private List<AbstractRequirement> requirements;
	private List<AbstractReward> rewards;
	
	public RequirementDependentReward() {
		this(new ArrayList<>(), new ArrayList<>());
	}
	
	public RequirementDependentReward(List<AbstractRequirement> requirements, List<AbstractReward> rewards) {
		super("requirementDependentReward");
		this.requirements = requirements;
		this.rewards = rewards;
	}
	
	@Override
	public List<String> give(Player p) {
		if (requirements.stream().allMatch(requirement -> requirement.test(p))) return Utils.giveRewards(p, rewards);
		return null;
	}
	
	@Override
	public boolean isAsync() {
		return rewards.stream().anyMatch(AbstractReward::isAsync);
	}
	
	@Override
	public AbstractReward clone() {
		return new RequirementDependentReward(new ArrayList<>(requirements), new ArrayList<>(rewards));
	}
	
	@Override
	public String getDescription(Player p) {
		return requirements.stream().allMatch(req -> req.test(p)) ? "aa" : null;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { QuestOption.formatDescription(Lang.requirements.format(requirements.size()) + ", " + Lang.actions.format(rewards.size())), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new CustomInventory() {
			
			private Inventory inv;
			
			@Override
			public Inventory open(Player p) {
				inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_REWARDS_WITH_REQUIREMENTS.toString());
				
				inv.setItem(0, ItemUtils.item(XMaterial.NETHER_STAR, "§b" + Lang.requirements.format(requirements.size())));
				inv.setItem(1, ItemUtils.item(XMaterial.CHEST, "§a" + Lang.rewards.format(rewards.size())));
				
				inv.setItem(4, ItemUtils.itemDone);
				
				return inv = p.openInventory(inv).getTopInventory();
			}
			
			private void reopen() {
				Inventories.put(event.getPlayer(), this, inv);
				event.getPlayer().openInventory(inv);
			}
			
			@Override
			public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
				switch (slot) {
				case 0:
					new QuestObjectGUI<>(Lang.INVENTORY_REQUIREMENTS.toString(), QuestObjectLocation.OTHER, QuestsAPI.requirements.values(), requirements -> {
						RequirementDependentReward.this.requirements = requirements;
						ItemUtils.name(current, Lang.requirements.format(requirements.size()));
						reopen();
					}, requirements).create(p);
					break;
				case 1:
					new QuestObjectGUI<>(Lang.INVENTORY_REWARDS.toString(), QuestObjectLocation.OTHER, QuestsAPI.rewards.values(), rewards -> {
						RequirementDependentReward.this.rewards = rewards;
						ItemUtils.name(current, Lang.rewards.format(rewards.size()));
						reopen();
					}, rewards).create(p);
					break;
				case 4:
					event.updateItemLore(getLore());
					event.reopenGUI();
					break;
				}
				return false;
			}
		}.create(event.getPlayer());
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("requirements", Utils.serializeList(requirements, AbstractRequirement::serialize));
		datas.put("rewards", Utils.serializeList(rewards, AbstractReward::serialize));
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		requirements = QuestObject.deserializeList((List<Map<?, ?>>) savedDatas.get("requirements"), AbstractRequirement::deserialize);
		rewards = QuestObject.deserializeList((List<Map<?, ?>>) savedDatas.get("rewards"), AbstractReward::deserialize);
	}
	
}
