package fr.skytasul.quests.requirements;

import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ImmutableMap;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.StaticPagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.items.ItemComparisonGUI;

public class EquipmentRequirement extends AbstractRequirement {
	
	private EquipmentSlot slot;
	private ItemStack item;
	private ItemComparisonMap comparisons;
	
	public EquipmentRequirement() {}
	
	public EquipmentRequirement(String customDescription, String customReason, EquipmentSlot slot, ItemStack item,
			ItemComparisonMap comparisons) {
		super(customDescription, customReason);
		this.slot = slot;
		this.item = item;
		this.comparisons = comparisons;
	}
	
	@Override
	public boolean test(Player p) {
		ItemStack playerItem = p.getInventory().getItem(slot);
		return comparisons.isSimilar(playerItem, item) && playerItem.getAmount() >= item.getAmount();
	}
	
	@Override
	public AbstractRequirement clone() {
		return new EquipmentRequirement(getCustomDescription(), getCustomReason(), slot, item, comparisons);
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		if (slot != null) {
			loreBuilder.addDescription(slot.name() + ": " + ItemUtils.getName(item));
		}
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation()) comparisons = new ItemComparisonMap();
		
		new EquipmentSlotGUI(newSlot -> {
			if (newSlot == null) {
				event.cancel();
				return;
			}
			
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemSelection(newItem -> {
				if (newItem == null) {
					event.cancel();
					return;
				}

				slot = newSlot;
				item = newItem;
				
				new ItemComparisonGUI(comparisons, event::reopenGUI).open(event.getPlayer());
			}, true).open(event.getPlayer());
			
		}).allowCancel().open(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("slot", slot.name());
		section.set("item", item);
		if (!comparisons.isDefault()) section.set("comparisons", comparisons.getNotDefault());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		slot = EquipmentSlot.valueOf(section.getString("slot"));
		item = section.getItemStack("item");
		comparisons = section.contains("comparisons") ? new ItemComparisonMap(section.getConfigurationSection("comparisons")) : new ItemComparisonMap();
	}
	
	public static class EquipmentSlotGUI extends StaticPagedGUI<EquipmentSlot>{
		
		private static final Map<EquipmentSlot, ItemStack> OBJECTS = ImmutableMap.<EquipmentSlot, ItemStack>builder()
				.put(EquipmentSlot.HAND, ItemUtils.item(XMaterial.GOLDEN_SWORD, "§6Main hand"))
				.put(EquipmentSlot.OFF_HAND, ItemUtils.item(XMaterial.SHIELD, "§eOff hand"))
				.put(EquipmentSlot.FEET, ItemUtils.item(XMaterial.IRON_BOOTS, "§bFeet"))
				.put(EquipmentSlot.LEGS, ItemUtils.item(XMaterial.IRON_LEGGINGS, "§bLegs"))
				.put(EquipmentSlot.CHEST, ItemUtils.item(XMaterial.ELYTRA, "§bChest"))
				.put(EquipmentSlot.HEAD, ItemUtils.item(XMaterial.TURTLE_HELMET.or(XMaterial.IRON_HELMET), "§bHead"))
				.build();

		public EquipmentSlotGUI(Consumer<EquipmentSlot> clicked) {
			super(Lang.INVENTORY_EQUIPMENT_SLOTS.toString(), DyeColor.BROWN, OBJECTS, clicked, EquipmentSlot::name);
		}
		
	}
	
}
