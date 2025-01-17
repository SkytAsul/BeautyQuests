package fr.skytasul.quests.questers.data;

import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.questers.AbstractQuesterImplementation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public interface QuesterDataHandler {

	void load(@NotNull AbstractQuesterImplementation quester);

	<T> @NotNull CompletableFuture<Void> setData(@NotNull SavableData<T> data, @Nullable T value);

	@NotNull
	CompletableFuture<Void> resetData();

	@NotNull
	QuesterQuestDataHandler createQuestHandler(int id);

	void save() throws DataSavingException;

	void unload();

}
