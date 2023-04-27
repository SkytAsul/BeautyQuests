package fr.skytasul.quests.api.options.description;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;

public class QuestDescriptionContext {
	
	private final QuestDescription descriptionOptions;
	private final Quest quest;
	private final PlayerAccount acc;
	private final PlayerListCategory category;
	private final DescriptionSource source;
	
	private PlayerQuestDatas cachedDatas;
	
	public QuestDescriptionContext(@NotNull QuestDescription descriptionOptions, @NotNull Quest quest,
			@NotNull PlayerAccount acc, @NotNull PlayerListCategory category, @NotNull DescriptionSource source) {
		this.descriptionOptions = descriptionOptions;
		this.quest = quest;
		this.acc = acc;
		this.category = category;
		this.source = source;
	}
	
	public @NotNull QuestDescription getDescriptionOptions() {
		return descriptionOptions;
	}
	
	public @NotNull Quest getQuest() {
		return quest;
	}
	
	public @NotNull PlayerAccount getPlayerAccount() {
		return acc;
	}
	
	public @NotNull PlayerListCategory getCategory() {
		return category;
	}
	
	public @NotNull DescriptionSource getSource() {
		return source;
	}

	public @Nullable PlayerQuestDatas getQuestDatas() {
		if (cachedDatas == null) cachedDatas = acc.getQuestDatasIfPresent(quest);
		return cachedDatas;
	}
	
	public @NotNull List<@Nullable String> formatDescription() {
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
	
	@Override
	public int hashCode() {
		return Objects.hash(descriptionOptions, quest, acc, category, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QuestDescriptionContext))
			return false;

		QuestDescriptionContext context = (QuestDescriptionContext) obj;

		return descriptionOptions.equals(context.descriptionOptions) && quest.equals(context.quest)
				&& acc.equals(context.acc) && category == context.category && source == context.source;
	}

}
