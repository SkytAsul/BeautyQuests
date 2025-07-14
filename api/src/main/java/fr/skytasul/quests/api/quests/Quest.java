package fr.skytasul.quests.api.quests;

import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.branches.QuestBranchesManager;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Quest extends OptionSet, Comparable<Quest>, HasPlaceholders {

	int getId();

	boolean isValid();

	void delete(boolean silently, boolean keepDatas);

	@NotNull
	QuestBranchesManager getBranchesManager();

	public void addOption(@NotNull QuestOption<?> option);

	public void removeOption(@NotNull Class<? extends QuestOption<?>> clazz);

	public @NotNull List<QuestDescriptionProvider> getDescriptions();

	public @Nullable String getName();

	public @Nullable String getDescription();

	public @NotNull ItemStack getQuestItem();

	public @Nullable BqNpc getStarterNpc();

	public boolean isScoreboardEnabled();

	public boolean isCancellable();

	public boolean isRepeatable();

	public boolean isHidden(QuestVisibilityLocation location);

	public boolean isHiddenWhenRequirementsNotMet();

	public boolean canBypassLimit();

	public boolean hasStarted(@NotNull Quester quester);

	public boolean hasFinished(@NotNull Quester quester);

	public @NotNull String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source);

	public boolean canStart(@NotNull Player player, boolean sendMessage);

	public boolean cancelQuester(@NotNull Quester quester);

	public @NotNull CompletableFuture<Boolean> resetQuester(@NotNull Quester quester);

	public @NotNull CompletableFuture<Boolean> attemptStart(@NotNull Player player);

	public void doNpcClick(@NotNull Player player);

	public void start(@NotNull Quester quester, boolean silently);

	public void finish(@NotNull Quester quester);

	@NotNull
	QuestQuesterStrategy getQuesterStrategy();

	@Override
	default int compareTo(Quest o) {
		return Integer.compare(getId(), o.getId());
	}

}
