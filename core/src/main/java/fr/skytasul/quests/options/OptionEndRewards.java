package fr.skytasul.quests.options;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class OptionEndRewards extends QuestOptionRewards implements QuestDescriptionProvider {
	
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\{JOIN\\}");
	
	@Override
	protected void attachedAsyncReward(AbstractReward reward) {
		getAttachedQuest().asyncEnd = true;
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.CHEST;
	}
	
	@Override
	public String getItemName() {
		return Lang.ending.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.rewardsLore.toString();
	}

	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (!context.getPlayerAccount().isCurrent()) return null;
		if (!context.getDescriptionOptions().showRewards()) return null;
		if (context.getCategory() == Category.FINISHED) return null;
		
		List<String> rewards = getValue().stream()
				.map(x -> x.getDescription(context.getPlayerAccount().getPlayer()))
				.filter(Objects::nonNull)
				.flatMap(SPLIT_PATTERN::splitAsStream)
				.filter(x -> !x.isEmpty())
				.map(x -> Utils.format(context.getDescriptionOptions().getRewardsFormat(), x))
				.collect(Collectors.toList());
		if (rewards.isEmpty()) return null;
		
		rewards.add(0, Lang.RWDTitle.toString());
		return rewards;
	}

	@Override
	public String getDescriptionId() {
		return "rewards";
	}

	@Override
	public double getDescriptionPriority() {
		return 10;
	}
	
}
