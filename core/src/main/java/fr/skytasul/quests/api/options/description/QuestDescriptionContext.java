package fr.skytasul.quests.api.options.description;

import java.util.ArrayList;
import java.util.List;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;

public class QuestDescriptionContext {
	
	private final QuestDescription descriptionOptions;
	private final Quest quest;
	private final PlayerAccount acc;
	private final Category category;
	private final Source source;
	
	private PlayerQuestDatas cachedDatas;
	
	public QuestDescriptionContext(QuestDescription descriptionOptions, Quest quest, PlayerAccount acc,
			PlayerListGUI.Category category, Source source) {
		this.descriptionOptions = descriptionOptions;
		this.quest = quest;
		this.acc = acc;
		this.category = category;
		this.source = source;
	}
	
	public QuestDescription getDescriptionOptions() {
		return descriptionOptions;
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	public PlayerAccount getPlayerAccount() {
		return acc;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public Source getSource() {
		return source;
	}

	public PlayerQuestDatas getQuestDatas() {
		if (cachedDatas == null) cachedDatas = acc.getQuestDatasIfPresent(quest);
		return cachedDatas;
	}
	
	public List<String> formatDescription() {
		List<String> list = new ArrayList<>();
		
		quest.getDescriptions()
			.stream()
			.sorted(QuestDescriptionProvider.COMPARATOR)
			.forEach(provider -> {
				List<String> description = provider.provideDescription(this);
				if (description == null || description.isEmpty()) return;
				
				if (!list.isEmpty() && provider.prefixDescriptionWithNewLine()) list.add("");
				list.addAll(description);
			});
		
		return list;
	}
	
}
