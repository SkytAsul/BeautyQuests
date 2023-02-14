package fr.skytasul.quests.requirements;

import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.ImmutableMap;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ItemComparisonGUI;
import fr.skytasul.quests.gui.misc.ItemGUI;
import fr.skytasul.quests.gui.templates.StaticPagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class EquipmentRequirement extends AbstractRequirement {
	
	private EquipmentSlot slot;
	private ItemStack item;
	private ItemComparisonMap comparisons;
	
	public EquipmentRequirement() {}
	
	public EquipmentRequirement(EquipmentSlot slot, ItemStack item, ItemComparisonMap comparisons) {
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
		return new EquipmentRequirement(slot, item, comparisons);
	}
	
	@Override
	public String[] getLore() {
		if (slot == null) return null;
		return new String[] {
				QuestOption.formatNullableValue(slot.name() + " > " + ItemUtils.getName(item)),
				"",
				Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (event.isInCreation()) comparisons = new ItemComparisonMap();
		
		new EquipmentSlotGUI(newSlot -> {
			if (newSlot == null) {
				event.cancel();
				return;
			}
			
			new ItemGUI(newItem -> {
				slot = newSlot;
				item = newItem;
				
				new ItemComparisonGUI(comparisons, event::reopenGUI).create(event.getPlayer());
				
			}, event::cancel).create(event.getPlayer());
			
		}).allowCancel().create(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("slot", slot.name());
		section.set("item", item);
		if (!comparisons.isDefault()) section.set("comparisons", comparisons.getNotDefault());
	}
	
	@Override
	public void load(ConfigurationSection section) {
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
