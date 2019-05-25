package fr.skytasul.quests.gui.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextListEditor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class ItemCreatorGUI implements CustomInventory {

	private Inventory inv;
	private Player p;
	private RunnableObj run;

	public ItemCreatorGUI(RunnableObj end){
		run = end;
	}

	private XMaterial type;
	private String name;
	private List<String> lore = new ArrayList<>();
	private boolean quest = false;
	private boolean flags = false;


	public Inventory open(Player p) {
		this.p = p;
		inv = Bukkit.createInventory(null, 18, Lang.INVENTORY_CREATOR.toString());

		inv.setItem(0, ItemUtils.item(XMaterial.ARROW, Lang.itemType.toString()));
		inv.setItem(1, ItemUtils.itemSwitch(Lang.itemFlags.toString(), false));
		inv.setItem(3, ItemUtils.item(XMaterial.NAME_TAG, Lang.itemName.toString()));
		inv.setItem(4, ItemUtils.item(XMaterial.FEATHER, Lang.itemLore.toString()));
		inv.setItem(6, ItemUtils.item(QuestsConfiguration.getItemMaterial(), Lang.itemQuest.toString() + " §c" + Lang.No.toString()));
		inv.setItem(8, ItemUtils.itemCancel());
		inv.setItem(17, ItemUtils.itemDone());
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
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				type = (XMaterial) obj;
				reopen();
			}, new MaterialParser(true)));;
			break;

		case 1:
			flags = ItemUtils.toggle(current);
			refresh();
			break;

		case 3:
			Lang.CHOOSE_ITEM_NAME.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				name = (String) obj;
				reopen();
			}));
			break;

		case 4:
			Lang.CHOOSE_ITEM_LORE.send(p);
			Editor.enterOrLeave(p, new TextListEditor(p, new RunnableObj() {
				public void run(Object obj) {
					lore = (List<String>) obj;
					reopen();
				}
			}, lore));
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
			run.run(null);
			break;

		case 17: //VALIDATE
			if (current.getType() == Material.DIAMOND){
				Inventories.closeAndExit(p);
				run.run(build());
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

		if (lore != null) ItemUtils.lore(is, lore.toArray(new String[0]));

		if (quest) ItemUtils.loreAdd(is, " ", Lang.QuestItemLore.toString());
		return is;
	}

}
