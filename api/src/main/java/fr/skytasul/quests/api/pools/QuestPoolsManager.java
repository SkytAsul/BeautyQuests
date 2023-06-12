package fr.skytasul.quests.api.pools;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.requirements.RequirementList;

public interface QuestPoolsManager {

	public @NotNull QuestPool createPool(@Nullable QuestPool editing, int npcID, @Nullable String hologram, int maxQuests,
			int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates, RequirementList requirements);

	public void removePool(int id);

	public @Nullable QuestPool getPool(int id);

	public @NotNull @UnmodifiableView Collection<QuestPool> getPools();

}
