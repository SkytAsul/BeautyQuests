package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface QuesterData {

	/**
	 * Checks whether the quester has data for a quest. Usually, a player has data if they:
	 * <ul>
	 * <li>are currently doing the quest
	 * <li>have already done the quest at least once
	 * </ul>
	 *
	 * @param quest quest to check if the quester has data for
	 * @return <code>true</code> if the quester has data for a quest
	 */
	public boolean hasQuestDatas(@NotNull Quest quest);

	/**
	 * @param quest quest to get the quester data from
	 * @return an optional containing the data for the quest if there is according to
	 *         {@link #hasQuestDatas(Quest)}, an empty optional otherwise
	 */
	public @NotNull Optional<QuesterQuestData> getQuestDataIfPresent(@NotNull Quest quest);

	/**
	 * @param quest quest to get the quester data from
	 * @return the data for the quest, creating it if needed
	 */
	public @NotNull QuesterQuestData getQuestData(@NotNull Quest quest);

	/**
	 * Removes the data for a quest.
	 *
	 * @param quest quest to remove data from
	 * @return a future that will complete with the data removed following this operation
	 */
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestData(@NotNull Quest quest);

	/**
	 * Removes the data for a quest.
	 *
	 * @param id if of the quest to remove data from
	 * @return a future that will complete with the data removed following this operation
	 */
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestData(int id);

	/**
	 * @return all data about quests that this quester has
	 */
	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterQuestData> getQuestsDatas();

	public boolean hasPoolDatas(@NotNull QuestPool pool);

	public @NotNull QuesterPoolData getPoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(int id);

	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterPoolData> getPoolDatas();

	/**
	 * @param <T> type of the savable data
	 * @param data SavableData instance
	 * @return the data associated with the SavableData instance that is currently saved for this
	 *         quester
	 */
	public <T> @Nullable T getData(@NotNull SavableData<T> data);

	/**
	 * Saves an additionnal data
	 *
	 * @param <T> type of the savable data
	 * @param data SavableData instance
	 * @param value the new value for the savable data
	 * @return a completable future that will contain the data previously associated with the
	 *         SavableData instance, or <code>null</code> if there is none.
	 */
	<T> @NotNull CompletableFuture<T> setData(@NotNull SavableData<T> data, @Nullable T value);

	@NotNull
	CompletableFuture<Void> resetData();

	void save() throws DataSavingException;

	void unload();

}
