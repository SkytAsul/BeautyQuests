package fr.skytasul.quests.api.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;

public abstract class StageCreation<T extends AbstractStage> {
	
	protected final Line line;
	private final boolean ending;
	private StageType<T> type;
	
	private List<AbstractReward> rewards;
	private List<AbstractRequirement> requirements;
	
	private List<StageOption<T>> options;
	
	private String customDescription, startMessage;
	
	private StagesGUI leadingBranch;
	
	public StageCreation(Line line, boolean ending) {
		this.line = line;
		this.ending = ending;
		
		line.setItem(1, StagesGUI.ending.clone(), (p, item) -> QuestsAPI.getRewards().createGUI(QuestObjectLocation.STAGE, rewards -> {
			setRewards(rewards);
			reopenGUI(p, true);
		}, rewards).create(p));
		
		line.setItem(2, StagesGUI.descMessage.clone(), (p, item) -> {
			Lang.DESC_MESSAGE.send(p);
			new TextEditor<String>(p, () -> reopenGUI(p, false), obj -> {
				setCustomDescription(obj);
				reopenGUI(p, false);
			}).passNullIntoEndConsumer().enter();
		});
		
		line.setItem(3, StagesGUI.startMessage.clone(), (p, item) -> {
			Lang.START_TEXT.send(p);
			new TextEditor<String>(p, () -> reopenGUI(p, false), obj -> {
				setStartMessage(obj);
				reopenGUI(p, false);
			}).passNullIntoEndConsumer().enter();
		});
		
		line.setItem(4, StagesGUI.validationRequirements.clone(), (p, item) -> {
			QuestsAPI.getRequirements().createGUI(QuestObjectLocation.STAGE, requirements -> {
				setRequirements(requirements);
				reopenGUI(p, true);
			}, requirements).create(p);
		});
	}
	
	public StageType<T> getType() {
		return type;
	}
	
	public Line getLine() {
		return line;
	}
	
	public void reopenGUI(Player p, boolean reImplement) {
		line.gui.reopen(p, reImplement);
	}
	
	public void remove() {
		line.gui.deleteStageLine(line);
	}
	
	protected Runnable removeAndReopen(Player p, boolean reImplement) {
		return () -> {
			remove();
			reopenGUI(p, reImplement);
		};
	}
	
	public boolean isEndingStage() {
		return ending;
	}
	
	public List<AbstractReward> getRewards() {
		return rewards;
	}
	
	public void setRewards(List<AbstractReward> rewards) {
		this.rewards = rewards;
		line.editItem(1, ItemUtils.lore(line.getItem(1), QuestOption.formatDescription(Lang.rewards.format(rewards.size()))));
	}
	
	public List<AbstractRequirement> getRequirements() {
		return requirements;
	}
	
	public void setRequirements(List<AbstractRequirement> requirements) {
		line.editItem(4, ItemUtils.lore(line.getItem(4), QuestOption.formatDescription(Lang.requirements.format(requirements.size()))));
		this.requirements = requirements;
	}
	
	public String getCustomDescription() {
		return customDescription;
	}
	
	public void setCustomDescription(String customDescription) {
		this.customDescription = customDescription;
		line.editItem(2, ItemUtils.lore(line.getItem(2), QuestOption.formatNullableValue(customDescription)));
	}
	
	public String getStartMessage() {
		return startMessage;
	}
	
	public void setStartMessage(String startMessage) {
		this.startMessage = startMessage;
		line.editItem(3, ItemUtils.lore(line.getItem(3), QuestOption.formatNullableValue(startMessage)));
	}
	
	public StagesGUI getLeadingBranch() {
		return leadingBranch;
	}
	
	public void setLeadingBranch(StagesGUI leadingBranch) {
		this.leadingBranch = leadingBranch;
	}
	
	public final void setup(StageType<T> type) {
		this.type = type;
	}
	
	/**
	 * Called when stage item clicked
	 * @param p player who click on the item
	 */
	public void start(Player p) {
		setRewards(new ArrayList<>());
		setRequirements(new ArrayList<>());
		setCustomDescription(null);
		setStartMessage(null);
		
		options = type.getOptionsRegistry().getCreators().stream().map(SerializableCreator::newObject).collect(Collectors.toList());
		options.forEach(option -> option.startEdition(this));
	}

	/**
	 * Called when quest edition started
	 * @param stage Existing stage
	 */
	public void edit(T stage) {
		setRewards(stage.getRewards());
		setRequirements(stage.getValidationRequirements());
		setStartMessage(stage.getStartMessage());
		setCustomDescription(stage.getCustomText());
		
		options = stage.getOptions().stream().map(StageOption::clone).map(x -> (StageOption<T>) x).collect(Collectors.toList());
		options.forEach(option -> option.startEdition(this));
	}

	public final T finish(QuestBranch branch) {
		T stage = finishStage(branch);
		stage.setRewards(rewards);
		stage.setValidationRequirements(requirements);
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
	protected abstract T finishStage(QuestBranch branch);
	
}
