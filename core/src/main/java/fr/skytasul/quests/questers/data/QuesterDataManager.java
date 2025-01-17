package fr.skytasul.quests.questers.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public interface QuesterDataManager {

	@NotNull
	CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request);

	void save() throws DataSavingException;

	void unload();

	record QuesterFetchRequest(@NotNull Key providerKey, @NotNull String identifier, boolean createIfMissing) {
	}

	record QuesterFetchResult(@NotNull Type type, @Nullable QuesterDataHandler dataHandler) {
		public enum Type {
			SUCCESS_LOADED, SUCCESS_CREATED, FAILED_NOT_FOUND, FAILED;

			public boolean isSuccess() {
				return this == SUCCESS_LOADED || this == SUCCESS_CREATED;
			}
		}
	}

}
