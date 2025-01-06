package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface Quester extends HasPlaceholders, Audience {

	/**
	 * @return the provider of this quester.
	 */
	public @NotNull QuesterProvider getProvider();

	/**
	 * @return an identifier that uniquely describe this quester for the providerx.
	 */
	public @NotNull String getIdentifier();

	/**
	 * @return a friendly name describing this quester. This name cannot be used to uniquely describe
	 *         the quester.
	 */
	public @NotNull String getFriendlyName();

	/**
	 * @return a detailed name describing this quester. This name should only be used for logging
	 *         purpose.
	 */
	public @NotNull String getDetailedName();

	/**
	 * @return the OfflinePlayer instances associated with this quester.
	 */
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers();

	/**
	 * @return the Player instances associated with this quester, only for online players.
	 */
	public @NotNull Collection<Player> getOnlinePlayers();

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
	 * @return the data for the quest if there is, <code>null</code> otherwise
	 */
	public @Nullable QuesterQuestData getQuestDatasIfPresent(@NotNull Quest quest);

	/**
	 * @param quest quest to get the quester data from
	 * @return the data for the quest, creating it if needed
	 */
	public @NotNull QuesterQuestData getQuestDatas(@NotNull Quest quest);

	/**
	 * Removes the data for a quest.
	 *
	 * @param quest quest to remove data from
	 * @return a future that will complete with the data removed following this operation
	 */
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(@NotNull Quest quest);

	/**
	 * Removes the data for a quest.
	 *
	 * @param id if of the quest to remove data from
	 * @return a future that will complete with the data removed following this operation
	 */
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(int id);

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
	 * Sets a savable data.
	 *
	 * @param <T> type of the savable data
	 * @param data SavableData instance
	 * @param value new value of the data
	 */
	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value);

}
