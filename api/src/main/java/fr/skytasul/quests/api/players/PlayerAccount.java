package fr.skytasul.quests.api.players;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;

public interface PlayerAccount extends HasPlaceholders {

	/**
	 * @return if this account is currently used by the player (if true, {@link #getPlayer()} cannot
	 *         return a null player)
	 */
	public boolean isCurrent();

	/**
	 * @return the OfflinePlayer instance attached to this account (no matter if the player is online or
	 *         not, or if the account is the currently used)
	 */
	public @NotNull OfflinePlayer getOfflinePlayer();

	/**
	 * @return the Player instance who own this account. If the account is not which in use by the
	 *         player ({@link #isCurrent()}), this will return null.
	 */
	public @Nullable Player getPlayer();

	public boolean hasQuestDatas(@NotNull Quest quest);

	public @Nullable PlayerQuestDatas getQuestDatasIfPresent(@NotNull Quest quest);

	public @NotNull PlayerQuestDatas getQuestDatas(@NotNull Quest quest);

	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(@NotNull Quest quest);

	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(int id);

	public @UnmodifiableView @NotNull Collection<@NotNull PlayerQuestDatas> getQuestsDatas();

	public boolean hasPoolDatas(@NotNull QuestPool pool);

	public @NotNull PlayerPoolDatas getPoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(int id);

	public @UnmodifiableView @NotNull Collection<@NotNull PlayerPoolDatas> getPoolDatas();

	public <T> @Nullable T getData(@NotNull SavableData<T> data);

	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value);

	public void resetDatas();

	public @NotNull String getName();

	public @NotNull String getNameAndID();

	public @NotNull String debugName();
}
