package fr.skytasul.quests.gui.pools;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.pools.QuestPoolData;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PoolEditGUI extends AbstractGui {

	private static final int SLOT_NAME = 0;
	private static final int SLOT_HOLOGRAM = 1;
	private static final int SLOT_NPC = 2;
	private static final int SLOT_MAX_QUESTS = 3;
	private static final int SLOT_QUESTS_PER_LAUNCH = 4;
	private static final int SLOT_REQUIREMENTS = 9;
	private static final int SLOT_TIME = 10;
	private static final int SLOT_REDO = 11;
	private static final int SLOT_DUPLICATE = 12;
	private static final int SLOT_CATEGORY = 13;
	private static final int SLOT_CANCEL = 16;
	private static final int SLOT_CREATE = 17;

	private final Runnable cancel;
	private final Consumer<QuestPoolData> end;

	private String name;
	private String hologram;
	private int maxQuests = 1;
	private int questsPerLaunch = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	private String npcID = null;
	private boolean avoidDuplicates = true;
	private RequirementList requirements = new RequirementList();
	private boolean showAsCategory = true;

	public PoolEditGUI(Runnable cancel, Consumer<QuestPoolData> end) {
		this.cancel = cancel;
		this.end = end;
	}

	public PoolEditGUI fillFrom(QuestPoolData editing) {
		name = editing.name();
		hologram = editing.hologram();
		maxQuests = editing.maxQuests();
		questsPerLaunch = editing.questsPerLaunch();
		redoAllowed = editing.redoAllowed();
		timeDiff = editing.timeDiff();
		npcID = editing.npcId();
		avoidDuplicates = editing.avoidDuplicates();
		requirements = editing.requirements();
		showAsCategory = editing.showAsCategory();

		return this;
	}

	private String[] getNameLore() {
		return new String[] {"", QuestOption.formatNullableValue(name, null)};
	}

	private String[] getNPCLore() {
		return new String[] {"", QuestOption.formatNullableValue("NPC " + npcID)};
	}

	private String[] getHologramLore() {
		return new String[] { "", hologram == null ? QuestOption.formatNullableValue(Lang.PoolHologramText.toString()) + " " + Lang.defaultValue.toString() : QuestOption.formatNullableValue(hologram) };
	}

	private String[] getMaxQuestsLore() {
		return new String[] { "", QuestOption.formatNullableValue(maxQuests) };
	}

	private String[] getQuestsPerLaunchLore() {
		return new String[] { "", QuestOption.formatNullableValue(Integer.toString(questsPerLaunch), questsPerLaunch == 1) };
	}

	private String[] getTimeLore() {
		return new String[] {"", QuestOption.formatNullableValue(Utils.millisToHumanString(timeDiff))};
	}

	private String[] getRequirementsLore() {
		return new String[] {"", QuestOption.formatDescription(requirements.getSizeString())};
	}

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 18, Lang.INVENTORY_POOL_CREATE.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inv) {
		inv.setItem(SLOT_NAME, ItemUtils.item(XMaterial.NAME_TAG, "§e" + Lang.poolEditName.toString(), getNameLore()));
		if (QuestsPlugin.getPlugin().getNpcManager().isEnabled())
			inv.setItem(SLOT_NPC,
					ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString(), getNPCLore()));
		inv.setItem(SLOT_HOLOGRAM, ItemUtils.item(XMaterial.OAK_SIGN, Lang.poolEditHologramText.toString(), getHologramLore()));
		inv.setItem(SLOT_MAX_QUESTS, ItemUtils.item(XMaterial.REDSTONE, Lang.poolMaxQuests.toString(), getMaxQuestsLore()));
		inv.setItem(SLOT_QUESTS_PER_LAUNCH, ItemUtils.item(XMaterial.GUNPOWDER, Lang.poolQuestsPerLaunch.toString(), getQuestsPerLaunchLore()));
		inv.setItem(SLOT_TIME, ItemUtils.item(XMaterial.CLOCK, Lang.poolTime.toString(), getTimeLore()));
		inv.setItem(SLOT_REDO, ItemUtils.itemSwitch(Lang.poolRedo.toString(), redoAllowed));
		inv.setItem(SLOT_DUPLICATE, ItemUtils.itemSwitch(Lang.poolAvoidDuplicates.toString(), avoidDuplicates, Lang.poolAvoidDuplicatesLore.toString()));
		inv.setItem(SLOT_CATEGORY, ItemUtils.itemSwitch(Lang.poolShowAsCategory.toString(), showAsCategory));
		inv.setItem(SLOT_REQUIREMENTS, ItemUtils.item(XMaterial.NETHER_STAR, Lang.poolRequirements.toString(), getRequirementsLore()));

		inv.setItem(SLOT_CANCEL, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel());
		inv.setItem(SLOT_CREATE, QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone());
	}

	@Override
	public void onClick(GuiClickEvent event) {
		switch (event.getSlot()) {
			case SLOT_NAME:
				Lang.POOL_NAME.send(event.getPlayer());
				new TextEditor<String>(event.getPlayer(), event::reopen, msg -> {
					name = msg;
					ItemUtils.lore(event.getClicked(), getNameLore());
					reopen(event.getPlayer());
				}).passNullIntoEndConsumer().start();
				break;
		case SLOT_NPC:
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createNpcSelection(event::reopen, npc -> {
				npcID = npc.getId();
				ItemUtils.lore(event.getClicked(), getNPCLore());
				reopen(event.getPlayer());
			}, false).open(event.getPlayer());
			break;
		case SLOT_HOLOGRAM:
			Lang.POOL_HOLOGRAM_TEXT.send(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopen, msg -> {
				hologram = msg;
				ItemUtils.lore(event.getClicked(), getHologramLore());
				reopen(event.getPlayer());
			}).passNullIntoEndConsumer().start();
			break;
		case SLOT_MAX_QUESTS:
			Lang.POOL_MAXQUESTS.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
				maxQuests = msg;
				ItemUtils.lore(event.getClicked(), getMaxQuestsLore());
				reopen(event.getPlayer());
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			break;
		case SLOT_QUESTS_PER_LAUNCH:
			Lang.POOL_QUESTS_PER_LAUNCH.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
				questsPerLaunch = msg;
				ItemUtils.lore(event.getClicked(), getQuestsPerLaunchLore());
				reopen(event.getPlayer());
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
			break;
		case SLOT_TIME:
			Lang.POOL_TIME.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
				timeDiff = msg * 1000;
				ItemUtils.lore(event.getClicked(), getTimeLore());
				reopen(event.getPlayer());
			}, new DurationParser(MinecraftTimeUnit.SECOND, MinecraftTimeUnit.DAY)).start();
			break;
		case SLOT_REDO:
			redoAllowed = ItemUtils.toggleSwitch(event.getClicked());
			break;
		case SLOT_DUPLICATE:
			avoidDuplicates = ItemUtils.toggleSwitch(event.getClicked());
			break;
		case SLOT_CATEGORY:
			showAsCategory = ItemUtils.toggleSwitch(event.getClicked());
			break;
		case SLOT_REQUIREMENTS:
			QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.POOL, newRequirements -> {
				requirements = new RequirementList(newRequirements);
				ItemUtils.lore(event.getClicked(), getRequirementsLore());
				reopen(event.getPlayer());
			}, requirements).open(event.getPlayer());
			break;

		case SLOT_CANCEL:
			cancel.run();
			break;
		case SLOT_CREATE:
			var poolData = new QuestPoolData(name, npcID, hologram, maxQuests, questsPerLaunch, redoAllowed, timeDiff,
					avoidDuplicates, requirements, showAsCategory);
			end.accept(poolData);
			break;
		}
	}

}
