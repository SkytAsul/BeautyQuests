package fr.skytasul.quests.requirements;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestRequirement extends AbstractRequirement {

	private int questId;
	private Quest cached;

	public QuestRequirement() {
		this(null, null, -1);
	}

	public QuestRequirement(String customDescription, String customReason, int questId) {
		super(customDescription, customReason);
		this.questId = questId;
	}

	@Override
	public boolean test(Player p) {
		if (exists()) return cached.hasFinished(PlayersManager.getPlayerAccount(p));
		return true;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("quest_name", () -> exists() ? cached.getName() : Integer.toString(questId));
		placeholders.register("quest_id", () -> Integer.toString(questId));
	}

	@Override
	protected String getDefaultReason(Player player) {
		return exists() ? Lang.REQUIREMENT_QUEST.format(cached) : null;
	}

	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDQuest.format(exists() ? cached : this);
	}

	private boolean exists(){
		cached = QuestsAPI.getAPI().getQuestsManager().getQuest(questId);
		return cached != null;
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(exists() ? cached.getName() : null));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createQuestSelection(quest -> {
			this.questId = quest.getId();
			event.reopenGUI();
		}, event::remove, QuestsAPI.getAPI().getQuestsManager().getQuests()).open(event.getPlayer());
	}

	@Override
	public AbstractRequirement clone() {
		return new QuestRequirement(getCustomDescription(), getCustomReason(), questId);
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("questID", questId);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		questId = section.getInt("questID");
	}

}
