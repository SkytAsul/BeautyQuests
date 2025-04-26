package fr.skytasul.quests.api;

import fr.skytasul.quests.api.blocks.BQBlocksManager;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.BqNpcManager;
import fr.skytasul.quests.api.npcs.dialogs.MessageSender;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.utils.messaging.MessageProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class contains most of the useful accessors to fetch data from BeautyQuests and methods to
 * implement custom behaviors.
 */
public interface QuestsAPI {

	/**
	 * Utility method to get the instance of the plugin.
	 *
	 * @return the plugin instance
	 */
	@NotNull
	QuestsPlugin getPlugin();

	/**
	 * Gets the quests manager, which provides methods to get and manage quests and also get list of
	 * quests based on player progress.
	 *
	 * @return the quest manager
	 */
	@NotNull
	QuestsManager getQuestsManager();

	/**
	 * Gets the quest pools manager, which provides methods to get and manage pools.
	 *
	 * @return the pools manager
	 */
	@NotNull
	QuestPoolsManager getPoolsManager();

	/**
	 * Gets the quester manager, which provides methods to manage questers and savable data.
	 *
	 * @return the quester manager
	 */
	@NotNull
	QuesterManager getQuesterManager();

	/**
	 * Registers a new mob factory.
	 *
	 * @param factory a MobFactory instance that has not yet been registered
	 */
	void registerMobFactory(@NotNull MobFactory<?> factory);

	/**
	 * Registers a new quest option creator.
	 *
	 * @param creator instance of the option creator
	 */
	void registerQuestOption(@NotNull QuestOptionCreator<?, ?> creator);

	/**
	 * Gets all registered item comparisons.
	 *
	 * @return immutable list of registered comparisons
	 */
	@NotNull
	List<@NotNull ItemComparison> getItemComparisons();

	/**
	 * Registers a new item comparison instance.
	 *
	 * @param comparison instance of an item comparison that has not already been registered
	 */
	void registerItemComparison(@NotNull ItemComparison comparison);

	/**
	 * Unregisters a previsouly registered item comparison instance.
	 *
	 * @param comparison instance of an item comparison that has been registered
	 */
	void unregisterItemComparison(@NotNull ItemComparison comparison);

	/**
	 * Gets a list of registered mob stackers.
	 *
	 * @return immutable list of registered stackers
	 */
	@NotNull
	List<@NotNull MobStacker> getMobStackers();

	/**
	 * Registers a new mob stacker instance.
	 *
	 * @param stacker instance of a mob stacker that has not already been registered
	 */
	void registerMobStacker(@NotNull MobStacker stacker);

	/**
	 * Gets the stage type registry, which provides methods to register stage types, stage options and
	 * get existing stage type.
	 *
	 * @return the stage type registry
	 */
	@NotNull
	StageTypeRegistry getStages();

	/**
	 * Gets the requirements registry, which provides methods to register and get requirements.
	 *
	 * @return the requirements registry
	 */
	@NotNull
	QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getRequirements();

	/**
	 * Gets the rewards registry, which provides methods to register and get rewards.
	 *
	 * @return the rewards registry
	 */
	@NotNull
	QuestObjectsRegistry<AbstractReward, RewardCreator> getRewards();

	/**
	 * Adds a new npc factory to the npc manager.
	 *
	 * @param key unique key of the npc factory
	 * @param factory factory
	 * @see BqNpcManager#addInternalFactory(String, BqInternalNpcFactory)
	 */
	void addNpcFactory(@NotNull String key, @NotNull BqInternalNpcFactory factory);

	/**
	 * Checks if there is an hologram manager registered.
	 *
	 * @return <code>true</code> if {@link #getHologramsManager()} will not return <code>null</code>,
	 *         <code>false</code> otherwise
	 */
	default boolean hasHologramsManager() {
		return getHologramsManager() != null;
	}

	/**
	 * Gets the currently registered holograms manager.
	 *
	 * @return the current holograms manager
	 */
	@Nullable
	AbstractHolograms<?> getHologramsManager();

	/**
	 * Sets the plugin's holograms manager to the one passed as argument.<br>
	 * If there is already an holograms manager registered, this one will replace it.
	 *
	 * @param newHologramsManager holograms manager to register
	 */
	void setHologramsManager(@NotNull AbstractHolograms<?> newHologramsManager);

	/**
	 * Gets the blocks manager, which provides methods to register custom block types and deserialize
	 * blocks.
	 *
	 * @return the blocks manager
	 */
	@NotNull
	BQBlocksManager getBlocksManager();

	/**
	 * Registers a quests handler and calls {@link QuestsHandler#load()} if it was not already
	 * registered.
	 *
	 * @param handler handler to register
	 */
	void registerQuestsHandler(@NotNull QuestsHandler handler);

	/**
	 * Unregisters a quests handler and calls {@link QuestsHandler#unload()} if it was registered.
	 *
	 * @param handler handler to unregister
	 */
	void unregisterQuestsHandler(@NotNull QuestsHandler handler);

	/**
	 * Gets the registered quests handlers.
	 *
	 * @return an unmodifiable collection of registered quest handlers
	 */
	@NotNull
	Collection<@NotNull QuestsHandler> getQuestsHandlers();

	/**
	 * Utility method to call a method on every registered quests handlers.
	 *
	 * @param consumer action to do on quests handlers
	 */
	void propagateQuestsHandlers(@NotNull Consumer<@NotNull QuestsHandler> consumer);

	/**
	 * Gets all registered message processors.
	 *
	 * @return an unmodifiable collection of registered message processors
	 */
	@NotNull
	Collection<MessageProcessor> getMessageProcessors();

	/**
	 * Registers a custom message processor into the pipeline.<br>
	 * If a processor with the same key and priority already exists, it will get replaced by this
	 * one.<br>
	 * Processors with the lowest priority are run first.
	 *
	 * @param key unique key of this message processor
	 * @param priority priority of this message processor
	 * @param processor processor
	 */
	void registerMessageProcessor(@NotNull String key, int priority, @NotNull MessageProcessor processor);

	/**
	 * Gets the currently registered message sender. It is responsible for displaying dialog messages to
	 * players.
	 *
	 * @return the message sender
	 */
	@NotNull
	MessageSender getMessageSender();

	/**
	 * Sets the new message sender.
	 *
	 * @param sender the new message sender
	 * @see #getMessageSender()
	 */
	void setMessageSender(@NotNull MessageSender sender);

	/**
	 * Utility method to get an instance of the API object.
	 *
	 * @return the api object
	 */
	public static @NotNull QuestsAPI getAPI() {
		return QuestsAPIProvider.getAPI();
	}

}
