package fr.skytasul.quests.gui.blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.utils.nms.NMS;

public class SelectBlockGUI extends LayoutedGUI.LayoutedRowsGUI {

	private BiConsumer<BQBlock, Integer> run;

	private XMaterial type = XMaterial.STONE;
	private String customName = null;
	private String blockData = null;
	private String tag = null;
	private int amount = 1;

	public SelectBlockGUI(boolean allowAmount, BiConsumer<BQBlock, Integer> run) {
		super(Lang.INVENTORY_BLOCK.toString(), new HashMap<>(), StandardCloseBehavior.REOPEN, 1);
		this.run = run;

		if (allowAmount)
			buttons.put(1,
					LayoutedButton.create(XMaterial.REDSTONE, () -> Lang.Amount.quickFormat("amount", amount),
							Collections.emptyList(), this::amountClick));
		buttons.put(2, LayoutedButton.create(XMaterial.NAME_TAG, Lang.blockName.toString(),
				Arrays.asList(QuestOption.formatNullableValue(customName, customName == null)), this::nameClick));
		buttons.put(4, new LayoutedButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				typeClick(event);
			}

			@Override
			public void place(@NotNull Inventory inventory, int slot) {
				inventory.setItem(slot, ItemUtils.item(type, Lang.materialName.quickFormat("block_type", type.name())));
				if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == Material.AIR) {
					// means that the material cannot be treated as an inventory item (ie: fire)
					inventory.setItem(slot,
							ItemUtils.item(XMaterial.STONE, Lang.materialName.quickFormat("block_type", type.name()),
									QuestOption.formatDescription(
											Lang.materialNotItemLore.quickFormat("block_type", type.name()))));
				}
				if (tag == null)
					ItemUtils.addEnchant(inventory.getItem(slot), Enchantment.DURABILITY, 1);
			}

		});
		if (MinecraftVersion.MAJOR >= 13) {
			buttons.put(5, LayoutedButton.create(() -> {
				ItemStack item = ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.blockData.toString(),
						QuestOption.formatNullableValue(blockData, blockData == null));
				if (blockData != null)
					ItemUtils.addEnchant(item, Enchantment.DAMAGE_ALL, 1);
				return item;
			}, this::dataClick));

			buttons.put(6, LayoutedButton.create(() -> {
				ItemStack item = ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.blockTag.toString(),
						QuestOption.formatDescription(Lang.blockTagLore.toString()), "",
						QuestOption.formatNullableValue(tag, tag == null));
				if (tag != null)
					ItemUtils.addEnchant(item, Enchantment.DAMAGE_ALL, 1);
				return item;
			}, this::tagClick));
		}

		buttons.put(8, LayoutedButton.create(ItemUtils.itemDone, this::doneClick));
	}

	private void amountClick(LayoutedClickEvent event) {
		Lang.BLOCKS_AMOUNT.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
			amount = obj;
			event.refreshItemReopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
	}

	private void nameClick(LayoutedClickEvent event) {
		Lang.BLOCK_NAME.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
			customName = obj;
			event.refreshItemReopen();
		}).passNullIntoEndConsumer().start();
	}

	private void typeClick(LayoutedClickEvent event) {
		Lang.BLOCK_NAME.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, type -> {
			this.type = type;
			if (blockData != null) {
				try {
					Bukkit.createBlockData(type.parseMaterial(), blockData);
				} catch (Exception ex) {
					Lang.INVALID_BLOCK_DATA.send(event.getPlayer(),
							PlaceholderRegistry.of("block_data", blockData, "block_material", type.name()));
					blockData = null;
				}
			}
			event.refreshGuiReopen();
		}, QuestsPlugin.getPlugin().getEditorManager().getFactory().getMaterialParser(false, true)).start();
	}

	private void dataClick(LayoutedClickEvent event) {
		Lang.BLOCK_DATA.quickSend(event.getPlayer(), "available_datas",
				String.join(", ", NMS.getNMS().getAvailableBlockProperties(type.parseMaterial())));
		new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
			String tmp = "[" + obj + "]";
			try {
				Bukkit.createBlockData(type.parseMaterial(), tmp);
				blockData = tmp;
				tag = null;
			} catch (Exception ex) {
				Lang.INVALID_BLOCK_DATA.send(event.getPlayer(),
						PlaceholderRegistry.of("block_data", tmp, "block_material", type.name()));
			}
			event.refreshGuiReopen();
		}, () -> {
			blockData = null;
			event.refreshGuiReopen();
		}).useStrippedMessage().start();
	}

	private void tagClick(LayoutedClickEvent event) {
		Lang.BLOCK_TAGS.quickSend(event.getPlayer(), "available_tags",
				String.join(", ", NMS.getNMS().getAvailableBlockTags()));
		new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
			NamespacedKey key = NamespacedKey.fromString((String) obj);
			if (key == null || Bukkit.getTag("blocks", key, Material.class) == null) {
				Lang.INVALID_BLOCK_TAG.quickSend(event.getPlayer(), "block_tag", obj);
			} else {
				tag = (String) obj;
				type = XMaterial.STONE;
				blockData = null;
			}
			event.refreshGuiReopen();
		}).useStrippedMessage().start();
	}

	private void doneClick(LayoutedClickEvent event) {
		event.close();
		BQBlock block;
		if (blockData != null) {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createBlockdata(blockData, customName);
		} else if (tag != null) {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createTag(tag, customName);
		} else {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createSimple(type, customName);
		}
		run.accept(block, amount);
	}

}
