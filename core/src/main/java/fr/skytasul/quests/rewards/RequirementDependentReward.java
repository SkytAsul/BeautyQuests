package fr.skytasul.quests.rewards;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.rewards.RewardList;

public class RequirementDependentReward extends AbstractReward {
	
	private RequirementList requirements;
	private RewardList rewards;
	
	public RequirementDependentReward() {
		this(null, new RequirementList(), new RewardList());
	}
	
	public RequirementDependentReward(String customDescription, RequirementList requirements,
			RewardList rewards) {
		super(customDescription);
		this.requirements = requirements;
		this.rewards = rewards;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		requirements.attachQuest(quest);
		rewards.attachQuest(quest);
	}
	
	@Override
	public void detach() {
		super.detach();
		requirements.forEach(AbstractRequirement::detach);
		rewards.forEach(AbstractReward::detach);
	}
	
	@Override
	public List<String> give(Player p) throws InterruptingBranchException {
		if (requirements.stream().allMatch(requirement -> requirement.test(p)))
			return rewards.giveRewards(p);
		return null;
	}
	
	@Override
	public boolean isAsync() {
		return rewards.stream().anyMatch(AbstractReward::isAsync);
	}
	
	@Override
	public AbstractReward clone() {
		return new RequirementDependentReward(getCustomDescription(), new RequirementList(requirements),
				new RewardList(rewards));
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return requirements.stream().allMatch(req -> req.test(p)) ?
				rewards
				.stream()
				.map(xreq -> xreq.getDescription(p))
				.filter(Objects::nonNull)
				.collect(Collectors.joining("{JOIN}"))
				: null;
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(rewards.getSizeString());
		loreBuilder.addDescription(requirements.getSizeString());
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		LayoutedGUI.newBuilder()
				.addButton(0,
						LayoutedButton.create(XMaterial.NETHER_STAR, () -> "§b" + requirements.getSizeString(),
								Collections.emptyList(), this::editRequirements))
				.addButton(1,
						LayoutedButton.create(XMaterial.CHEST, () -> "§a" + rewards.getSizeString(),
								Collections.emptyList(), this::editRewards))
				.addButton(4, LayoutedButton.create(ItemUtils.itemDone, __ -> event.reopenGUI()))
				.setName(Lang.INVENTORY_REWARDS_WITH_REQUIREMENTS.toString())
				.setCloseBehavior(StandardCloseBehavior.REOPEN)
				.build()
				.open(event.getPlayer());
	}
	
	private void editRequirements(LayoutedClickEvent event) {
		QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.OTHER, newRequirements -> {
			requirements = new RequirementList(newRequirements);
			event.refreshItemReopen();
		}, requirements).open(event.getPlayer());
	}

	private void editRewards(LayoutedClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.OTHER, newRewards -> {
			rewards = new RewardList(newRewards);
			event.refreshItemReopen();
		}, rewards).open(event.getPlayer());
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("requirements", requirements.serialize());
		section.set("rewards", rewards.serialize());
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		requirements = RequirementList.deserialize(section.getMapList("requirements"));
		rewards = RewardList.deserialize(section.getMapList("rewards"));
	}
	
}
