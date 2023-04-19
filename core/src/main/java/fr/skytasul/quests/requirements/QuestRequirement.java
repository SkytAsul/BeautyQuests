package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class QuestRequirement extends AbstractRequirement {

	public int questId;
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
	protected String getDefaultReason(Player player) {
		return exists() ? Lang.REQUIREMENT_QUEST.format(cached.getName()) : null;
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDQuest.format(exists() ? cached.getName() : questId);
	}

	private boolean exists(){
		cached = QuestsAPI.getQuests().getQuest(questId);
		return cached != null;
	}
	
	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(QuestOption.formatNullableValue(exists() ? cached.getName() : null));
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		if (QuestsAPI.getQuests().getQuests().isEmpty()) {
			event.getGUI().remove(this);
			event.reopenGUI();
			return;
		}
		
		new ChooseQuestGUI(QuestsAPI.getQuests().getQuests(), quest -> {
			this.questId = quest.getID();
			event.reopenGUI();
		}) {
			@Override
			public fr.skytasul.quests.gui.CustomInventory.CloseBehavior onClose(Player p, org.bukkit.inventory.Inventory inv) {
				Utils.runSync(event::remove);
				return CloseBehavior.NOTHING;
			}
		}.create(event.getPlayer());
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
