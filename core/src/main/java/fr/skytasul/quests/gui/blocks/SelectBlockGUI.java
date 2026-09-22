package fr.skytasul.quests.gui.blocks;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ImmutableList;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.editors.parsers.CollectionParser;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		buttons.put(2, LayoutedButton.createLoreValue(XMaterial.NAME_TAG, Lang.blockName.toString(), () -> customName,
				this::nameClick));
		buttons.put(4, new LayoutedButton() {

			@Override
			public void click(@NotNull LayoutedClickEvent event) {
				typeClick(event);
			}

			@Override
			public void place(@NotNull Inventory inventory, int slot) {
				XMaterial mat = type;
				if (!type.get().isItem())
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

		buttons.put(8, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone(), this::doneClick));
	}

	private void amountClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(event.getPlayer(),
				NumberParser.INTEGER_PARSER_STRICT_POSITIVE, event::reopen, newAmount -> {
					amount = newAmount;
					event.refreshItemReopen();
				})
				.setIndication(Lang.BLOCKS_AMOUNT.toString())
				.setInitialValue(amount)
				.build().start();
	}

	private void nameClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderString(event.getPlayer(), event::reopen, newName -> {
					customName = newName;
					event.refreshItemReopen();
				})
				.setIndication(Lang.BLOCK_NAME.toString())
				.setInitialString(customName)
				.allowEmpty()
				.build().start();
	}

	private void typeClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderParser(event.getPlayer(),
						QuestsPlugin.getPlugin().getEditorManager().getFactory().getMaterialParser(false, true),
						event::reopen, newType -> {
							this.type = newType;
							if (blockData != null) {
								try {
									Bukkit.createBlockData(type.get(), blockData);
								} catch (Exception ex) {
									Lang.INVALID_BLOCK_DATA.send(event.getPlayer(),
											PlaceholderRegistry.of("block_data", blockData, "block_material",
													type.name()));
									blockData = null;
								}
							}
							event.refreshGuiReopen();
						})
				.setIndication(Lang.BLOCK_NAME.toString())
				.setInitialValue(type)
				.build().start();
	}

	private void dataClick(LayoutedClickEvent event) {
		String availableData = String.join(", ",
				BeautyQuests.getInstance().getInternalsAccess().getAvailableBlockProperties(type.get()));
		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderString(event.getPlayer(), event::reopen, obj -> {
					String tmp = "[" + obj + "]";
					try {
						Bukkit.createBlockData(type.get(), tmp);
						blockData = tmp;
						tag = null;
					} catch (Exception ex) {
						Lang.INVALID_BLOCK_DATA.send(event.getPlayer(),
								PlaceholderRegistry.of("block_data", tmp, "block_material", type.name()));
					}
					event.refreshGuiReopen();
				})
				.setIndication(Lang.BLOCK_DATA.quickFormat("available_datas", availableData))
				.setInitialString(blockData)
				.addReset(() -> {
					blockData = null;
					event.refreshGuiReopen();
				}, "reset")
				.build().start();
	}

	private void tagClick(LayoutedClickEvent event) {
		var tags = ImmutableList.copyOf(Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class));

		QuestsPlugin.getPlugin().getEditorManager().getFactory()
				.createTextEditorBuilderParser(event.getPlayer(), new CollectionParser<Tag<Material>>(tags) {
					@Override
					public @Nullable String serialize(@NotNull Tag<Material> value) {
						return processName(value.key().asString());
					}

					@Override
					protected String processName(String msg) {
						Key key = Key.key(msg);
						if (key.namespace().equals(Key.MINECRAFT_NAMESPACE))
							return key.value();
						return key.asString();
					}
				}, event::reopen, newTag -> {
					tag = newTag.key().asString();
					type = XMaterial.STONE;
					blockData = null;
					event.refreshGuiReopen();
				})
				.setIndication(Lang.BLOCK_TAG.toString())
				.setInitialString(tag)
				.build().start();
	}

	private void doneClick(LayoutedClickEvent event) {
		event.close();
		BQBlock block;
		if (blockData != null) {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager()
					.createBlockdata(Bukkit.createBlockData(type.get(), blockData), customName);
		} else if (tag != null) {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createTag(tag, customName);
		} else {
			block = BeautyQuests.getInstance().getAPI().getBlocksManager().createSimple(type, customName);
		}
		run.accept(block, amount);
	}

}
