package fr.skytasul.quests.gui.quests;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public class ChooseQuestGUI extends PagedGUI<Quest> {
	
	private Consumer<Quest> run;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public ChooseQuestGUI(List<Quest> quests, Consumer<Quest> run){
		super(Lang.INVENTORY_CHOOSE.toString(), DyeColor.MAGENTA, quests);
		Validate.notNull(run, "Runnable cannot be null");
		
		this.run = run;
	}
	
	public Inventory open(Player p){
		if (objects.size() == 0) {
			click(null, null, null);
			return null;
		}else if (objects.size() == 1) {
			click(objects.get(0), null, null);
			return null;
		}

		return super.open(p);
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

	@Override
	public ItemStack getItemStack(Quest object) {
		return ItemUtils.item(object.getQuestMaterial(), ChatColor.YELLOW + object.getName(), object.getDescription());
	}

	@Override
	public void click(Quest existing, ItemStack item, ClickType clickType) {
		if (inv != null) Inventories.closeAndExit(p);
		run.accept(existing);
	}

}