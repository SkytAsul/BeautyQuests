package fr.skytasul.quests;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.BossBarManager;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.mobs.MobStacker;
import fr.skytasul.quests.api.npcs.BQNPCsManager;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.StageTypeRegistry;

public class QuestsAPIImplementation implements QuestsAPI {

	static final QuestsAPIImplementation INSTANCE = new QuestsAPIImplementation();

	private final QuestObjectsRegistry<AbstractRequirement, RequirementCreator> requirements =
			new QuestObjectsRegistry<>("requirements", Lang.INVENTORY_REQUIREMENTS.toString());
	private final QuestObjectsRegistry<AbstractReward, RewardCreator> rewards =
			new QuestObjectsRegistry<>("rewards", Lang.INVENTORY_REWARDS.toString());
	private final StageTypeRegistry stages = new StageTypeRegistry();
	private final List<ItemComparison> itemComparisons = new LinkedList<>();
	private final List<MobStacker> mobStackers = new ArrayList<>();

	private BQNPCsManager npcsManager = null;
	private AbstractHolograms<?> hologramsManager = null;
	private BossBarManager bossBarManager = null;

	private final Set<QuestsHandler> handlers = new HashSet<>();

	private QuestsAPIImplementation() {}

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
		Bukkit.getPluginManager().registerEvents(factory, BeautyQuests.getInstance());
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
	public @NotNull BQNPCsManager getNPCsManager() {
		return npcsManager;
	}

	@Override
	public void setNPCsManager(@NotNull BQNPCsManager newNpcsManager) {
		if (npcsManager != null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(newNpcsManager.getClass().getSimpleName() + " will replace "
					+ npcsManager.getClass().getSimpleName() + " as the new NPCs manager.");
			HandlerList.unregisterAll(npcsManager);
		}
		npcsManager = newNpcsManager;
		Bukkit.getPluginManager().registerEvents(npcsManager, BeautyQuests.getInstance());
	}

	@Override
	public boolean hasHologramsManager() {
		return hologramsManager != null;
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
	public boolean hasBossBarManager() {
		return bossBarManager != null;
	}

	@Override
	public @Nullable BossBarManager getBossBarManager() {
		return bossBarManager;
	}

	@Override
	public void setBossBarManager(@NotNull BossBarManager newBossBarManager) {
		Validate.notNull(newBossBarManager);
		if (bossBarManager != null)
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(newBossBarManager.getClass().getSimpleName()
					+ " will replace " + hologramsManager.getClass().getSimpleName() + " as the new boss bar manager.");
		bossBarManager = newBossBarManager;
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.debug("Bossbars manager has been registered: " + newBossBarManager.getClass().getName());
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
	public @NotNull QuestsManager getQuestsManager() {
		return BeautyQuests.getInstance().getQuestsManager();
	}

	@Override
	public @NotNull QuestPoolsManager getPoolsManager() {
		return BeautyQuests.getInstance().getPoolsManager();
	}

	@Override
	public @NotNull QuestsPlugin getPlugin() {
		return BeautyQuests.getInstance();
	}

}
