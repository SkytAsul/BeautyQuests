package fr.skytasul.quests.gui.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.TextListEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;

public class ItemCreatorGUI extends AbstractGui {

	private Consumer<ItemStack> run;
	private boolean allowCancel;

	private XMaterial type;
	private int amount = 1;
	private String name;
	private List<String> lore = new ArrayList<>();
	private boolean quest = false;
	private boolean flags = false;

	public ItemCreatorGUI(Consumer<ItemStack> end, boolean allowCancel) {
		run = end;
		this.allowCancel = allowCancel;
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 18, Lang.INVENTORY_CREATOR.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(0, ItemUtils.item(XMaterial.GRASS_BLOCK, Lang.itemType.toString()));
		inventory.setItem(1, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.quickFormat("amount", 1)));
		inventory.setItem(2, ItemUtils.itemSwitch(Lang.itemFlags.toString(), false));
		inventory.setItem(3, ItemUtils.item(XMaterial.NAME_TAG, Lang.itemName.toString()));
		inventory.setItem(4, ItemUtils.item(XMaterial.FEATHER, Lang.itemLore.toString()));
		inventory.setItem(6, ItemUtils.item(XMaterial.BOOK, Lang.itemQuest.toString() + " §c" + Lang.No.toString()));
		if (allowCancel)
			inventory.setItem(8, ItemUtils.itemCancel);
		inventory.setItem(17, ItemUtils.itemDone);
		inventory.getItem(17).setType(Material.COAL);
	}

	private void refresh() {
		if (type != null) {
			getInventory().setItem(13, build());
			if (getInventory().getItem(17).getType() != Material.DIAMOND)
				getInventory().getItem(17).setType(Material.DIAMOND);
		}
	}

	@Override
	public void onClick(GuiClickEvent event) {
		switch (event.getSlot()) {
			case 0:
				Lang.CHOOSE_ITEM_TYPE.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					type = obj;
					event.reopen();
				}, QuestsPlugin.getPlugin().getEditorManager().getFactory().getMaterialParser(true, false)).start();
				break;

			case 1:
				Lang.CHOOSE_ITEM_AMOUNT.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					amount = /* Math.min(obj, 64) */ obj;
					ItemUtils.name(event.getClicked(), Lang.Amount.quickFormat("amount", amount));
					event.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
				break;

			case 2:
				flags = ItemUtils.toggleSwitch(event.getClicked());
				refresh();
				break;

			case 3:
				Lang.CHOOSE_ITEM_NAME.send(event.getPlayer());
				new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
					name = obj;
					event.reopen();
				}).start();
				break;

			case 4:
				Lang.CHOOSE_ITEM_LORE.send(event.getPlayer());
				new TextListEditor(event.getPlayer(), list -> {
					lore = list;
					event.reopen();
				}, lore).start();
				break;

			case 6:
				if (!quest) {
					ItemUtils.name(event.getClicked(), Lang.itemQuest.toString() + " §a" + Lang.Yes.toString());
					quest = true;
				} else {
					ItemUtils.name(event.getClicked(), Lang.itemQuest.toString() + " §c" + Lang.No.toString());
					quest = false;
				}
				refresh();
				break;

			case 8:
				close(event.getPlayer());
				run.accept(null);
				break;

			case 17: // VALIDATE
				if (event.getClicked().getType() == Material.DIAMOND) {
					close(event.getPlayer());
					run.accept(build());
				}
				break;

			case 13: // GIVE
				if (type != null)
					event.getPlayer().getOpenInventory().setCursor(build());
				break;

		}
	}

	private ItemStack build() {
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		if (name != null)
			im.setDisplayName(name);
		if (flags)
			im.addItemFlags(ItemFlag.values());
		is.setItemMeta(im);
		is.setAmount(amount);

		if (lore != null)
			ItemUtils.lore(is, lore);

		if (quest)
			ItemUtils.loreAdd(is, " ", Lang.QuestItemLore.toString());
		return is;
	}

}
