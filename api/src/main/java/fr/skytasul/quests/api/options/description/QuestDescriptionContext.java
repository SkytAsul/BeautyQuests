package fr.skytasul.quests.api.options.description;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class QuestDescriptionContext {

	private final QuestDescription descriptionOptions;
	private final Quest quest;
	private final @Nullable Player player;
	private final Quester quester;
	private final PlayerListCategory category;
	private final DescriptionSource source;

	private @Nullable Optional<QuesterQuestData> cachedDatas;

	// TODO integrate Player here because it might be useful for some description providers
	public QuestDescriptionContext(@NotNull QuestDescription descriptionOptions, @NotNull Quest quest,
			@Nullable Player player, @NotNull Quester quester, @NotNull PlayerListCategory category,
			@NotNull DescriptionSource source) {
		this.descriptionOptions = descriptionOptions;
		this.quest = quest;
		this.player = player;
		this.quester = quester;
		this.category = category;
		this.source = source;
	}

	public @NotNull QuestDescription getDescriptionOptions() {
		return descriptionOptions;
	}

	public @NotNull Quest getQuest() {
		return quest;
	}

	public @Nullable Player getPlayer() {
		return player;
	}

	public @NotNull Quester getQuester() {
		return quester;
	}

	public @NotNull PlayerListCategory getCategory() {
		return category;
	}

	public @NotNull DescriptionSource getSource() {
		return source;
	}

	public @NotNull Optional<QuesterQuestData> getQuestDatas() {
		if (cachedDatas == null)
			cachedDatas = quester.getDataHolder().getQuestDataIfPresent(quest);
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
		return Objects.hash(descriptionOptions, quest, quester, category, source);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QuestDescriptionContext))
			return false;

		QuestDescriptionContext context = (QuestDescriptionContext) obj;

		return descriptionOptions.equals(context.descriptionOptions) && quest.equals(context.quest)
				&& quester.equals(context.quester) && category == context.category && source == context.source;
	}

}
