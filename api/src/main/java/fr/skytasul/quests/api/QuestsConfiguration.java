package fr.skytasul.quests.api;

import java.util.Collection;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.api.options.description.QuestDescription;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.progress.ProgressBarConfig;
import fr.skytasul.quests.api.utils.progress.itemdescription.ItemsDescriptionConfiguration;

public interface QuestsConfiguration {

	static @NotNull QuestsConfiguration getConfig() {
		return QuestsPlugin.getPlugin().getConfiguration();
	}

	@NotNull
	Quests getQuestsConfig();

	@NotNull
	Dialogs getDialogsConfig();

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

		boolean skipNpcGuiIfOnlyOneQuest();

		ItemStack getDefaultQuestItem();

		XMaterial getPageMaterial();

		double startParticleDistance();

		int requirementUpdateTime();

		boolean requirementReasonOnMultipleQuests();

		boolean stageEndRewardsMessage();

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

	interface QuestsMenu {

		boolean isNotStartedTabOpenedWhenEmpty();

		boolean allowPlayerCancelQuest();

		Set<PlayerListCategory> getEnabledTabs();

	}

	interface StageDescription extends ItemsDescriptionConfiguration, ProgressBarConfig {

		String getStageDescriptionFormat();

	}

}