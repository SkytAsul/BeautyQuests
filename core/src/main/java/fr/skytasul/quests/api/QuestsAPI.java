package fr.skytasul.quests.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.NPCStarter;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;
import net.citizensnpcs.api.npc.NPC;

public class QuestsAPI {
	
	public static final Map<Class<? extends AbstractReward>, QuestObjectCreator<AbstractReward>> rewards = new HashMap<>();
	public static final Map<Class<? extends AbstractRequirement>, QuestObjectCreator<AbstractRequirement>> requirements = new HashMap<>();
	public static final LinkedList<StageType<?>> stages = new LinkedList<>();
	
	private static AbstractHolograms<?> hologramsManager = null;
	
	/**
	 * Register new stage type into the plugin
	 * @param type StageType object
	 * @param item ItemStack shown in stages GUI when choosing stage type
	 * @param runnables Instance of special runnables
	 */
	public static <T extends AbstractStage> void registerStage(StageType<T> creator) {
		stages.add(creator);
		DebugUtils.logMessage("Stage registered (" + creator.name + ", " + (stages.size() - 1) + ")");
	}
	
	/**
	 * Registers a new requirement type into the plugin
	 * @param clazz Class extending {@link AbstractRequirement}
	 * @param item ItemStack shown in requirements GUI
	 * @param newRequirementSupplier lambda returning an instance of this Requirement (Requirement::new)
	 * @deprecated use {@link QuestsAPI#registerRequirement(QuestObjectCreator)}
	 */
	@Deprecated
	public static <T extends AbstractRequirement> void registerRequirement(Class<T> clazz, ItemStack item, Supplier<T> newRequirementSupplier) {
		registerRequirement(new QuestObjectCreator<>(clazz, item, newRequirementSupplier, true));
	}
	
	/**
	 * Registers a new requirement type into the plugin
	 * @param creator {@link QuestObjectCreator} instance of an {@link AbstractRequirement}
	 */
	public static <T extends AbstractRequirement> void registerRequirement(QuestObjectCreator<T> creator) {
		requirements.put(creator.clazz, (QuestObjectCreator<AbstractRequirement>) creator);
		DebugUtils.logMessage("Requirement registered (class: " + creator.clazz.getSimpleName() + ")");
	}
	
	/**
	 * Registers a new reward type into the plugin
	 * @param clazz Class extending {@link AbstractReward}
	 * @param item ItemStack shown in rewards GUI
	 * @param newRewardSupplier lambda returning an instance of this Reward (Reward::new)
	 * @deprecated use {@link QuestsAPI#registerReward(QuestObjectCreator)}
	 */
	@Deprecated
	public static <T extends AbstractReward> void registerReward(Class<T> clazz, ItemStack item, Supplier<T> newRewardSupplier) {
		registerReward(new QuestObjectCreator<T>(clazz, item, newRewardSupplier, true));
	}
	
	/**
	 * Registers a new reward type into the plugin
	 * @param creator {@link QuestObjectCreator} instance of an {@link AbstractReward}
	 */
	public static <T extends AbstractReward> void registerReward(QuestObjectCreator<T> creator) {
		rewards.put(creator.clazz, (QuestObjectCreator<AbstractReward>) creator);
		DebugUtils.logMessage("Reward registered (class: " + creator.clazz.getSimpleName() + ")");
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
	}

	public static List<Quest> getQuestsStarteds(PlayerAccount acc){
		return getQuestsStarteds(acc, false);
	}

	public static List<Quest> getQuestsStarteds(PlayerAccount acc, boolean withoutScoreboard){
		List<Quest> launched = new ArrayList<>();
		for (PlayerQuestDatas datas : acc.getQuestsDatas()) {
			Quest quest = datas.getQuest();
			if (quest == null) continue; // non-existent quest
			if (datas.hasStarted() && (!withoutScoreboard || quest.isScoreboardEnabled())) launched.add(quest);
		}
		return launched;
	}

	public static void updateQuestsStarteds(PlayerAccount acc, boolean withoutScoreboard, List<Quest> list) {
		for (Quest qu : BeautyQuests.getInstance().getQuests()) {
			boolean contains = list.contains(qu);
			if (qu.hasStarted(acc) && (withoutScoreboard ? qu.isScoreboardEnabled() : true)) {
				if (!list.contains(qu)) list.add(qu);
			}else if (contains) list.remove(qu);
		}
	}

	public static int getStartedSize(PlayerAccount acc){
		int i = 0;
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (qu.canBypassLimit()) continue;
			if (qu.hasStarted(acc)) i++;
		}
		return i;
	}

	public static List<Quest> getQuestsFinished(PlayerAccount acc){
		List<Quest> finished = new ArrayList<>();
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (qu.hasFinished(acc)) finished.add(qu);
		}
		return finished;
	}

	public static List<Quest> getQuestsUnstarted(PlayerAccount acc, boolean hide, boolean redoable) {
		List<Quest> unstarted = new ArrayList<>();
		for (Quest qu : BeautyQuests.getInstance().getQuests()){
			if (hide && qu.isHidden()) continue;
			if (qu.hasStarted(acc)) continue;
			if (qu.hasFinished(acc)) {
				if (!redoable || !qu.isRepeatable() || !qu.testTimer(acc, false)) continue;
			}
			unstarted.add(qu);
		}
		return unstarted;
	}

	public static List<Quest> getQuestsAssigneds(NPC npc) {
		NPCStarter starter = BeautyQuests.getInstance().getNPCs().get(npc);
		return starter == null ? Collections.emptyList() : new ArrayList<>(starter.getQuests());
	}
	
	public static boolean isQuestStarter(NPC npc){
		NPCStarter starter = BeautyQuests.getInstance().getNPCs().get(npc);
		return starter != null && !starter.getQuests().isEmpty();
	}

	public static boolean hasQuestStarted(Player p, NPC npc){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		for (Quest qu : getQuestsAssigneds(npc)){
			if (qu.hasStarted(acc)) return true;
		}
		return false;
	}

	public static Quest getQuestFromID(int id){
		return BeautyQuests.getInstance().getQuests().stream().filter(x -> x.getID() == id).findFirst().orElse(null);
	}
	
	public static List<Quest> getQuests(){
		return BeautyQuests.getInstance().getQuests();
	}
	
}
