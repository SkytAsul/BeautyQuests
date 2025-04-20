package fr.skytasul.quests.api;

import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.api.options.description.QuestDescription;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.progress.ProgressBarConfig;
import fr.skytasul.quests.api.utils.progress.itemdescription.ItemsDescriptionConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Set;

public interface QuestsConfiguration {

	static @NotNull QuestsConfiguration getConfig() {
		return QuestsPlugin.getPlugin().getConfiguration();
	}

	@NotNull
	Quests getQuestsConfig();

	@NotNull
	Gui getGuiConfig();

	@NotNull
	Dialogs getDialogsConfig();

	@NotNull
	QuestsSelection getQuestsSelectionConfig();

	@NotNull
	QuestsMenu getQuestsMenuConfig();

	@NotNull
	StageDescription getStageDescriptionConfig();

	@NotNull
	QuestDescription getQuestDescriptionConfig();

	interface Quests {

		int getDefaultTimer();

		int maxLaunchedQuests();

		boolean scoreboards();

		boolean playerQuestUpdateMessage();

		boolean playerStageStartMessage();

		boolean questConfirmGUI();

		boolean sounds();

		String finishSound();

		String nextStageSound();

		boolean fireworks();

		Collection<NpcClickType> getNpcClicks();

		boolean dontCancelNpcClick();

		ItemStack getDefaultQuestItem();

		double startParticleDistance();

		int requirementUpdateTime();

		boolean requirementReasonOnMultipleQuests();

		boolean stageEndRewardsMessage();

	}

	interface Gui {

		ItemStack getPreviousPageItem();

		ItemStack getNextPageItem();

		boolean showVerticalSeparator();

	}

	interface Dialogs {

		boolean sendInActionBar();

		int getDefaultTime();

		boolean isSkippableByDefault();

		boolean isClickDisabled();

		boolean isHistoryEnabled();

		int getMaxMessagesPerHistoryPage();

		int getMaxDistance();

		int getMaxDistanceSquared();

		String getDefaultPlayerSound();

		String getDefaultNPCSound();

	}

	interface QuestsSelection {

		boolean skipGuiIfOnlyOneQuest();

		boolean hideNoRequirements();

	}

	interface QuestsMenu {

		boolean isNotStartedTabOpenedWhenEmpty();

		boolean allowPlayerCancelQuest();

		Set<PlayerListCategory> getEnabledTabs();

	}

	interface StageDescription extends ItemsDescriptionConfiguration, ProgressBarConfig {

		String getStageDescriptionFormat();

	}

}
