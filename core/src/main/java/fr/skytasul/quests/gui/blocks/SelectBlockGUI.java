package fr.skytasul.quests.gui.blocks;

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
import fr.skytasul.quests.utils.compatibility.Post1_13;
import fr.skytasul.quests.utils.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiConsumer;

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
				XMaterial mat = type;
				if (MinecraftVersion.MAJOR >= 13 && !Post1_13.isItem(type.parseMaterial()))
					mat = XMaterial.STONE;
				placeInternal(inventory, slot, mat);

				if (tag == null)
					ItemUtils.setGlittering(inventory.getItem(slot), true);
			}

			private void placeInternal(@NotNull Inventory inventory, int slot, @NotNull XMaterial material) {
				ItemStack item = ItemUtils.item(material, Lang.materialName.quickFormat("block_type", type.name()));
				if (material != type)
					ItemUtils.lore(item,
							QuestOption.formatDescription(Lang.materialNotItemLore.quickFormat("block_type", type.name())));

				inventory.setItem(slot, item);

				item = inventory.getItem(slot);
				if (item == null || item.getType() == Material.AIR)
					placeInternal(inventory, slot, XMaterial.STONE);
			}

		});
		if (MinecraftVersion.MAJOR >= 13) {
			buttons.put(5, LayoutedButton.create(() -> {
				ItemStack item = ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.blockData.toString(),
						QuestOption.formatNullableValue(blockData, blockData == null));
				if (blockData != null)
					ItemUtils.setGlittering(item, true);
				return item;
			}, this::dataClick));

			buttons.put(6, LayoutedButton.create(() -> {
				ItemStack item = ItemUtils.item(XMaterial.FILLED_MAP, Lang.blockTag.toString(),
						QuestOption.formatDescription(Lang.blockTagLore.toString()), "",
						QuestOption.formatNullableValue(tag, tag == null));
				if (tag != null)
					ItemUtils.setGlittering(item, true);
				return item;
			}, this::tagClick));
		}

		buttons.put(8, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone(), this::doneClick));
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
			block = BeautyQuests.getInstance().getAPI().getBlocksManager()
					.createBlockdata(Bukkit.createBlockData(type.parseMaterial(), blockData), customName);
		} else if (tag != null) {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createTag(tag, customName);
		} else {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createSimple(type, customName);
		}
		run.accept(block, amount);
	}

}
