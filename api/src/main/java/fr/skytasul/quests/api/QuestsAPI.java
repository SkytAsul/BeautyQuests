package fr.skytasul.quests.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.blocks.BQBlocksManager;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.utils.messaging.MessageProcessor;

/**
 * This class contains most of the useful accessors to fetch data from BeautyQuests and methods to
 * implement custom behaviors.
 */
public interface QuestsAPI {

	@NotNull
	QuestsPlugin getPlugin();

	@NotNull
	QuestsManager getQuestsManager();

	@NotNull
	QuestPoolsManager getPoolsManager();

	/**
	 * Register new mob factory
	 * @param factory MobFactory instance
	 */
	void registerMobFactory(@NotNull MobFactory<?> factory);

	void registerQuestOption(@NotNull QuestOptionCreator<?, ?> creator);

	@NotNull
	List<@NotNull ItemComparison> getItemComparisons();

	void registerItemComparison(@NotNull ItemComparison comparison);

	void unregisterItemComparison(@NotNull ItemComparison comparison);

	@NotNull
	List<@NotNull MobStacker> getMobStackers();

	void registerMobStacker(@NotNull MobStacker stacker);

	@NotNull
	StageTypeRegistry getStages();

	@NotNull
	QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getRequirements();

	@NotNull
	QuestObjectsRegistry<AbstractReward, RewardCreator> getRewards();

	void addNpcFactory(@NotNull String key, @NotNull BqInternalNpcFactory factory);

	boolean hasHologramsManager();

	@Nullable
	AbstractHolograms<?> getHologramsManager();

	void setHologramsManager(@NotNull AbstractHolograms<?> newHologramsManager);

	boolean hasBossBarManager();

	@Nullable
	BossBarManager getBossBarManager();

	void setBossBarManager(@NotNull BossBarManager newBossBarManager);

	@NotNull
	BQBlocksManager getBlocksManager();

	void registerQuestsHandler(@NotNull QuestsHandler handler);

	void unregisterQuestsHandler(@NotNull QuestsHandler handler);

	@NotNull
	Collection<@NotNull QuestsHandler> getQuestsHandlers();

	void propagateQuestsHandlers(@NotNull Consumer<@NotNull QuestsHandler> consumer);

	@NotNull
	Set<MessageProcessor> getMessageProcessors();

	void registerMessageProcessor(@NotNull MessageProcessor processor);

	public static @NotNull QuestsAPI getAPI() {
		return QuestsAPIProvider.getAPI();
	}

}
