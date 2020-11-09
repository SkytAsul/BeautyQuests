package fr.skytasul.quests.gui.pools;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class PoolEditGUI implements CustomInventory {
	
	private final Supplier<QuestPool> poolSupplier;
	private final Runnable end;
	
	private String name;
	private int maxQuests = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	
	public PoolEditGUI(Supplier<QuestPool> poolSupplier, Runnable end) {
		this.poolSupplier = poolSupplier;
		this.end = end;
	}
	
	public PoolEditGUI(Supplier<QuestPool> poolSupplier, Runnable end, QuestPool copyFrom) {
		this(poolSupplier, end);
		copyFrom(copyFrom);
	}
	
	public void copyFrom(QuestPool pool) {
		name = pool.getName();
		maxQuests = pool.getMaxQuests();
		redoAllowed = pool.isRedoAllowed();
		timeDiff = pool.getTimeDiff();
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
		inv.setItem(3, ItemUtils.itemSwitch("Is redo allowed", redoAllowed));
		
		inv.setItem(7, ItemUtils.itemCancel);
		inv.setItem(8, ItemUtils.itemDone);
		
		return p.openInventory(inv).getTopInventory();
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		Runnable reopen = () -> p.openInventory(inv);
		switch (slot) {
		case 0:
			new TextEditor<String>(p, reopen, msg -> {
				name = msg;
				ItemUtils.lore(current, QuestOption.formatNullableValue(name));
				reopen.run();
			}).enter();
			break;
		case 1:
			new TextEditor<>(p, reopen, msg -> {
				maxQuests = msg;
				ItemUtils.lore(current, Lang.optionValue.format(maxQuests));
				reopen.run();
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;
		case 2:
			new TextEditor<>(p, reopen, msg -> {
				timeDiff = TimeUnit.DAYS.toMillis(msg);
				ItemUtils.lore(current, getTimeLore());
				reopen.run();
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
			break;
		case 3:
			redoAllowed = ItemUtils.toggle(current);
			break;
		
		case 7:
			end.run();
			break;
		case 8:
			QuestPool pool = poolSupplier.get();
			copyTo(pool);
			BeautyQuests.getInstance().getPoolsManager().save(pool);
			end.run();
			break;
		}
		return true;
	}
	
}
