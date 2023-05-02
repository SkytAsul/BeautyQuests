package fr.skytasul.quests.gui.pools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.DurationParser;
import fr.skytasul.quests.api.editors.checkers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.npc.NpcSelectGUI;

public class PoolEditGUI extends Gui {
	
	private static final int SLOT_NPC = 1;
	private static final int SLOT_HOLOGRAM = 2;
	private static final int SLOT_MAX_QUESTS = 3;
	private static final int SLOT_QUESTS_PER_LAUNCH = 4;
	private static final int SLOT_TIME = 5;
	private static final int SLOT_REDO = 6;
	private static final int SLOT_DUPLICATE = 7;
	private static final int SLOT_REQUIREMENTS = 8;
	private static final int SLOT_CANCEL = 12;
	private static final int SLOT_CREATE = 14;
	
	private final Runnable end;
	
	private String hologram;
	private int maxQuests = 1;
	private int questsPerLaunch = 1;
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
			questsPerLaunch = editing.getQuestsPerLaunch();
			redoAllowed = editing.isRedoAllowed();
			timeDiff = editing.getTimeDiff();
			npcID = editing.getNpcId();
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
	
	private String[] getMaxQuestsLore() {
		return new String[] { "", Lang.optionValue.format(maxQuests) };
	}
	
	private String[] getQuestsPerLaunchLore() {
		return new String[] { "", QuestOption.formatNullableValue(Integer.toString(questsPerLaunch), questsPerLaunch == 1) };
	}
	
	private String[] getTimeLore() {
		return new String[] { "", Lang.optionValue.format(Utils.millisToHumanString(timeDiff)) };
	}
	
	private String[] getRequirementsLore() {
		return new String[] { "", QuestOption.formatDescription(Lang.requirements.format(requirements.size())) };
	}
	
	private void handleDoneButton(Inventory inv) {
		boolean newState = /*name != null &&*/ npcID != -1;
		if (newState == canFinish) return;
		inv.getItem(SLOT_CREATE).setType((newState ? XMaterial.DIAMOND : XMaterial.CHARCOAL).parseMaterial());
		canFinish = newState;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 18, Lang.INVENTORY_POOL_CREATE.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inv) {
		inv.setItem(SLOT_NPC, ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString(), getNPCLore()));
		inv.setItem(SLOT_HOLOGRAM, ItemUtils.item(XMaterial.OAK_SIGN, Lang.poolEditHologramText.toString(), getHologramLore()));
		inv.setItem(SLOT_MAX_QUESTS, ItemUtils.item(XMaterial.REDSTONE, Lang.poolMaxQuests.toString(), getMaxQuestsLore()));
		inv.setItem(SLOT_QUESTS_PER_LAUNCH, ItemUtils.item(XMaterial.GUNPOWDER, Lang.poolQuestsPerLaunch.toString(), getQuestsPerLaunchLore()));
		inv.setItem(SLOT_TIME, ItemUtils.item(XMaterial.CLOCK, Lang.poolTime.toString(), getTimeLore()));
		inv.setItem(SLOT_REDO, ItemUtils.itemSwitch(Lang.poolRedo.toString(), redoAllowed));
		inv.setItem(SLOT_DUPLICATE, ItemUtils.itemSwitch(Lang.poolAvoidDuplicates.toString(), avoidDuplicates, Lang.poolAvoidDuplicatesLore.toString()));
		inv.setItem(SLOT_REQUIREMENTS, ItemUtils.item(XMaterial.NETHER_STAR, Lang.poolRequirements.toString(), getRequirementsLore()));
		
		inv.setItem(SLOT_CANCEL, ItemUtils.itemCancel);
		inv.setItem(SLOT_CREATE, ItemUtils.item(XMaterial.CHARCOAL, Lang.done.toString()));
		handleDoneButton(inv);
	}
	
	@Override
	public void onClick(GuiClickEvent event) {
		switch (slot) {
		case SLOT_NPC:
			NpcSelectGUI.select(() -> reopen(p), npc -> {
				npcID = npc.getId();
				ItemUtils.lore(current, getNPCLore());
				handleDoneButton(getInventory());
				reopen(p);
			}).open(p);
			break;
		case SLOT_HOLOGRAM:
			Lang.POOL_HOLOGRAM_TEXT.send(p);
			new TextEditor<String>(p, () -> reopen(p), msg -> {
				hologram = msg;
				ItemUtils.lore(current, getHologramLore());
				reopen(p);
			}).passNullIntoEndConsumer().start();
			break;
		case SLOT_MAX_QUESTS:
			Lang.POOL_MAXQUESTS.send(p);
			new TextEditor<>(p, () -> reopen(p), msg -> {
				maxQuests = msg;
				ItemUtils.lore(current, getMaxQuestsLore());
				reopen(p);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			break;
		case SLOT_QUESTS_PER_LAUNCH:
			Lang.POOL_QUESTS_PER_LAUNCH.send(p);
			new TextEditor<>(p, () -> reopen(p), msg -> {
				questsPerLaunch = msg;
				ItemUtils.lore(current, getQuestsPerLaunchLore());
				reopen(p);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			break;
		case SLOT_TIME:
			Lang.POOL_TIME.send(p);
			new TextEditor<>(p, () -> reopen(p), msg -> {
				timeDiff = msg * 1000;
				ItemUtils.lore(current, getTimeLore());
				reopen(p);
			}, new DurationParser(MinecraftTimeUnit.SECOND, MinecraftTimeUnit.DAY)).start();
			break;
		case SLOT_REDO:
			redoAllowed = ItemUtils.toggleSwitch(current);
			break;
		case SLOT_DUPLICATE:
			avoidDuplicates = ItemUtils.toggleSwitch(current);
			break;
		case SLOT_REQUIREMENTS:
			QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.POOL, requirements -> {
				PoolEditGUI.this.requirements = requirements;
				ItemUtils.lore(current, getRequirementsLore());
				reopen(p);
			}, requirements).open(p);
			break;
		
		case SLOT_CANCEL:
			end.run();
			break;
		case SLOT_CREATE:
			if (canFinish) {
				BeautyQuests.getInstance().getPoolsManager().createPool(editing, npcID, hologram, maxQuests, questsPerLaunch, redoAllowed, timeDiff, avoidDuplicates, requirements);
				end.run();
			}else p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			break;
		}
		return true;
	}
	
}
