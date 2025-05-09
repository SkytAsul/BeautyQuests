package fr.skytasul.quests;

import fr.skytasul.quests.api.*;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
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

	static final QuestsAPIImplementation INSTANCE = new QuestsAPIImplementation();

	private final StageTypeRegistry stages = new StageTypeRegistry();
	private final List<ItemComparison> itemComparisons = new LinkedList<>();
	private final List<MobStacker> mobStackers = new ArrayList<>();

	private QuesterManager questerManager;

	private QuestObjectsRegistry<AbstractRequirement, RequirementCreator> requirements;
	private QuestObjectsRegistry<AbstractReward, RewardCreator> rewards;

	private AbstractHolograms<?> hologramsManager = null;
	private BQBlocksManagerImplementation blocksManager = new BQBlocksManagerImplementation();
	private MessageSender messageSender;

	private final Set<QuestsHandler> handlers = new HashSet<>();

	private final Set<MessageProcessorInfo> processors = new TreeSet<>();

	private QuestsAPIImplementation() {}

	void setup() {
		requirements = new QuestObjectsRegistry<>("requirements", Lang.INVENTORY_REQUIREMENTS.toString());
		rewards = new QuestObjectsRegistry<>("rewards", Lang.INVENTORY_REWARDS.toString());

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
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Mob factory registered (id: " + factory.getID() + ")");
	}

	@Override
	public void registerQuestOption(@NotNull QuestOptionCreator<?, ?> creator) {
		Validate.notNull(creator);
		Validate.isTrue(!QuestOptionCreator.creators.containsKey(creator.optionClass),
				"This quest option was already registered");
		QuestOptionCreator.creators.put(creator.optionClass, creator);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Quest option registered (id: " + creator.id + ")");
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
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Item comparison registered (id: " + comparison.getID() + ")");
	}

	@Override
	public void unregisterItemComparison(@NotNull ItemComparison comparison) {
		Validate.isTrue(itemComparisons.remove(comparison), "This item comparison was not registered");
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Item comparison unregistered (id: " + comparison.getID() + ")");
	}

	@Override
	public @NotNull List<@NotNull MobStacker> getMobStackers() {
		return mobStackers;
	}

	@Override
	public void registerMobStacker(@NotNull MobStacker stacker) {
		mobStackers.add(stacker);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Added " + stacker.toString() + " mob stacker");
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
	public void addNpcFactory(@NotNull String key, @NotNull BqInternalNpcFactory factory) {
		QuestsPlugin.getPlugin().getNpcManager().addInternalFactory(key, factory);
	}

	@Override
	public @Nullable AbstractHolograms<?> getHologramsManager() {
		return hologramsManager;
	}

	@Override
	public void setHologramsManager(@NotNull AbstractHolograms<?> newHologramsManager) {
		Validate.notNull(newHologramsManager);
		if (hologramsManager != null)
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(newHologramsManager.getClass().getSimpleName()
					+ " will replace " + hologramsManager.getClass().getSimpleName() + " as the new holograms manager.");
		hologramsManager = newHologramsManager;
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.debug("Holograms manager has been registered: " + newHologramsManager.getClass().getName());
	}

	@Override
	public @NotNull BQBlocksManagerImplementation getBlocksManager() {
		return blocksManager;
	}

	@Override
	public void registerQuestsHandler(@NotNull QuestsHandler handler) {
		Validate.notNull(handler);
		if (handlers.add(handler) && BeautyQuests.getInstance().loaded)
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
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while updating quests handler.", ex);
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
			BeautyQuests.getInstance().getLogger().warning("Replacing message processor " + key);
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
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.debug("Message sender has been registered: " + sender.getClass().getName());
	}

	@Override
	public @NotNull QuestsManager getQuestsManager() {
		return BeautyQuests.getInstance().getQuestsManager();
	}

	@Override
	public @NotNull QuestPoolsManager getPoolsManager() {
		return BeautyQuests.getInstance().getPoolsManager();
	}

	@Override
	public @NotNull QuesterManager getQuesterManager() {
		return BeautyQuests.getInstance().getQuesterManager();
	}

	@Override
	public @NotNull QuestsPlugin getPlugin() {
		return BeautyQuests.getInstance();
	}

	private class MessageProcessorInfo implements Comparable<MessageProcessorInfo> {
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
			return Integer.compare(priority, o.priority);
		}
	}

}
