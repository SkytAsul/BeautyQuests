package fr.skytasul.quests.gui.items;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cryptomorin.xseries.XMaterial;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;

public class ItemCreatorGUI extends LayoutedGUI.LayoutedRowsGUI {

	private final @NotNull Consumer<ItemStack> endCallback;
	private final @NotNull Runnable cancelCallback;

	private @Nullable XMaterial type;
	private int amount = 1;
	private @Nullable String name;
	private @Nullable String lore;
	private boolean quest = false;
	private boolean flags = false;

	public ItemCreatorGUI(@NotNull Consumer<ItemStack> endCallback, @NotNull Runnable cancelCallback) {
		super(Lang.INVENTORY_CREATOR.toString(), new HashMap<>(), new DelayCloseBehavior(cancelCallback), 2);
		this.endCallback = endCallback;
		this.cancelCallback = cancelCallback;

		super.buttons.put(0,
				LayoutedButton.create(
						() -> ItemUtils.item(type == null ? XMaterial.SPONGE : type, Lang.itemType.toString()),
						this::handleTypeClick));
		super.buttons.put(1,
				LayoutedButton.create(XMaterial.REDSTONE, () -> Lang.Amount.quickFormat("amount", amount), List.of(),
						this::handleAmountClick));
		super.buttons.put(2, LayoutedButton.createLoreValue(XMaterial.NAME_TAG, Lang.itemName.toString(), () -> name,
				this::handleNameClick));
		super.buttons.put(3, LayoutedButton.createLoreValue(XMaterial.FEATHER, Lang.itemLore.toString(),
				() -> lore, this::handleLoreClick));
		super.buttons.put(5,
				LayoutedButton.createSwitch(() -> flags, Lang.itemFlags.toString(), null, this::handleFlagsClick));
		super.buttons.put(6, LayoutedButton.createSwitch(() -> quest, Lang.itemQuest.toString(),
				List.of(QuestOption.formatDescription(Lang.itemQuestDescription.toString())), this::handleQuestItemClick));

		super.buttons.put(13, LayoutedButton.create(() -> isValid() ? generateItem() : null, this::handleGeneratedItemClick));

		super.buttons.put(8, LayoutedButton.create(
				QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel(), event -> cancelCallback.run()));
		super.buttons.put(17,
				LayoutedButton.create(
						() -> isValid()
								? QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone()
								: ItemUtils.loreAdd(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getNotDone(),
										Lang.itemNeedType.toString()),
						event -> {
							if (isValid())
								endCallback.accept(generateItem());
						}));
	}

	private boolean isValid() {
		return type != null;
	}

	private @NotNull ItemStack generateItem() {
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		if (name != null)
			im.setDisplayName(name);
		if (flags)
			ItemUtils.addSpecificFlags(im, is.getType());
		is.setItemMeta(im);
		is.setAmount(amount);

		if (lore != null)
			ItemUtils.lore(is, lore);

		if (quest)
			ItemUtils.loreAdd(is, " ", Lang.QuestItemLore.toString());
		return is;
	}

	private void handleGeneratedItemClick(LayoutedClickEvent event) {
		event.getPlayer().setItemOnCursor(generateItem());
	}

	private void handleTypeClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(
				event.getPlayer(),
				QuestsPlugin.getPlugin().getEditorManager().getFactory().getMaterialParser(true, false),
				event::reopen,
				material -> {
					type = material;
					event.refreshGuiReopen();
				})
				.setIndication(Lang.CHOOSE_ITEM_TYPE.toString())
				.setInitialValue(type)
				.build().start();
	}

	private void handleAmountClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(
				event.getPlayer(),
				NumberParser.INTEGER_PARSER_STRICT_POSITIVE,
				event::reopen,
				amount -> {
					this.amount = amount;
					event.refreshGuiReopen();
				})
				.setIndication(Lang.CHOOSE_ITEM_AMOUNT.toString())
				.setInitialString(Integer.toString(amount))
				.build().start();
	}

	private void handleFlagsClick(LayoutedClickEvent event) {
		flags = !flags;
		event.refreshGui();
	}

	private void handleNameClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderString(
				event.getPlayer(),
				event::reopen,
				name -> {
					this.name = name;
					event.refreshGuiReopen();
				})
				.setIndication(Lang.CHOOSE_ITEM_NAME.toString())
				.setInitialString(name)
				.allowEmpty()
				.build().start();
	}

	private void handleLoreClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderString(
				event.getPlayer(),
				event::reopen,
				lore -> {
					this.lore = lore;
					event.refreshGuiReopen();
				})
				.setIndication(Lang.CHOOSE_ITEM_LORE.toString())
				.setInitialString(lore)
				.allowMultiline().forceMultiline()
				.build().start();
	}

	private void handleQuestItemClick(LayoutedClickEvent event) {
		quest = !quest;
		event.refreshGui();
	}

}
