package fr.skytasul.quests.api.stages.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.options.StageOption;

public abstract class StageCreation<T extends AbstractStage> {

	protected static final int SLOT_REWARDS = 1;
	protected static final int SLOT_DESCRIPTION = 2;
	protected static final int SLOT_MESSAGE = 3;
	protected static final int SLOT_REQUIREMENTS = 4;

	private static final ItemStack ENDING_ITEM = ItemUtils.item(XMaterial.BAKED_POTATO, Lang.ending.toString());
	private static final ItemStack DESCRITPION_MESSAGE_ITEM =
			ItemUtils.item(XMaterial.OAK_SIGN, Lang.descMessage.toString());
	private static final ItemStack START_MESSAGE_ITEM = ItemUtils.item(XMaterial.FEATHER, Lang.startMsg.toString());
	private static final ItemStack VALIDATION_REQUIREMENTS_ITEM =
			ItemUtils.item(XMaterial.NETHER_STAR, Lang.validationRequirements.toString(),
					QuestOption.formatDescription(Lang.validationRequirementsLore.toString()));

	protected final @NotNull StageCreationContext<T> context;

	private List<AbstractReward> rewards;
	private List<AbstractRequirement> requirements;

	private List<StageOption<T>> options;

	private String customDescription, startMessage;

	protected StageCreation(@NotNull StageCreationContext<T> context) {
		this.context = context;
	}

	public @NotNull StageCreationContext<T> getCreationContext() {
		return context;
	}

	public @NotNull StageGuiLine getLine() {
		return context.getLine();
	}

	public List<AbstractReward> getRewards() {
		return rewards;
	}

	public void setRewards(List<AbstractReward> rewards) {
		this.rewards = rewards;
		getLine().refreshItemLore(1, QuestOption.formatDescription(RewardList.getSizeString(rewards.size())));
	}

	public List<AbstractRequirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<AbstractRequirement> requirements) {
		getLine().refreshItemLore(4, QuestOption.formatDescription(RequirementList.getSizeString(requirements.size())));
		this.requirements = requirements;
	}

	public String getCustomDescription() {
		return customDescription;
	}

	public void setCustomDescription(String customDescription) {
		this.customDescription = customDescription;
		getLine().refreshItemLore(2, QuestOption.formatNullableValue(customDescription));
	}

	public String getStartMessage() {
		return startMessage;
	}

	public void setStartMessage(String startMessage) {
		this.startMessage = startMessage;
		getLine().refreshItemLore(3, QuestOption.formatNullableValue(startMessage));
	}

	public void setupLine(@NotNull StageGuiLine line) {
		line.setItem(SLOT_REWARDS, ENDING_ITEM.clone(),
				event -> QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.STAGE, rewards -> {
					setRewards(rewards);
					event.reopen();
				}, rewards).open(event.getPlayer()));

		line.setItem(SLOT_DESCRIPTION, DESCRITPION_MESSAGE_ITEM.clone(), event -> {
			Lang.DESC_MESSAGE.send(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
				setCustomDescription(obj);
				event.reopen();
			}).passNullIntoEndConsumer().start();
		});

		line.setItem(SLOT_MESSAGE, START_MESSAGE_ITEM.clone(), event -> {
			Lang.START_TEXT.send(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
				setStartMessage(obj);
				event.reopen();
			}).passNullIntoEndConsumer().start();
		});

		line.setItem(SLOT_REQUIREMENTS, VALIDATION_REQUIREMENTS_ITEM.clone(), event -> {
			QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.STAGE, requirements -> {
				setRequirements(requirements);
				event.reopen();
			}, requirements).open(event.getPlayer());
		});
	}

	/**
	 * Called when stage item clicked
	 * @param p player who click on the item
	 */
	public void start(@NotNull Player p) {
		setRewards(new ArrayList<>());
		setRequirements(new ArrayList<>());
		setCustomDescription(null);
		setStartMessage(null);

		options = context.getType().getOptionsRegistry().getCreators().stream().map(SerializableCreator::newObject)
				.collect(Collectors.toList());
		options.forEach(option -> option.startEdition(this));
	}

	/**
	 * Called when quest edition started
	 * @param stage Existing stage
	 */
	public void edit(@NotNull T stage) {
		setRewards(stage.getRewards());
		setRequirements(stage.getValidationRequirements());
		setStartMessage(stage.getStartMessage());
		setCustomDescription(stage.getCustomText());

		options = stage.getOptions().stream().map(StageOption::clone).map(x -> (StageOption<T>) x).collect(Collectors.toList());
		options.forEach(option -> option.startEdition(this));
	}


	public final @NotNull T finish(@NotNull StageController branch) {
		T stage = finishStage(branch);
		stage.setRewards(new RewardList(rewards));
		stage.setValidationRequirements(new RequirementList(requirements));
		stage.setCustomText(customDescription);
		stage.setStartMessage(startMessage);
		stage.setOptions((List) options);
		return stage;
	}

	/**
	 * Called when quest creation finished
	 * @param branch quest created
	 * @return AsbtractStage created
	 */
	protected abstract @NotNull T finishStage(@NotNull StageController branch);

}
