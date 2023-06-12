package fr.skytasul.quests.api.stages;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.options.StageOption;

public abstract class AbstractStage {
	
	protected final @NotNull StageController controller;
	
	private @Nullable String startMessage = null;
	private @Nullable String customText = null;
	private @NotNull RewardList rewards = new RewardList();
	private @NotNull RequirementList validationRequirements = new RequirementList();
	
	private @NotNull List<@NotNull StageOption> options;
	
	protected AbstractStage(@NotNull StageController controller) {
		this.controller = controller;

		options = controller.getStageType().getOptionsRegistry().getCreators().stream().map(SerializableCreator::newObject)
				.collect(Collectors.toList());
	}
	
	public @NotNull StageController getController() {
		return controller;
	}
	
	public @NotNull Quest getQuest() {
		return controller.getBranch().getQuest();
	}

	public void setStartMessage(@Nullable String text) {
		this.startMessage = text;
	}
	
	public @Nullable String getStartMessage() {
		return startMessage;
	}
	
	public @NotNull RewardList getRewards() {
		return rewards;
	}
	
	public void setRewards(@NotNull RewardList rewards) {
		this.rewards = rewards;
		rewards.attachQuest(getQuest());
	}

	public @NotNull RequirementList getValidationRequirements() {
		return validationRequirements;
	}

	public void setValidationRequirements(@NotNull RequirementList validationRequirements) {
		this.validationRequirements = validationRequirements;
		validationRequirements.attachQuest(getQuest());
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
		return startMessage == null && QuestsConfiguration.getConfig().getQuestsConfig().playerStageStartMessage();
	}
	
	public boolean hasAsyncEnd() {
		return rewards.hasAsync();
	}

	protected boolean canUpdate(@NotNull Player player) {
		return canUpdate(player, false);
	}

	protected boolean canUpdate(@NotNull Player player, boolean msg) {
		return validationRequirements.testPlayer(player, msg);
	}
	
	/**
	 * Called internally when a player finish stage's objectives
	 * 
	 * @param player Player who finish the stage
	 */
	protected final void finishStage(@NotNull Player player) {
		controller.finishStage(player);
	}
	
	/**
	 * Called internally to test if a player has the stage started
	 * 
	 * @param player Player to test
	 * @see QuestBranch#hasStageLaunched(PlayerAccount, AbstractStage)
	 */
	protected final boolean hasStarted(@NotNull Player player) {
		return controller.hasStarted(PlayersManager.getPlayerAccount(player));
	}
	
	/**
	 * Called when the stage starts (player can be offline)
	 * @param acc PlayerAccount for which the stage starts
	 */
	public void started(@NotNull PlayerAccount acc) {}
	
	/**
	 * Called when the stage ends (player can be offline)
	 * @param acc PlayerAccount for which the stage ends
	 */
	public void ended(@NotNull PlayerAccount acc) {}

	/**
	 * Called when an account with this stage launched joins
	 */
	public void joined(@NotNull Player player) {}
	
	/**
	 * Called when an account with this stage launched leaves
	 */
	public void left(@NotNull Player player) {}
	
	public void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {}

	/**
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return the progress of the stage for the player
	 */
	public abstract @NotNull String descriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source);
	
	/**
	 * Will be called only if there is a {@link #customText}
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return all strings that can be used to format the custom description text
	 */
	public @Nullable Object @NotNull [] descriptionFormat(@NotNull PlayerAccount acc, @NotNull DescriptionSource source) {
		return null;
	}
	
	public void updateObjective(@NotNull Player p, @NotNull String dataKey, @Nullable Object dataValue) {
		controller.updateObjective(p, dataKey, dataValue);
	}

	protected <T> @Nullable T getData(@NotNull Player p, @NotNull String dataKey) {
		return getData(PlayersManager.getPlayerAccount(p), dataKey);
	}

	protected <T> @Nullable T getData(@NotNull PlayerAccount acc, @NotNull String dataKey) {
		return controller.getData(acc, dataKey);
	}

	/**
	 * Called when the stage has to be unloaded
	 */
	public void unload(){
		rewards.detachQuest();
		validationRequirements.detachQuest();
	}
	
	/**
	 * Called when the stage loads
	 */
	public void load() {}
	
	protected void serialize(@NotNull ConfigurationSection section) {}
	
	public final void save(@NotNull ConfigurationSection section) {
		serialize(section);
		
		section.set("stageType", controller.getStageType().getID());
		section.set("customText", customText);
		if (startMessage != null) section.set("text", startMessage);
		
		if (!rewards.isEmpty())
			section.set("rewards", rewards.serialize());
		if (!validationRequirements.isEmpty())
			section.set("requirements", validationRequirements.serialize());
		
		options.stream().filter(StageOption::shouldSave).forEach(option -> option.save(section.createSection("options." + option.getCreator().getID())));
	}
	
	public final void load(@NotNull ConfigurationSection section) {
		if (section.contains("text"))
			startMessage = section.getString("text");
		if (section.contains("customText"))
			customText = section.getString("customText");
		if (section.contains("rewards"))
			setRewards(RewardList.deserialize(section.getMapList("rewards")));
		if (section.contains("requirements"))
			setValidationRequirements(RequirementList.deserialize(section.getMapList("requirements")));

		if (section.contains("options")) {
			ConfigurationSection optionsSection = section.getConfigurationSection("options");
			optionsSection.getKeys(false).forEach(optionID -> {
				options
						.stream()
						.filter(option -> option.getCreator().getID().equals(optionID))
						.findAny()
						.ifPresent(option -> option.load(optionsSection.getConfigurationSection(optionID)));
			});
		}
	}
}
