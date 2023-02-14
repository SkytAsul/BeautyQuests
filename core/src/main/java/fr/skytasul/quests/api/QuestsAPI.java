package fr.skytasul.quests.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.bossbar.BQBossBarManager;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BQNPCsManager;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.structure.QuestsManager;
import fr.skytasul.quests.structure.pools.QuestPoolsManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;

/**
 * This class contains most of the useful accessors to fetch data from BeautyQuests
 * and methods to implement custom behaviors.
 */
public final class QuestsAPI {
	
	private static final QuestObjectsRegistry<AbstractRequirement, RequirementCreator> requirements = new QuestObjectsRegistry<>("requirements", Lang.INVENTORY_REQUIREMENTS.toString());
	private static final QuestObjectsRegistry<AbstractReward, RewardCreator> rewards = new QuestObjectsRegistry<>("rewards", Lang.INVENTORY_REWARDS.toString());
	private static final StageTypeRegistry stages = new StageTypeRegistry();
	private static final List<ItemComparison> itemComparisons = new LinkedList<>();
	private static final List<MobStacker> mobStackers = new ArrayList<>();
	
	private static BQNPCsManager npcsManager = null;
	private static AbstractHolograms<?> hologramsManager = null;
	private static BQBossBarManager bossBarManager = null;
	
	private static final Set<QuestsHandler> handlers = new HashSet<>();
	
	private QuestsAPI() {}
	
	/**
	 * Register new stage type into the plugin
	 * @param type StageType instance
	 * @deprecated use {@link StageTypeRegistry#register(StageType)}
	 */
	@Deprecated
	public static <T extends AbstractStage> void registerStage(StageType<T> type) { // TODO remove, edited on 0.20
		stages.register(type);
	}
	
	public static StageTypeRegistry getStages() {
		return stages;
	}
	
	/**
	 * Register new mob factory
	 * @param factory MobFactory instance
	 */
	public static void registerMobFactory(MobFactory<?> factory) {
		MobFactory.factories.add(factory);
		Bukkit.getPluginManager().registerEvents(factory, BeautyQuests.getInstance());
		DebugUtils.logMessage("Mob factory registered (id: " + factory.getID() + ")");
	}
	
	public static void registerQuestOption(QuestOptionCreator<?, ?> creator) {
		Validate.notNull(creator);
		Validate.isTrue(!QuestOptionCreator.creators.containsKey(creator.optionClass), "This quest option was already registered");
		QuestOptionCreator.creators.put(creator.optionClass, creator);
		DebugUtils.logMessage("Quest option registered (id: " + creator.id + ")");
	}
	
	public static List<ItemComparison> getItemComparisons() {
		return itemComparisons;
	}
	
	public static void registerItemComparison(ItemComparison comparison) {
		Validate.isTrue(itemComparisons.stream().noneMatch(x -> x.getID().equals(comparison.getID())),
				"This item comparison was already registered");
		itemComparisons.add(comparison);
		DebugUtils.logMessage("Item comparison registered (id: " + comparison.getID() + ")");
	}

	public static void unregisterItemComparison(ItemComparison comparison) {
		Validate.isTrue(itemComparisons.remove(comparison), "This item comparison was not registered");
		DebugUtils.logMessage("Item comparison unregistered (id: " + comparison.getID() + ")");
	}
	
	public static List<MobStacker> getMobStackers() {
		return mobStackers;
	}

	public static void registerMobStacker(MobStacker stacker) {
		mobStackers.add(stacker);
		DebugUtils.logMessage("Added " + stacker.toString() + " mob stacker");
	}

	public static QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getRequirements() {
		return requirements;
	}
	
	public static QuestObjectsRegistry<AbstractReward, RewardCreator> getRewards() {
		return rewards;
	}
	
	public static BQNPCsManager getNPCsManager() {
		return npcsManager;
	}
	
	public static void setNPCsManager(BQNPCsManager newNpcsManager) {
		if (npcsManager != null) {
			BeautyQuests.logger.warning(newNpcsManager.getClass().getSimpleName() + " will replace " + npcsManager.getClass().getSimpleName() + " as the new NPCs manager.");
			HandlerList.unregisterAll(npcsManager);
		}
		npcsManager = newNpcsManager;
		Bukkit.getPluginManager().registerEvents(npcsManager, BeautyQuests.getInstance());
	}
	
	public static boolean hasHologramsManager() {
		return hologramsManager != null;
	}
	
	public static AbstractHolograms<?> getHologramsManager() {
		return hologramsManager;
	}
	
	public static void setHologramsManager(AbstractHolograms<?> newHologramsManager) {
		Validate.notNull(newHologramsManager);
		if (hologramsManager != null) BeautyQuests.logger.warning(newHologramsManager.getClass().getSimpleName() + " will replace " + hologramsManager.getClass().getSimpleName() + " as the new holograms manager.");
		hologramsManager = newHologramsManager;
		DebugUtils.logMessage("Holograms manager has been registered: " + newHologramsManager.getClass().getName());
	}
	
	public static boolean hasBossBarManager() {
		return bossBarManager != null;
	}
	
	public static BQBossBarManager getBossBarManager() {
		return bossBarManager;
	}
	
	public static void setBossBarManager(BQBossBarManager newBossBarManager) {
		Validate.notNull(newBossBarManager);
		if (bossBarManager != null) BeautyQuests.logger.warning(newBossBarManager.getClass().getSimpleName() + " will replace " + hologramsManager.getClass().getSimpleName() + " as the new boss bar manager.");
		bossBarManager = newBossBarManager;
		DebugUtils.logMessage("Bossbars manager has been registered: " + newBossBarManager.getClass().getName());
	}
	
	public static void registerQuestsHandler(QuestsHandler handler) {
		Validate.notNull(handler);
		if (handlers.add(handler) && BeautyQuests.loaded)
			handler.load(); // if BeautyQuests not loaded so far, it will automatically call the load method
	}
	
	public static void unregisterQuestsHandler(QuestsHandler handler) {
		if (handlers.remove(handler)) handler.unload();
	}
	
	public static Collection<QuestsHandler> getQuestsHandlers() {
		return handlers;
	}
	
	public static void propagateQuestsHandlers(Consumer<QuestsHandler> consumer) {
		handlers.forEach(handler -> {
			try {
				consumer.accept(handler);
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An error occurred while updating quests handler.", ex);
			}
		});
	}
	
	public static QuestsManager getQuests() {
		return BeautyQuests.getInstance().getQuestsManager();
	}
	
	public static QuestPoolsManager getQuestPools() {
		return BeautyQuests.getInstance().getPoolsManager();
	}
	
}
