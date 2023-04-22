package fr.skytasul.quests.api.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Utils;

public abstract class AbstractStage implements Listener {
	
	private final @NotNull StageType<?> type;
	
	protected final @NotNull QuestBranch branch;
	
	private @Nullable String startMessage = null;
	private @Nullable String customText = null;
	private @NotNull List<@NotNull AbstractReward> rewards = new ArrayList<>();
	private @NotNull List<@NotNull AbstractRequirement> validationRequirements = new ArrayList<>();
	
	private @NotNull List<@NotNull StageOption> options;
	protected boolean asyncEnd = false;
	
	protected AbstractStage(@NotNull QuestBranch branch) {
		this.branch = branch;
		this.type = QuestsAPI.getStages().getType(getClass()).orElseThrow(() -> new IllegalArgumentException(getClass().getName() + "has not been registered as a stage type via the API."));
		
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}
	
	public @NotNull QuestBranch getQuestBranch() {
		return branch;
	}
	
	public void setStartMessage(@Nullable String text) {
		this.startMessage = text;
	}
	
	public @Nullable String getStartMessage() {
		return startMessage;
	}
	
	public @NotNull List<@NotNull AbstractReward> getRewards() {
		return rewards;
	}
	
	public void setRewards(@NotNull List<@NotNull AbstractReward> rewards) {
		this.rewards = rewards;
		rewards.forEach(reward -> reward.attach(branch.getQuest()));
		checkAsync();
	}

	public @NotNull List<@NotNull AbstractRequirement> getValidationRequirements() {
		return validationRequirements;
	}

	public void setValidationRequirements(@NotNull List<@NotNull AbstractRequirement> validationRequirements) {
		this.validationRequirements = validationRequirements;
		validationRequirements.forEach(requirement -> requirement.attach(branch.getQuest()));
	}
	
	public @NotNull List<@NotNull StageOption> getOptions() {
		return options;
	}
	
	public void setOptions(@NotNull List<@NotNull StageOption> options) {
		this.options = options;
	}

	public @Nullable String getCustomText() {
		return customText;
	}
	
	public void setCustomText(@Nullable String message) {
		this.customText = message;
	}
	
	public boolean sendStartMessage(){
		return startMessage == null && QuestsConfiguration.sendStageStartMessage();
	}
	
	public @NotNull StageType<?> getType() {
		return type;
	}
	
	public boolean hasAsyncEnd(){
		return asyncEnd;
	}
	
	private void checkAsync(){
		for(AbstractReward rew : rewards){
			if (rew.isAsync()) {
				asyncEnd = true;
				break;
			}
		}
	}
	
	public int getID(){
		return branch.getID(this);
	}
	
	public int getStoredID(){
		if (branch.isRegularStage(this)) {
			return 0;
		}
		int index = 0;
		for (AbstractStage stage : branch.getEndingStages().keySet()) {
			if (stage == this) break;
			index++;
		}
		return index;
	}
	
	protected boolean canUpdate(@NotNull Player p) {
		return canUpdate(p, false);
	}

	protected boolean canUpdate(@NotNull Player p, boolean msg) {
		return Utils.testRequirements(p, validationRequirements, msg);
	}
	
	@Override
	public String toString() {
		return "stage " + getID() + "(" + type.getID() + ") of quest " + branch.getQuest().getID() + ", branch " + branch.getID();
	}

	private void propagateStageHandlers(@NotNull Consumer<@NotNull StageHandler> consumer) {
		Consumer<StageHandler> newConsumer = handler -> {
			try {
				consumer.accept(handler);
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An error occurred while updating stage handler.", ex);
			}
		};
		QuestsAPI.getQuestsHandlers().forEach(newConsumer);
		options.forEach(newConsumer);
	}
	
	/**
	 * Called internally when a player finish stage's objectives
	 * @param p Player who finish the stage
	 */
	protected final void finishStage(@NotNull Player p) {
		Utils.runSync(() -> branch.finishStage(p, this));
	}
	
	/**
	 * Called internally to test if a player has the stage started
	 * @param p Player to test
	 * @see QuestBranch#hasStageLaunched(PlayerAccount, AbstractStage)
	 */
	protected final boolean hasStarted(@NotNull Player p) {
		return branch.hasStageLaunched(PlayersManager.getPlayerAccount(p), this);
	}
	
	/**
	 * Called when the stage starts (player can be offline)
	 * @param acc PlayerAccount for which the stage starts
	 */
	public void start(@NotNull PlayerAccount acc) {
		if (acc.isCurrent()) Utils.sendOffMessage(acc.getPlayer(), startMessage);
		Map<String, Object> datas = new HashMap<>();
		initPlayerDatas(acc, datas);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), datas);
		propagateStageHandlers(handler -> handler.stageStart(acc, this));
	}
	
	protected void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {}

	/**
	 * Called when the stage ends (player can be offline)
	 * @param acc PlayerAccount for which the stage ends
	 */
	public void end(@NotNull PlayerAccount acc) {
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), null);
		propagateStageHandlers(handler -> handler.stageEnd(acc, this));
	}
	
	/**
	 * Called when an account with this stage launched joins
	 * @param acc PlayerAccount which just joined
	 */
	public void joins(@NotNull PlayerAccount acc, @NotNull Player p) {
		propagateStageHandlers(handler -> handler.stageJoin(acc, p, this));
	}
	
	/**
	 * Called when an account with this stage launched leaves
	 * @param acc PlayerAccount which just left
	 */
	public void leaves(@NotNull PlayerAccount acc, @NotNull Player p) {
		propagateStageHandlers(handler -> handler.stageLeave(acc, p, this));
	}
	
	public final @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull Source source) {
		if (customText != null) return "§e" + Utils.format(customText, descriptionFormat(acc, source));
		try{
			return descriptionLine(acc, source);
		}catch (Exception ex){
			BeautyQuests.logger.severe("An error occurred while getting the description line for player " + acc.getName() + " in " + toString(), ex);
			return "§a" + type.getName();
		}
	}
	
	/**
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return the progress of the stage for the player
	 */
	protected abstract @NotNull String descriptionLine(@NotNull PlayerAccount acc, @NotNull Source source);
	
	/**
	 * Will be called only if there is a {@link #customText}
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return all strings that can be used to format the custom description text
	 */
	protected @Nullable Object @NotNull [] descriptionFormat(@NotNull PlayerAccount acc, @NotNull Source source) {
		return null;
	}
	
	public void updateObjective(@NotNull PlayerAccount acc, @NotNull Player p, @NotNull String dataKey,
			@Nullable Object dataValue) {
		Map<String, Object> datas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStoredID());
		Validate.notNull(datas, "Account " + acc.debugName() + " does not have datas for " + toString());
		datas.put(dataKey, dataValue);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), datas);
		branch.getBranchesManager().objectiveUpdated(p, acc);
	}

	protected <T> @Nullable T getData(@NotNull PlayerAccount acc, @NotNull String dataKey) {
		Map<String, Object> stageDatas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStoredID());
		return stageDatas == null ? null : (T) stageDatas.get(dataKey);
	}

	/**
	 * Called when the stage has to be unloaded
	 */
	public void unload(){
		propagateStageHandlers(handler -> handler.stageUnload(this));
        HandlerList.unregisterAll(this);
		rewards.forEach(AbstractReward::detach);
		validationRequirements.forEach(AbstractRequirement::detach);
	}
	
	/**
	 * Called when the stage loads
	 */
	public void load() {
		propagateStageHandlers(handler -> handler.stageLoad(this));
	}
	
	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (e.isFirstJoin()) return;
		if (branch.hasStageLaunched(e.getPlayerAccount(), this)) {
			joins(e.getPlayerAccount(), e.getPlayer());
		}
	}
	
	@EventHandler
	public void onLeave(PlayerAccountLeaveEvent e) {
		if (branch.hasStageLaunched(e.getPlayerAccount(), this)) {
			leaves(e.getPlayerAccount(), e.getPlayer());
		}
	}
	
	/**
	 * @deprecated for removal, {@link #serialize(ConfigurationSection)} should be used instead.
	 */
	@Deprecated
	protected void serialize(Map<String, Object> map) {}
	
	protected void serialize(@NotNull ConfigurationSection section) {
		Map<String, Object> map = new HashMap<>();
		serialize(map);
		Utils.setConfigurationSectionContent(section, map);
	}
	
	public final void save(@NotNull ConfigurationSection section) {
		serialize(section);
		
		section.set("stageType", type.getID());
		section.set("customText", customText);
		if (startMessage != null) section.set("text", startMessage);
		
		if (!rewards.isEmpty()) section.set("rewards", SerializableObject.serializeList(rewards));
		if (!validationRequirements.isEmpty()) section.set("requirements", SerializableObject.serializeList(validationRequirements));
		
		options.stream().filter(StageOption::shouldSave).forEach(option -> option.save(section.createSection("options." + option.getCreator().getID())));
	}
	
	public static @NotNull AbstractStage deserialize(@NotNull ConfigurationSection section, @NotNull QuestBranch branch) {
		String typeID = section.getString("stageType");
		
		Optional<StageType<?>> stageTypeOptional = QuestsAPI.getStages().getType(typeID);
		if (!stageTypeOptional.isPresent()) {
			BeautyQuests.getInstance().getLogger().severe("Unknown stage type : " + typeID);
			return null;
		}
		
		StageType<?> stageType = stageTypeOptional.get();
		if (!stageType.isValid()) {
			BeautyQuests.getInstance().getLogger().severe("The stage " + typeID + " requires not enabled dependencies: " + Arrays.toString(stageType.dependencies));
			return null;
		}

		AbstractStage st = stageType.getLoader().supply(section, branch);
		if (section.contains("text")) st.startMessage = section.getString("text");
		if (section.contains("customText")) st.customText = section.getString("customText");
		if (section.contains("rewards")) st.setRewards(QuestObject.deserializeList(section.getMapList("rewards"), AbstractReward::deserialize));
		if (section.contains("requirements")) st.setValidationRequirements(QuestObject.deserializeList(section.getMapList("requirements"), AbstractRequirement::deserialize));
		
		st.options = stageType.getOptionsRegistry().getCreators().stream().map(SerializableCreator::newObject).collect(Collectors.toList());
		if (section.contains("options")) {
			ConfigurationSection optionsSection = section.getConfigurationSection("options");
			optionsSection.getKeys(false).forEach(optionID -> {
				st.options
					.stream()
					.filter(option -> option.getCreator().getID().equals(optionID))
					.findAny()
					.ifPresent(option -> option.load(optionsSection.getConfigurationSection(optionID)));
			});
			
		}
		
		return st;
	}
}
