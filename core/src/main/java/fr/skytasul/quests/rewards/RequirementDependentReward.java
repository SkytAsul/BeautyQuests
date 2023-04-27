package fr.skytasul.quests.rewards;

import java.util.ArrayList;
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
import fr.skytasul.quests.api.gui.layout.Button;
import fr.skytasul.quests.api.gui.layout.ClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.utils.QuestUtils;

public class RequirementDependentReward extends AbstractReward {
	
	private List<AbstractRequirement> requirements;
	private List<AbstractReward> rewards;
	
	public RequirementDependentReward() {
		this(null, new ArrayList<>(), new ArrayList<>());
	}
	
	public RequirementDependentReward(String customDescription, List<AbstractRequirement> requirements,
			List<AbstractReward> rewards) {
		super(customDescription);
		this.requirements = requirements;
		this.rewards = rewards;
	}
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		requirements.forEach(req -> req.attach(quest));
		rewards.forEach(rew -> rew.attach(quest));
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
			return QuestUtils.giveRewards(p, rewards);
		return null;
	}
	
	@Override
	public boolean isAsync() {
		return rewards.stream().anyMatch(AbstractReward::isAsync);
	}
	
	@Override
	public AbstractReward clone() {
		return new RequirementDependentReward(getCustomDescription(), new ArrayList<>(requirements),
				new ArrayList<>(rewards));
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
		loreBuilder.addDescription(Lang.rewards.format(rewards.size()));
		loreBuilder.addDescription(Lang.requirements.format(requirements.size()));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		LayoutedGUI.newBuilder()
				.addButton(0,
						Button.create(XMaterial.NETHER_STAR, () -> "§b" + Lang.requirements.format(requirements.size()),
								Collections.emptyList(), this::editRequirements))
				.addButton(1,
						Button.create(XMaterial.CHEST, () -> "§a" + Lang.rewards.format(rewards.size()),
								Collections.emptyList(), this::editRewards))
				.addButton(4, Button.create(ItemUtils.itemDone, __ -> event.reopenGUI()))
				.setName(Lang.INVENTORY_REWARDS_WITH_REQUIREMENTS.toString())
				.setCloseBehavior(StandardCloseBehavior.REOPEN)
				.build()
				.open(event.getPlayer());
	}
	
	private void editRequirements(ClickEvent event) {
		QuestsAPI.getAPI().getRequirements().createGUI(QuestObjectLocation.OTHER, newRequirements -> {
			RequirementDependentReward.this.requirements = newRequirements;
			event.refreshItemReopen();
		}, requirements).open(event.getPlayer());
	}

	private void editRewards(ClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(QuestObjectLocation.OTHER, newRewards -> {
			RequirementDependentReward.this.rewards = newRewards;
			event.refreshItemReopen();
		}, rewards).open(event.getPlayer());
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("requirements", SerializableObject.serializeList(requirements));
		section.set("rewards", SerializableObject.serializeList(rewards));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		requirements = SerializableObject.deserializeList(section.getMapList("requirements"), AbstractRequirement::deserialize);
		rewards = SerializableObject.deserializeList(section.getMapList("rewards"), AbstractReward::deserialize);
	}
	
}
