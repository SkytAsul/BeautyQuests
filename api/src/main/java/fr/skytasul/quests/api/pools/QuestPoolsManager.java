package fr.skytasul.quests.api.pools;

import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.requirements.AbstractRequirement;

public interface QuestPoolsManager {

	public @NotNull QuestPool createPool(@Nullable QuestPool editing, int npcID, @Nullable String hologram, int maxQuests,
			int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates,
			@NotNull List<AbstractRequirement> requirements);

	public void removePool(int id);

	public @Nullable QuestPool getPool(int id);

	public @NotNull @UnmodifiableView Collection<QuestPool> getPools();

}
