package fr.skytasul.quests.gui.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.TextListEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class ItemCreatorGUI implements CustomInventory {

	private Inventory inv;
	private Player p;
	private Consumer<ItemStack> run;
	private boolean allowCancel;
	
	public ItemCreatorGUI(Consumer<ItemStack> end, boolean allowCancel){
		run = end;
		this.allowCancel = allowCancel;
	}

	private XMaterial type;
	private int amount = 1;
	private String name;
	private List<String> lore = new ArrayList<>();
	private boolean quest = false;
	private boolean flags = false;
	

	public Inventory open(Player p) {
		this.p = p;
		inv = Bukkit.createInventory(null, 18, Lang.INVENTORY_CREATOR.toString());

		inv.setItem(0, ItemUtils.item(XMaterial.ARROW, Lang.itemType.toString()));
		inv.setItem(1, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(1)));
		inv.setItem(2, ItemUtils.itemSwitch(Lang.itemFlags.toString(), false));
		inv.setItem(3, ItemUtils.item(XMaterial.NAME_TAG, Lang.itemName.toString()));
		inv.setItem(4, ItemUtils.item(XMaterial.FEATHER, Lang.itemLore.toString()));
		inv.setItem(6, ItemUtils.item(QuestsConfiguration.getItemMaterial(), Lang.itemQuest.toString() + " §c" + Lang.No.toString()));
		if (allowCancel) inv.setItem(8, ItemUtils.itemCancel);
		inv.setItem(17, ItemUtils.itemDone);
		inv.getItem(17).setType(Material.COAL);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	private void reopen(){
		p.openInventory(inv);
		refresh();
	}

	private void refresh(){
		if (type != null){
			inv.setItem(13, build());
			inv.getItem(17).setType(Material.DIAMOND);
		}
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){
		case 0:
			Lang.CHOOSE_ITEM_TYPE.send(p);
			new TextEditor<>(p, () -> reopen(), obj -> {
				type = obj;
				reopen();
			}, new MaterialParser(true, false)).enter();
			break;

		case 1:
			Lang.CHOOSE_ITEM_AMOUNT.send(p);
			new TextEditor<>(p, () -> reopen(), obj -> {
				amount = obj;
				ItemUtils.name(current, Lang.Amount.format(amount));
				reopen();
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;
		
		case 2:
			flags = ItemUtils.toggle(current);
			refresh();
			break;

		case 3:
			Lang.CHOOSE_ITEM_NAME.send(p);
			new TextEditor<String>(p, () -> reopen(), obj -> {
				name = obj;
				reopen();
			}).enter();
			break;

		case 4:
			Lang.CHOOSE_ITEM_LORE.send(p);
			new TextListEditor(p, list -> {
				lore = list;
				reopen();
			}, lore).enter();
			break;

		case 6:
			if (!quest){
				ItemUtils.name(current, Lang.itemQuest.toString() + " §a" + Lang.Yes.toString());
				quest = true;
			}else {
				ItemUtils.name(current, Lang.itemQuest.toString() + " §c" + Lang.No.toString());
				quest = false;
			}
			refresh();
			break;

		case 8:
			Inventories.closeAndExit(p);
			run.accept(null);
			break;

		case 17: //VALIDATE
			if (current.getType() == Material.DIAMOND){
				Inventories.closeAndExit(p);
				run.accept(build());
			}
			break;

		case 13: //GIVE
			p.getOpenInventory().setCursor(build());
			break;

		}
		return true;
	}

	private ItemStack build(){
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		if (name != null) im.setDisplayName(name);
		if (flags) im.addItemFlags(ItemFlag.values());
		is.setItemMeta(im);
		is.setAmount(amount);

		if (lore != null) ItemUtils.lore(is, lore.toArray(new String[0]));

		if (quest) ItemUtils.loreAdd(is, " ", Lang.QuestItemLore.toString());
		return is;
	}

}
