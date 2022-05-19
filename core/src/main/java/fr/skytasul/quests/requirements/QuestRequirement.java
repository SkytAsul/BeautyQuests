package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
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
		this(-1);
	}
	
	public QuestRequirement(int questId) {
		this.questId = questId;
	}
	
	@Override
	public boolean test(Player p) {
		if (exists()) return cached.hasFinished(PlayersManager.getPlayerAccount(p));
		return true;
	}
	
	@Override
	public void sendReason(Player p){
		if (exists()) Lang.REQUIREMENT_QUEST.send(p, cached.getName());
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDQuest.format(exists() ? cached.getName() : questId);
	}

	private boolean exists(){
		cached = QuestsAPI.getQuests().getQuest(questId);
		return cached != null;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + (exists() ? cached.getName() : Lang.NotSet.toString()), "", Lang.RemoveMid.toString() };
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
			event.updateItemLore(getLore());
			event.reopenGUI();
		}) {
			@Override
			public fr.skytasul.quests.gui.CustomInventory.CloseBehavior onClose(Player p, org.bukkit.inventory.Inventory inv) {
				Utils.runSync(() -> {
					event.getGUI().remove(QuestRequirement.this);
					event.reopenGUI();
				});
				return CloseBehavior.NOTHING;
			}
		}.create(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new QuestRequirement(questId);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("questID", questId);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		questId = section.getInt("questID");
	}
	
}
