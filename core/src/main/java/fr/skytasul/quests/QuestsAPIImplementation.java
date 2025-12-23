package fr.skytasul.quests;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.holograms.BqHologramManager;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.dialogs.MessageSender;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategyCreator;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.api.utils.messaging.MessageProcessor;
import fr.skytasul.quests.blocks.BQBlocksManagerImplementation;
import fr.skytasul.quests.npcs.dialogs.ActionBarMessageSender;
import fr.skytasul.quests.npcs.dialogs.ChatMessageSender;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QuestsAPIImplementation implements QuestsAPI {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.API");

	private final StageTypeRegistry stages = new StageTypeRegistry();
	private final List<ItemComparison> itemComparisons = new LinkedList<>();
	private final List<MobStacker> mobStackers = new ArrayList<>();

	public Map<Class<? extends QuestOption<?>>, QuestOptionCreator<?, ?>> questOptions = new HashMap<>();

	private QuestObjectsRegistry<AbstractRequirement, RequirementCreator> requirements;
	private QuestObjectsRegistry<AbstractReward, RewardCreator> rewards;
	private SerializableRegistry<QuestQuesterStrategy, QuestQuesterStrategyCreator> questerStrategies;

	private BqHologramManager hologramsManager = null;
	private BQBlocksManagerImplementation blocksManager = new BQBlocksManagerImplementation();
	private MessageSender messageSender;

	private final Set<QuestsHandler> handlers = new HashSet<>();

	private final Set<MessageProcessorInfo> processors = new TreeSet<>();

	private final BeautyQuests plugin;

	protected QuestsAPIImplementation(BeautyQuests plugin) {
		this.plugin = plugin;
	}

	void setup() {
		requirements = new QuestObjectsRegistry<>("requirements", Lang.INVENTORY_REQUIREMENTS.toString());
		rewards = new QuestObjectsRegistry<>("rewards", Lang.INVENTORY_REWARDS.toString());

		questerStrategies = new SerializableRegistry<>("quester-strategies");

		setMessageSender(QuestsConfiguration.getConfig().getDialogsConfig().sendInActionBar()
				? new ActionBarMessageSender()
				: new ChatMessageSender());
	}



	@Override
	public @NotNull StageTypeRegistry getStages() {
		return stages;
	}

	/**
	 * Register new mob factory
	 *
	 * @param factory MobFactory instance
	 */
	@Override
	public void registerMobFactory(@NotNull MobFactory<?> factory) {
		MobFactory.factories.add(factory);
		QuestUtils.autoRegister(factory);
		LOGGER.debug("Mob factory registered (id: " + factory.getID() + ")");
	}

	@Override
	public void registerQuestOption(@NotNull QuestOptionCreator<?, ?> creator) {
		Validate.notNull(creator);
		Validate.isTrue(!questOptions.containsKey(creator.optionClass),
				"This quest option was already registered");
		questOptions.put(creator.optionClass, creator);
		LOGGER.debug("Quest option registered (id: " + creator.id + ")");
	}

	@Override
	public <D, T extends QuestOption<D>> Optional<QuestOptionCreator<D, T>> getQuestOption(Class<T> optionClass) {
		return (Optional) Optional.ofNullable(questOptions.get(optionClass));
	}

	@Override
	public @NotNull Collection<QuestOptionCreator<?, ?>> getQuestOptions() {
		return questOptions.values();
	}

	@Override
	public @NotNull List<@NotNull ItemComparison> getItemComparisons() {
		return itemComparisons;
	}

	@Override
	public void registerItemComparison(@NotNull ItemComparison comparison) {
		Validate.isTrue(itemComparisons.stream().noneMatch(x -> x.getID().equals(comparison.getID())),
				"This item comparison was already registered");
		itemComparisons.add(comparison);
		LOGGER.debug("Item comparison registered (id: " + comparison.getID() + ")");
	}

	@Override
	public void unregisterItemComparison(@NotNull ItemComparison comparison) {
		Validate.isTrue(itemComparisons.remove(comparison), "This item comparison was not registered");
		LOGGER.debug("Item comparison unregistered (id: " + comparison.getID() + ")");
	}

	@Override
	public @NotNull List<@NotNull MobStacker> getMobStackers() {
		return mobStackers;
	}

	@Override
	public void registerMobStacker(@NotNull MobStacker stacker) {
		mobStackers.add(stacker);
		LOGGER.debug("Added " + stacker.toString() + " mob stacker");
	}

	@Override
	public @NotNull QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getRequirements() {
		return requirements;
	}

	@Override
	public @NotNull QuestObjectsRegistry<AbstractReward, RewardCreator> getRewards() {
		return rewards;
	}

	@Override
	public @NotNull SerializableRegistry<QuestQuesterStrategy, QuestQuesterStrategyCreator> getQuestQuesterStrategyRegistry() {
		return questerStrategies;
	}

	@Override
	public void addNpcFactory(@NotNull String key, @NotNull BqInternalNpcFactory factory) {
		plugin.getNpcManager().addInternalFactory(key, factory);
	}

	@Override
	public @Nullable BqHologramManager getHologramsManager() {
		return hologramsManager;
	}

	@Override
	public void setHologramsManager(@NotNull BqHologramManager newHologramsManager) {
		Validate.notNull(newHologramsManager);
		if (hologramsManager != null)
			LOGGER.warning(newHologramsManager.getClass().getSimpleName()
					+ " will replace " + hologramsManager.getClass().getSimpleName() + " as the new holograms manager.");
		hologramsManager = newHologramsManager;
		LOGGER.debug("Holograms manager has been registered: " + newHologramsManager.getClass().getName());
	}

	@Override
	public @NotNull BQBlocksManagerImplementation getBlocksManager() {
		return blocksManager;
	}

	@Override
	public void registerQuestsHandler(@NotNull QuestsHandler handler) {
		Validate.notNull(handler);
		if (handlers.add(handler) && plugin.loaded)
			handler.load(); // if BeautyQuests not loaded so far, it will automatically call the load method
	}

	@Override
	public void unregisterQuestsHandler(@NotNull QuestsHandler handler) {
		if (handlers.remove(handler))
			handler.unload();
	}

	@Override
	public @NotNull Collection<@NotNull QuestsHandler> getQuestsHandlers() {
		return handlers;
	}

	@Override
	public void propagateQuestsHandlers(@NotNull Consumer<@NotNull QuestsHandler> consumer) {
		handlers.forEach(handler -> {
			try {
				consumer.accept(handler);
			} catch (Exception ex) {
				LOGGER.severe("An error occurred while updating quests handler.", ex);
			}
		});
	}

	@Override
	public @NotNull Collection<MessageProcessor> getMessageProcessors() {
		return processors.stream().map(x -> x.processor).collect(Collectors.toList());
	}

	@Override
	public void registerMessageProcessor(@NotNull String key, int priotity, @NotNull MessageProcessor processor) {
		Optional<MessageProcessorInfo> existing =
				processors.stream().filter(x -> x.key.equals(key) && x.priority == priotity).findAny();
		if (existing.isPresent()) {
			processors.remove(existing.get());
			plugin.getLogger().warning("Replacing message processor " + key);
		}

		processors.add(new MessageProcessorInfo(key, priotity, processor));
	}

	@Override
	public @NotNull MessageSender getMessageSender() {
		return messageSender;
	}

	@Override
	public void setMessageSender(@NotNull MessageSender sender) {
		this.messageSender = sender;
		LOGGER.debug("Message sender has been registered: " + sender.getClass().getName());
	}

	@Override
	public @NotNull QuestsManager getQuestsManager() {
		return plugin.getQuestsManager();
	}

	@Override
	public @NotNull QuestPoolsManager getPoolsManager() {
		return plugin.getPoolsManager();
	}

	@Override
	public @NotNull QuesterManager getQuesterManager() {
		return plugin.getQuesterManager();
	}

	@Override
	public @NotNull QuestsPlugin getPlugin() {
		return plugin;
	}

	private class MessageProcessorInfo implements Comparable<MessageProcessorInfo> {
		private static final Comparator<MessageProcessorInfo> COMPARATOR = Comparator
				.<MessageProcessorInfo>comparingInt(info -> info.priority)
				.thenComparing(info -> info.key);

		private String key;
		private int priority;
		private MessageProcessor processor;

		public MessageProcessorInfo(String key, int priority, MessageProcessor processor) {
			this.key = key;
			this.priority = priority;
			this.processor = processor;
		}

		@Override
		public int compareTo(MessageProcessorInfo o) {
			return COMPARATOR.compare(this, o);
		}
	}

}
