package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;

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
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(exists() ? cached.getName() : null));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		ChooseQuestGUI.choose(event.getPlayer(), QuestsAPI.getAPI().getQuestsManager().getQuests(), quest -> {
			this.questId = quest.getId();
			event.reopenGUI();
		}, event::remove, false);
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
