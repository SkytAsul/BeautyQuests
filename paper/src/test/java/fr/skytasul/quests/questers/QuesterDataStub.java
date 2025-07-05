package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public class QuesterDataStub extends AbstractQuesterDataImplementation {

	private final @NotNull Key provider;
	private final @NotNull String identifier;

	public QuesterDataStub(@NotNull Key provider, @NotNull String identifier) {
		this.provider = provider;
		this.identifier = identifier;
	}

	@Override
	public @NotNull Key provider() {
		return provider;
	}

	@Override
	public @NotNull String identifier() {
		return identifier;
	}

	@Override
	public void save() throws DataSavingException {}

	@Override
	public void unload() {}

	@Override
	public @NotNull CompletableFuture<Void> delete() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	protected @NotNull QuesterQuestData createQuestData(@NotNull Quest quest) {
		return new QuestData(quest.getId());
	}

	@Override
	protected @NotNull QuesterPoolData createPoolData(@NotNull QuestPool pool) {
		return null;
	}

	@Override
	protected <T> CompletableFuture<Void> setDataInternal(@NotNull SavableData<T> data, @Nullable T value) {
		return CompletableFuture.completedFuture(null);
	}

	class QuestData extends AbstractQuesterQuestDataImplementation {

		public QuestData(int questID) {
			super(questID);
		}

		@Override
		public @NotNull CompletableFuture<Void> remove() {
			return CompletableFuture.completedFuture(null);
		}

	}

}
