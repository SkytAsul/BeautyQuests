package fr.skytasul.quests.gui.pools;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI.LayoutedRowsGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.pools.QuestPoolData;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.utils.Utils;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PoolEditGUI extends LayoutedRowsGUI {

	private final Consumer<QuestPoolData> end;

	private String name;
	private String hologram;
	private int maxQuests = 1;
	private int questsPerLaunch = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	private String npcID = null;
	private boolean avoidDuplicates = true;
	private boolean showAsCategory = true;
	private RequirementList requirements = new RequirementList();
	private RewardList startRewards = new RewardList();
	private RewardList endRewards = new RewardList();

	public PoolEditGUI(Runnable cancel, Consumer<QuestPoolData> end) {
		super(Lang.INVENTORY_POOL_CREATE.toString(), new HashMap<>(), new DelayCloseBehavior(cancel), 2);
		this.end = end;

		buttons.put(0, LayoutedButton.createLoreValue(XMaterial.NAME_TAG, "§e" + Lang.poolEditName.toString(), () -> name,
				this::onNameClick));

		if (QuestsPlugin.getPlugin().getNpcManager().isEnabled()) {
			buttons.put(1, LayoutedButton.createLoreValue(XMaterial.OAK_SIGN, Lang.poolEditHologramText.toString(),
					() -> hologram, this::onHologramClick));
			buttons.put(2, LayoutedButton.createLoreValue(XMaterial.VILLAGER_SPAWN_EGG, Lang.stageNPCSelect.toString(),
					() -> npcID, this::onNpcClick));
		}

		buttons.put(3, LayoutedButton.createLoreValue(XMaterial.REDSTONE, Lang.poolMaxQuests.toString(), () -> maxQuests,
				this::onMaxQuestsClick));
		buttons.put(4,
				LayoutedButton.createLoreValue(XMaterial.GUNPOWDER, Lang.poolQuestsPerLaunch.toString(),
						() -> questsPerLaunch,
						this::onQuestsPerLaunchClick));
		buttons.put(5,
				LayoutedButton.createLoreValue(XMaterial.CLOCK, Lang.poolTime.toString(),
						() -> Utils.millisToHumanString(timeDiff),
						this::onTimeClick));
		buttons.put(6, LayoutedButton.createSwitch(() -> redoAllowed, Lang.poolRedo.toString(), null, event -> {
			redoAllowed = !redoAllowed;
			event.refreshItem();
		}));
		buttons.put(7,
				LayoutedButton.createSwitch(() -> avoidDuplicates, Lang.poolAvoidDuplicates.toString(), null, event -> {
					avoidDuplicates = !avoidDuplicates;
					event.refreshItem();
				}));
		buttons.put(8,
				LayoutedButton.createSwitch(() -> showAsCategory, Lang.poolShowAsCategory.toString(), null, event -> {
					showAsCategory = !showAsCategory;
					event.refreshItem();
				}));
		buttons.put(9,
				LayoutedButton.createLoreValue(XMaterial.NETHER_BRICK, Lang.poolRequirements.toString(),
						() -> requirements.getSizeString(),
						this::onRequirementsClick));
		buttons.put(10,
				LayoutedButton.createLoreValue(XMaterial.CHEST, Lang.poolStartRewards.toString(),
						() -> startRewards.getSizeString(),
						this::onStartRewardsClick));
		buttons.put(11,
				LayoutedButton.createLoreValue(XMaterial.ENDER_CHEST, Lang.poolEndRewards.toString(),
						() -> endRewards.getSizeString(),
						this::onEndRewardsClick));

		buttons.put(16, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel(),
				event -> cancel.run()));
		buttons.put(17, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone(),
				this::onDoneClick));
	}

	public PoolEditGUI fillFrom(QuestPoolData editing) {
		if (getInventory() != null)
			throw new IllegalStateException("GUI has already been built");
		name = editing.name();
		hologram = editing.hologram();
		maxQuests = editing.maxQuests();
		questsPerLaunch = editing.questsPerLaunch();
		redoAllowed = editing.redoAllowed();
		timeDiff = editing.timeDiff();
		npcID = editing.npcId();
		avoidDuplicates = editing.avoidDuplicates();
		requirements = editing.requirements();
		startRewards = editing.startRewards();
		endRewards = editing.endRewards();
		showAsCategory = editing.showAsCategory();
		return this;
	}

	private void onNameClick(LayoutedClickEvent event) {
		Lang.POOL_NAME.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, msg -> {
			name = msg;
			event.refreshItemReopen();
		}).passNullIntoEndConsumer().start();
	}

	private void onNpcClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createNpcSelection(event::reopen, npc -> {
			npcID = npc.getId();
			event.refreshItemReopen();
		}, false).open(event.getPlayer());
	}

	private void onHologramClick(LayoutedClickEvent event) {
		Lang.POOL_HOLOGRAM_TEXT.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, msg -> {
			hologram = msg;
			event.refreshItemReopen();
		}).passNullIntoEndConsumer().start();
	}

	private void onMaxQuestsClick(LayoutedClickEvent event) {
		Lang.POOL_MAXQUESTS.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
			maxQuests = msg;
			event.refreshItemReopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
	}

	private void onQuestsPerLaunchClick(LayoutedClickEvent event) {
		Lang.POOL_QUESTS_PER_LAUNCH.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
			questsPerLaunch = msg;
			event.refreshItemReopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
	}

	private void onTimeClick(LayoutedClickEvent event) {
		Lang.POOL_TIME.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, msg -> {
			timeDiff = msg * 1000;
			event.refreshItemReopen();
		}, new DurationParser(MinecraftTimeUnit.SECOND, MinecraftTimeUnit.DAY)).start();
	}

	private void onRequirementsClick(LayoutedClickEvent event) {
		QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.POOL, newRequirements -> {
			requirements = new RequirementList(newRequirements);
			event.refreshItemReopen();
		}, requirements).open(event.getPlayer());
	}

	private void onStartRewardsClick(LayoutedClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.POOL, newRewards -> {
			startRewards = new RewardList(newRewards);
			event.refreshItemReopen();
		}, startRewards).open(event.getPlayer());
	}

	private void onEndRewardsClick(LayoutedClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.POOL, newRewards -> {
			endRewards = new RewardList(newRewards);
			event.refreshItemReopen();
		}, endRewards).open(event.getPlayer());
	}

	private void onDoneClick(LayoutedClickEvent event) {
		var poolData = new QuestPoolData(name, npcID, hologram, maxQuests, questsPerLaunch, redoAllowed, timeDiff,
				avoidDuplicates, showAsCategory, requirements, startRewards, endRewards);
		end.accept(poolData);
	}

}

