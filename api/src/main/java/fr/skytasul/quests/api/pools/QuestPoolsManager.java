package fr.skytasul.quests.api.pools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;

public interface QuestPoolsManager {

	public @NotNull @UnmodifiableView Collection<? extends QuestPoolController> getPools();

	public @Nullable QuestPoolController getPool(int id);

	public @NotNull QuestPoolController registerPool(@NotNull QuestPoolData pool);

	public @NotNull QuestPoolController editPool(int id, @NotNull QuestPoolData newPool);

	public void removePool(int id);

}
