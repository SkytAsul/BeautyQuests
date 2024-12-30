package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AutoRegistered
public abstract class AbstractStage implements HasPlaceholders {

	protected final @NotNull StageController controller;

	private @Nullable String startMessage = null;
	private @Nullable String customText = null;
	private @NotNull RewardList rewards = new RewardList();
	private @NotNull RequirementList validationRequirements = new RequirementList();

	private @NotNull List<@NotNull StageOption> options;

	private @Nullable PlaceholderRegistry placeholders;

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

	@Override
	public final @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry();
			createdPlaceholdersRegistry(placeholders);
		}
		return placeholders;
	}

	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		placeholders.compose(false, controller.getBranch().getQuest());
		placeholders.register("stage_type", controller.getStageType().getName());
		placeholders.register("stage_rewards", rewards.getSizeString());
		placeholders.register("stage_requirements", validationRequirements.getSizeString());
	}

	protected final boolean canUpdate(@NotNull Player player) {
		return canUpdate(player, false);
	}

	protected final boolean canUpdate(@NotNull Player player, boolean msg) {
		return validationRequirements.allMatch(player, msg);
	}

	/**
	 * To be called when a quester completes its stage.
	 *
	 * @param quester Quester which completed the stage
	 */
	protected final void finishStage(@NotNull Quester quester) {
		controller.finishStage(quester);
	}

	/**
	 * Called internally to test if a player has the stage started
	 *
	 * @param player Player to test
	 * @see QuestBranch#hasStageLaunched(Quester, AbstractStage)
	 */
	protected final boolean hasStarted(@NotNull Player player) {
		return controller.hasStarted(PlayersManager.getPlayerAccount(player));
	}

	/**
	 * Called when the stage starts (player can be offline)
	 * @param acc PlayerAccount for which the stage starts
	 */
	public void started(@NotNull Quester acc) {}

	/**
	 * Called when the stage ends (player can be offline)
	 * @param acc PlayerAccount for which the stage ends
	 */
	public void ended(@NotNull Quester acc) {}

	/**
	 * Called when an account with this stage launched joins
	 */
	public void joined(@NotNull Player player) {}

	/**
	 * Called when an account with this stage launched leaves
	 */
	public void left(@NotNull Player player) {}

	public void initPlayerDatas(@NotNull Quester acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {}

	public abstract @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context);

	protected final void updateObjective(@NotNull Quester quester, @NotNull String dataKey, @Nullable Object dataValue) {
		controller.updateObjective(quester, dataKey, dataValue);
	}

	@Deprecated
	protected final <T> @Nullable T getData(@NotNull Player p, @NotNull String dataKey) {
		return getData(PlayersManager.getPlayerAccount(p), dataKey);
	}

	@Deprecated
	protected final <T> @Nullable T getData(@NotNull Quester acc, @NotNull String dataKey) {
		return getData(acc, dataKey, null);
	}

	protected final <T> @Nullable T getData(@NotNull Player p, @NotNull String dataKey, @NotNull Class<T> dataType) {
		return getData(PlayersManager.getPlayerAccount(p), dataKey, dataType);
	}

	protected final <T> @Nullable T getData(@NotNull Quester acc, @NotNull String dataKey,
			@NotNull Class<T> dataType) {
		return controller.getData(acc, dataKey, dataType);
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
