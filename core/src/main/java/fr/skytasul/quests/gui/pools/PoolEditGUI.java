package fr.skytasul.quests.gui.pools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class PoolEditGUI implements CustomInventory {
	
	private static final int SLOT_NPC = 1;
	private static final int SLOT_HOLOGRAM = 2;
	private static final int SLOT_MAX_QUESTS = 3;
	private static final int SLOT_TIME = 4;
	private static final int SLOT_REDO = 5;
	private static final int SLOT_DUPLICATE = 6;
	private static final int SLOT_REQUIREMENTS = 7;
	private static final int SLOT_CANCEL = 12;
	private static final int SLOT_CREATE = 14;
	
	private final Runnable end;
	
	private String hologram;
	private int maxQuests = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	private int npcID = -1;
	private boolean avoidDuplicates = true;
	private List<AbstractRequirement> requirements = new ArrayList<>();
	
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
			requirements = editing.getRequirements();
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
	
	private String[] getRequirementsLore() {
		return new String[] { "", QuestOption.formatDescription(Lang.requirements.format(requirements.size())) };
	}
	
	private void reopen(Player p, Inventory inv, boolean reimplement) {
		if (reimplement) Inventories.put(p, this, inv);
		p.openInventory(inv);
	}
	
	private void handleDoneButton(Inventory inv) {
		boolean newState = /*name != null &&*/ npcID != -1;
		if (newState == canFinish) return;
		inv.getItem(SLOT_CREATE).setType((newState ? XMaterial.DIAMOND : XMaterial.CHARCOAL).parseMaterial());
		canFinish = newState;
	}
	
	@Override
	public Inventory open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 18, Lang.INVENTORY_POOL_CREATE.toString());
		
		inv.setItem(SLOT_NPC, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString(), getNPCLore()));
		inv.setItem(SLOT_HOLOGRAM, ItemUtils.item(XMaterial.OAK_SIGN, Lang.poolEditHologramText.toString(), getHologramLore()));
		inv.setItem(SLOT_MAX_QUESTS, ItemUtils.item(XMaterial.REDSTONE, Lang.poolMaxQuests.toString(), getAmountLore()));
		inv.setItem(SLOT_TIME, ItemUtils.item(XMaterial.CLOCK, Lang.poolTime.toString(), getTimeLore()));
		inv.setItem(SLOT_REDO, ItemUtils.itemSwitch(Lang.poolRedo.toString(), redoAllowed));
		inv.setItem(SLOT_DUPLICATE, ItemUtils.itemSwitch(Lang.poolAvoidDuplicates.toString(), avoidDuplicates, Lang.poolAvoidDuplicatesLore.toString()));
		inv.setItem(SLOT_REQUIREMENTS, ItemUtils.item(XMaterial.NETHER_STAR, Lang.poolRequirements.toString(), getRequirementsLore()));
		
		inv.setItem(SLOT_CANCEL, ItemUtils.itemCancel);
		inv.setItem(SLOT_CREATE, ItemUtils.item(XMaterial.CHARCOAL, Lang.done.toString()));
		handleDoneButton(inv);
		
		return p.openInventory(inv).getTopInventory();
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case SLOT_NPC:
			new SelectGUI(() -> reopen(p, inv, true), npc -> {
				npcID = npc.getId();
				ItemUtils.lore(current, getNPCLore());
				handleDoneButton(inv);
				reopen(p, inv, true);
			}).create(p);
			break;
		case SLOT_HOLOGRAM:
			Lang.POOL_HOLOGRAM_TEXT.send(p);
			new TextEditor<String>(p, () -> reopen(p, inv, false), msg -> {
				hologram = msg;
				ItemUtils.lore(current, getHologramLore());
				reopen(p, inv, false);
			}).passNullIntoEndConsumer().enter();
			break;
		case SLOT_MAX_QUESTS:
			Lang.POOL_MAXQUESTS.send(p);
			new TextEditor<>(p, () -> reopen(p, inv, false), msg -> {
				maxQuests = msg;
				ItemUtils.lore(current, getAmountLore());
				reopen(p, inv, false);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;
		case SLOT_TIME:
			Lang.POOL_TIME.send(p);
			new TextEditor<>(p, () -> reopen(p, inv, false), msg -> {
				timeDiff = TimeUnit.DAYS.toMillis(msg);
				ItemUtils.lore(current, getTimeLore());
				reopen(p, inv, false);
			}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
			break;
		case SLOT_REDO:
			redoAllowed = ItemUtils.toggle(current);
			break;
		case SLOT_DUPLICATE:
			avoidDuplicates = ItemUtils.toggle(current);
			break;
		case SLOT_REQUIREMENTS:
			new QuestObjectGUI<>(Lang.INVENTORY_REQUIREMENTS.toString(), QuestObjectLocation.POOL, QuestsAPI.requirements.values(), requirements -> {
				PoolEditGUI.this.requirements = requirements;
				ItemUtils.lore(current, getRequirementsLore());
				reopen(p, inv, true);
			}, requirements).create(p);
			break;
		
		case SLOT_CANCEL:
			end.run();
			break;
		case SLOT_CREATE:
			if (canFinish) {
				BeautyQuests.getInstance().getPoolsManager().createPool(editing, npcID, hologram, maxQuests, redoAllowed, timeDiff, avoidDuplicates, requirements);
				end.run();
			}else p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			break;
		}
		return true;
	}
	
}
