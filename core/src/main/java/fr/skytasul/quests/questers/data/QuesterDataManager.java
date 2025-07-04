package fr.skytasul.quests.questers.data;

import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.questers.QuesterManager;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public interface QuesterDataManager {

	@NotNull
	CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request);

	/**
	 * Resets the data associated with this quest for all questers, online AND offline. This method is
	 * called AFTER having reset the quest for ONLINE questers.
	 *
	 * @param questId id of the quest to reset
	 * @return a future that completes with the amount of data reset
	 */
	CompletableFuture<Integer> resetQuestData(int questId);

	/**
	 * Resets the data associated with this pool for all questers, online AND offline. This method is
	 * called AFTER having reset the quest for ONLINE questers.
	 *
	 * @param poolId id of the pool to reset
	 * @return a future that completes with the amount of data reset
	 */
	CompletableFuture<Integer> resetPoolData(int poolId);

	/**
	 * Called when everything is setup and data are ready to be loaded.
	 *
	 * @param questerManager
	 * @throws DataLoadingException when something goes wrong
	 */
	void load(@NotNull QuesterManager questerManager) throws DataLoadingException;

	void save() throws DataSavingException;

	void unload();

	/**
	 * Gets an iterator that should iterate over all saved quester data in this data manager.
	 * <p>
	 * This iterator is mainly used to import the questers in another data manager.
	 * <p>
	 * The iterator can be iterated on asynchronously.
	 *
	 * @return an iterator containing all questers in this manager
	 * @throws UnsupportedOperationException
	 * @see #importAll(Iterator)
	 */
	default @NotNull Iterator<? extends QuesterData> getAll() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("QuesterDataManager#getAll");
	}

	/**
	 * Imports all quester datas from an iterator to this data manager.
	 * <p>
	 * <strong>WARNING:</strong> The iterator should be iterated on asynchronously.
	 *
	 * @param iterator an iterator containing questers to import
	 * @return a future completed with the import results
	 * @throws UnsupportedOperationException
	 */
	default @NotNull CompletableFuture<ImportResult> importAll(@NotNull Iterator<? extends QuesterData> iterator)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("QuesterDataManager#importAll");
	}

	/**
	 * A request to get the data associated with a quester.
	 *
	 * @param providerKey key uniquely identifying the provider that "contains" the quester
	 * @param identifier unique identifier of the quester that should be fetched
	 * @param createIfMissing boolean indicating whether a quester should be created if it did not
	 *        already exist
	 * @param shouldCache boolean indicating whether the quester data should be cached or not. If
	 *        <code>true</code>, {@link QuesterData#unload()} <b>will</b> be called whenever the data is
	 *        no longer used. If <code>false</code>, data <b>can</b> be cached but
	 *        {@link QuesterData#unload()} <b>will not</b> be called when the data is no longer used.
	 */
	record QuesterFetchRequest(@NotNull Key providerKey, @NotNull String identifier, boolean createIfMissing,
			boolean shouldCache) {
	}

	record QuesterFetchResult(@NotNull Type type, @Nullable QuesterData dataHandler) {
		public enum Type {
			SUCCESS_LOADED, SUCCESS_CREATED, FAILED_NOT_FOUND, FAILED;

			public boolean isSuccess() {
				return this == SUCCESS_LOADED || this == SUCCESS_CREATED;
			}
		}
	}

	record ImportResult(int questers, int failures) {
	}

}
