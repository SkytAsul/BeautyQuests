package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardGiveContext;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CheckpointReward extends AbstractReward {

	private RewardList actions;

	public CheckpointReward() {
		this(null, new RewardList());
	}

	public CheckpointReward(String customDescription, RewardList actions) {
		super(customDescription);
		this.actions = actions;
	}

	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		actions.attachQuest(quest);
	}

	@Override
	public void detach() {
		super.detach();
		actions.detachQuest();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("actions_amount", this::getActionsSizeString);
	}

	private @NotNull String getActionsSizeString() {
		return Lang.actions.quickFormat("amount", actions.size());
	}

	@Override
	public void give(RewardGiveContext context) {
		Lang.QUEST_CHECKPOINT.send(context.getQuester());
	}

	public void apply(@NotNull Player player) {
		// since we want to apply the checkpoint only to a player, we can safely access their account
		// and not care if they is in a party/global quest
		Quester quester = PlayersManager.getPlayerAccount(player);
		actions.giveRewards(quester).whenComplete((result, ex) -> {
			if (ex != null) {
				DefaultErrors.sendGeneric(quester, "giving checkpoint reward");
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while giving checkpoint to {}.", ex,
						quester.getFriendlyName());
			}

			if (result.branchInterruption())
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Useless branching interruption in a checkpoint");
		});
	}

	@Override
	public AbstractReward clone() {
		return new CheckpointReward(getCustomDescription(), new RewardList(actions));
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(getActionsSizeString());
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		QuestsAPI.getAPI().getRewards().createGUI(Lang.INVENTORY_CHECKPOINT_ACTIONS.toString(), QuestObjectLocation.CHECKPOINT, rewards -> {
			actions = new RewardList(rewards);
			event.reopenGUI();
		}, actions, null).open(event.getPlayer());
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("actions", actions.serialize());
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		actions = RewardList.deserialize(section.getMapList("actions"));
	}

}
