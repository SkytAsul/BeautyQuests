package fr.skytasul.quests.questers.data;

import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.utils.DataSavingException;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

public interface QuesterDataManager {

	@NotNull
	CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request);

	/**
	 * Resets the data associated with this quest for all questers, online AND offline. This method is
	 * called AFTER having reset the quest for ONLINE questers.
	 *
	 * @param questId id of the quest to reset
	 * @return a future that completes when everything has been correctly reset
	 */
	CompletableFuture<Void> resetQuestData(int questId);

	void save() throws DataSavingException;

	void unload();

	record QuesterFetchRequest(@NotNull Key providerKey, @NotNull String identifier, boolean createIfMissing) {
	}

	record QuesterFetchResult(@NotNull Type type, @Nullable QuesterData dataHandler) {
		public enum Type {
			SUCCESS_LOADED, SUCCESS_CREATED, FAILED_NOT_FOUND, FAILED;

			public boolean isSuccess() {
				return this == SUCCESS_LOADED || this == SUCCESS_CREATED;
			}
		}
	}

}
