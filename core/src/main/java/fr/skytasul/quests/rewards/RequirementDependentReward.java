package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class RequirementDependentReward extends AbstractReward {
	
	private List<AbstractRequirement> requirements;
	private List<AbstractReward> rewards;
	
	public RequirementDependentReward() {
		this(null, new ArrayList<>(), new ArrayList<>());
	}
	
	public RequirementDependentReward(String customDescription, List<AbstractRequirement> requirements,
			List<AbstractReward> rewards) {
		super(customDescription);
		this.requirements = requirements;
		this.rewards = rewards;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		requirements.forEach(req -> req.attach(quest));
		rewards.forEach(rew -> rew.attach(quest));
	}
	
	@Override
	public void detach() {
		super.detach();
		requirements.forEach(AbstractRequirement::detach);
		rewards.forEach(AbstractReward::detach);
	}
	
	@Override
	public List<String> give(Player p) throws InterruptingBranchException {
		if (requirements.stream().allMatch(requirement -> requirement.test(p))) return Utils.giveRewards(p, rewards);
		return null;
	}
	
	@Override
	public boolean isAsync() {
		return rewards.stream().anyMatch(AbstractReward::isAsync);
	}
	
	@Override
	public AbstractReward clone() {
		return new RequirementDependentReward(getCustomDescription(), new ArrayList<>(requirements),
				new ArrayList<>(rewards));
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return requirements.stream().allMatch(req -> req.test(p)) ?
				rewards
				.stream()
				.map(xreq -> xreq.getDescription(p))
				.filter(Objects::nonNull)
				.collect(Collectors.joining("{JOIN}"))
				: null;
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.requirements.format(requirements.size()));
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
					QuestsAPI.getRequirements().createGUI(QuestObjectLocation.OTHER, requirements -> {
						RequirementDependentReward.this.requirements = requirements;
						ItemUtils.name(current, "§b" + Lang.requirements.format(requirements.size()));
						reopen();
					}, requirements).create(p);
					break;
				case 1:
					QuestsAPI.getRewards().createGUI(QuestObjectLocation.OTHER, rewards -> {
						RequirementDependentReward.this.rewards = rewards;
						ItemUtils.name(current, "§a" + Lang.rewards.format(rewards.size()));
						reopen();
					}, rewards).create(p);
					break;
				case 4:
					event.reopenGUI();
					break;
				}
				return false;
			}
		}.create(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("requirements", SerializableObject.serializeList(requirements));
		section.set("rewards", SerializableObject.serializeList(rewards));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		requirements = SerializableObject.deserializeList(section.getMapList("requirements"), AbstractRequirement::deserialize);
		rewards = SerializableObject.deserializeList(section.getMapList("rewards"), AbstractReward::deserialize);
	}
	
}
