package fr.skytasul.quests.gui.pools;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class PoolEditGUI implements CustomInventory {
	
	private final Runnable end;
	
	private String hologram;
	private int maxQuests = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	private int npcID = -1;
	private boolean avoidDuplicates = true;
	
	private boolean canFinish = false;
	private QuestPool editing;
	
	public PoolEditGUI(Runnable end, QuestPool editing) {
		this.end = end;
		this.editing = editing;
		if (editing != null) {
			hologram = editing.getHologram();
			maxQuests = editing.getMaxQuests();
			redoAllowed = editing.isRedoAllowed();
			timeDiff = editing.getTimeDiff();
			npcID = editing.getNPCID();
			avoidDuplicates = editing.doAvoidDuplicates();
		}
	}
	
	private String[] getNPCLore() {
		return new String[] { "§8> " + Lang.requiredParameter.toString(), "", QuestOption.formatNullableValue("NPC #" + npcID) };
	}
	
	private String[] getHologramLore() {
		return new String[] { "", hologram == null ? QuestOption.formatNullableValue(Lang.PoolHologramText.toString()) + " " + Lang.defaultValue.toString() : Lang.optionValue.format(hologram) };
	}
	
	private String[] getAmountLore() {
		return new String[] { "", Lang.optionValue.format(maxQuests) };
	}
	
	private String[] getTimeLore() {
		return new String[] { "", Lang.optionValue.format(Utils.millisToHumanString(timeDiff)) };
	}
	
	private void reopen(Player p, Inventory inv, boolean reimplement) {
		if (reimplement) Inventories.put(p, this, inv);
		p.openInventory(inv);
	}
	
	private void handleDoneButton(Inventory inv) {
		boolean newState = /*name != null &&*/ npcID != -1;
		if (newState == canFinish) return;
		inv.getItem(8).setType((newState ? XMaterial.DIAMOND : XMaterial.CHARCOAL).parseMaterial());
		canFinish = newState;
	}
	
	@Override
	public Inventory open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_POOL_CREATE.toString());
		
		inv.setItem(0, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString(), getNPCLore()));
		inv.setItem(1, ItemUtils.item(XMaterial.OAK_SIGN, Lang.poolEditHologramText.toString(), getHologramLore()));
		inv.setItem(2, ItemUtils.item(XMaterial.REDSTONE, Lang.poolMaxQuests.toString(), getAmountLore()));
		inv.setItem(3, ItemUtils.item(XMaterial.CLOCK, Lang.poolTime.toString(), getTimeLore()));
		inv.setItem(4, ItemUtils.itemSwitch(Lang.poolRedo.toString(), redoAllowed));
		inv.setItem(5, ItemUtils.itemSwitch(Lang.poolAvoidDuplicates.toString(), avoidDuplicates, Lang.poolAvoidDuplicatesLore.toString()));
		
		inv.setItem(7, ItemUtils.itemCancel);
		inv.setItem(8, ItemUtils.item(XMaterial.CHARCOAL, Lang.done.toString()));
		handleDoneButton(inv);
		
		return p.openInventory(inv).getTopInventory();
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case 0:
			new SelectGUI(() -> reopen(p, inv, true), npc -> {
				npcID = npc.getId();
				ItemUtils.lore(current, getNPCLore());
				handleDoneButton(inv);
				reopen(p, inv, true);
			}).create(p);
			break;
		case 1:
			Lang.POOL_HOLOGRAM_TEXT.send(p);
			new TextEditor<String>(p, () -> reopen(p, inv, false), msg -> {
				hologram = msg;
				ItemUtils.lore(current, getHologramLore());
				reopen(p, inv, false);
			}).passNullIntoEndConsumer().enter();
			break;
		case 2:
			Lang.POOL_HOLOGRAM_MAXQUESTS.send(p);
			new TextEditor<>(p, () -> reopen(p, inv, false), msg -> {
				maxQuests = msg;
				ItemUtils.lore(current, getAmountLore());
				reopen(p, inv, false);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;
		case 3:
			Lang.POOL_HOLOGRAM_TIME.send(p);
			new TextEditor<>(p, () -> reopen(p, inv, false), msg -> {
				timeDiff = TimeUnit.DAYS.toMillis(msg);
				ItemUtils.lore(current, getTimeLore());
				reopen(p, inv, false);
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
			break;
		case 4:
			redoAllowed = ItemUtils.toggle(current);
			break;
		case 5:
			avoidDuplicates = ItemUtils.toggle(current);
			break;
		
		case 7:
			end.run();
			break;
		case 8:
			if (canFinish) {
				BeautyQuests.getInstance().getPoolsManager().createPool(editing, npcID, hologram, maxQuests, redoAllowed, timeDiff, avoidDuplicates);
				end.run();
			}else p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			break;
		}
		return true;
	}
	
}
