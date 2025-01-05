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
	 * @return the OfflinePlayer instances associated with this quester.
	 */
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers();

	/**
	 * @return the Player instances associated with this quester, only for online players.
	 */
	public @NotNull Collection<Player> getOnlinePlayers();

	public boolean hasQuestDatas(@NotNull Quest quest);

	public @Nullable QuesterQuestData getQuestDatasIfPresent(@NotNull Quest quest);

	public @NotNull QuesterQuestData getQuestDatas(@NotNull Quest quest);

	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(@NotNull Quest quest);

	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(int id);

	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterQuestData> getQuestsDatas();

	public boolean hasPoolDatas(@NotNull QuestPool pool);

	public @NotNull QuesterPoolData getPoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(@NotNull QuestPool pool);

	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(int id);

	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterPoolData> getPoolDatas();

	public <T> @Nullable T getData(@NotNull SavableData<T> data);

	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value);

	public void resetDatas();

	public @NotNull String getName();

	public @NotNull String getNameAndID();

	public @NotNull String debugName();
}
