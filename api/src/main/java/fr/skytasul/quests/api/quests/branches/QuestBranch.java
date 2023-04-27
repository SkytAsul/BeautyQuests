package fr.skytasul.quests.api.quests.branches;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;

public interface QuestBranch {

	int getId();

	@NotNull
	QuestBranchesManager getManager();

	@NotNull
	default Quest getQuest() {
		return getManager().getQuest();
	}

	public @NotNull @UnmodifiableView List<@NotNull StageController> getRegularStages();

	public @NotNull StageController getRegularStage(int id);

	public @NotNull @UnmodifiableView List<EndingStage> getEndingStages();

	public @NotNull StageController getEndingStage(int id);

	public @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source);

	public boolean hasStageLaunched(@Nullable PlayerAccount acc, @NotNull StageController stage);

}
