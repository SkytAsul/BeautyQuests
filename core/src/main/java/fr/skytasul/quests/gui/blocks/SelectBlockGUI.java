package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.BQBlock;

public class SelectBlockGUI implements CustomInventory{
	
	private static final int AMOUNT_SLOT = 1;
	private static final int NAME_SLOT = 2;
	private static final int TYPE_SLOT = 4;
	private static final int DATA_SLOT = 5;
	private static final int TAG_SLOT = 6;
	private static final int FINISH_SLOT = 8;
	
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	private boolean allowAmount;
	private BiConsumer<BQBlock, Integer> run;
	
	public Inventory inv;
	
	private XMaterial type = XMaterial.STONE;
	private String customName = null;
	private String blockData = null;
	private String tag = null;
	private int amount = 1;
	
	public SelectBlockGUI(boolean allowAmount, BiConsumer<BQBlock, Integer> run) {
		this.allowAmount = allowAmount;
		this.run = run;
	}
	
	public String name() {
		return Lang.INVENTORY_BLOCK.toString();
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	@Override
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, name());
		
		if (allowAmount)
			inv.setItem(AMOUNT_SLOT, item(XMaterial.REDSTONE, Lang.Amount.format(amount)));
		inv.setItem(NAME_SLOT,
				item(XMaterial.NAME_TAG, Lang.blockName.toString(), QuestOption.formatNullableValue(null, true)));
		if (NMS.getMCVersion() >= 13) inv.setItem(DATA_SLOT, item(XMaterial.COMMAND_BLOCK, Lang.blockData.toString(), Lang.NotSet.toString()));
		if (NMS.getMCVersion() >= 13) inv.setItem(TAG_SLOT, item(XMaterial.FILLED_MAP, Lang.blockTag.toString(), QuestOption.formatDescription(Lang.blockTagLore.toString()), "", Lang.NotSet.toString()));
		inv.setItem(FINISH_SLOT, done.clone());
		updateTypeItem();
		
		return inv = p.openInventory(inv).getTopInventory();
	}

	private void updateTypeItem() {
		inv.setItem(TYPE_SLOT, item(type, Lang.materialName.format(type.name())));
		if (inv.getItem(TYPE_SLOT) == null || inv.getItem(TYPE_SLOT).getType() == Material.AIR) { // means that the material cannot be treated as an inventory item (ex: fire)
			inv.setItem(TYPE_SLOT, item(XMaterial.STONE, Lang.materialName.format(type.name()), QuestOption.formatDescription(Lang.materialNotItemLore.format(type.name()))));
		}
		if (tag == null) ItemUtils.addEnchant(inv.getItem(TYPE_SLOT), Enchantment.DURABILITY, 1);
	}
	
	private void resetBlockData() {
		if (blockData == null) return;
		blockData = null;
		ItemStack item = inv.getItem(DATA_SLOT);
		ItemUtils.removeEnchant(item, Enchantment.DURABILITY);
		ItemUtils.lore(item, Lang.NotSet.toString());
	}
	
	private void resetTag() {
		if (tag == null) return;
		tag = null;
		ItemStack item = inv.getItem(TAG_SLOT);
		ItemUtils.removeEnchant(item, Enchantment.DURABILITY);
		ItemUtils.lore(item, QuestOption.formatDescription(Lang.blockTagLore.toString()), "", Lang.NotSet.toString());
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		default:
			break;
			
		case AMOUNT_SLOT:
			Lang.BLOCKS_AMOUNT.send(p);
			new TextEditor<>(p, () -> openLastInv(p), obj -> {
				amount = obj;
				ItemUtils.name(current, Lang.Amount.format(amount));
				openLastInv(p);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;
			
		case NAME_SLOT:
			Lang.BLOCK_NAME.send(p);
			new TextEditor<String>(p, () -> openLastInv(p), obj -> {
				customName = obj;
				ItemUtils.lore(current, QuestOption.formatNullableValue(customName, customName == null));
				openLastInv(p);
			}).passNullIntoEndConsumer().enter();
			break;

		case TYPE_SLOT:
			Lang.BLOCK_NAME.send(p);
			new TextEditor<>(p, () -> openLastInv(p), type -> {
				this.type = type;
				if (blockData != null) {
					try {
						Bukkit.createBlockData(type.parseMaterial(), blockData);
					}catch (Exception ex) {
						Lang.INVALID_BLOCK_DATA.send(p, blockData, type.name());
						resetBlockData();
					}
				}
				resetTag();
				updateTypeItem();
				openLastInv(p);
			}, MaterialParser.BLOCK_PARSER).enter();
			break;
		
		case DATA_SLOT:
			Lang.BLOCK_DATA.send(p, String.join(", ", NMS.getNMS().getAvailableBlockProperties(type.parseMaterial())));
			new TextEditor<>(p, () -> openLastInv(p), obj -> {
				String tmp = "[" + obj + "]";
				try {
					Bukkit.createBlockData(type.parseMaterial(), tmp);
					blockData = tmp;
					ItemUtils.lore(current, blockData);
					ItemUtils.addEnchant(current, Enchantment.DURABILITY, 1);
					if (tag != null) {
						resetTag();
						updateTypeItem();
					}
				}catch (Exception ex) {
					Lang.INVALID_BLOCK_DATA.send(p, tmp, type.name());
				}
				openLastInv(p);
			}, () -> {
				resetBlockData();
				openLastInv(p);
			}).useStrippedMessage().enter();
			break;
		
		case TAG_SLOT:
			Lang.BLOCK_TAGS.send(p, String.join(", ", NMS.getNMS().getAvailableBlockTags()));
			new TextEditor<>(p, () -> openLastInv(p), obj -> {
				NamespacedKey key = NamespacedKey.fromString((String) obj);
				if (key == null || Bukkit.getTag("blocks", key, Material.class) == null) {
					Lang.INVALID_BLOCK_TAG.send(p, obj);
				}else {
					tag = (String) obj;
					type = XMaterial.STONE;
					ItemUtils.lore(current, QuestOption.formatDescription(Lang.blockTagLore.toString()), "", Lang.optionValue.format(tag));
					ItemUtils.addEnchant(current, Enchantment.DURABILITY, 1);
					resetBlockData();
					updateTypeItem();
				}
				openLastInv(p);
			}).useStrippedMessage().enter();
			break;

		case FINISH_SLOT:
			Inventories.closeAndExit(p);
			BQBlock block;
			if (blockData != null) {
				block = new Post1_13.BQBlockData(customName, Bukkit.createBlockData(type.parseMaterial(), blockData));
			}else if (tag != null) {
				block = new Post1_13.BQBlockTag(customName, tag);
			}else {
				block = new BQBlock.BQBlockMaterial(customName, type);
			}
			run.accept(block, amount);
			break;
			
		}
		return true;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REOPEN;
	}

}