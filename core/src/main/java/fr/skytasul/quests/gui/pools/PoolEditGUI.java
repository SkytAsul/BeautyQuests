package fr.skytasul.quests.gui.pools;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class PoolEditGUI implements CustomInventory {
	
	private final Runnable end;
	
	private String name;
	private int maxQuests = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	
	public PoolEditGUI(Runnable end) {
		this.end = end;
	}
	
	public PoolEditGUI(Runnable end, QuestPool copyFrom) {
		this(end);
		
		name = copyFrom.getName();
		maxQuests = copyFrom.getMaxQuests();
		redoAllowed = copyFrom.isRedoAllowed();
		timeDiff = copyFrom.getTimeDiff();
	}
	
	public void copyTo(QuestPool pool) {
		pool.setName(name);
		pool.setMaxQuests(maxQuests);
		pool.setRedoAllowed(redoAllowed);
		pool.setTimeDiff(timeDiff);
	}
	
	private String getTimeLore() {
		return Lang.optionValue.format(timeDiff + " milliseconds (" + TimeUnit.MILLISECONDS.toDays(timeDiff) + " days)");
	}
	
	@Override
	public Inventory open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, "Quest pool creation");
		
		inv.setItem(0, ItemUtils.item(XMaterial.NAME_TAG, "§e§lPool name", QuestOption.formatNullableValue(name)));
		inv.setItem(1, ItemUtils.item(XMaterial.REDSTONE, "§aMax quests", Lang.optionValue.format(maxQuests)));
		inv.setItem(2, ItemUtils.item(XMaterial.CLOCK, "§bSet time between quests", getTimeLore()));
		inv.setItem(3, ItemUtils.itemSwitch("Is redo allowed", true));
		
		return p.openInventory(inv).getTopInventory();
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		return false;
	}
	
}
