package fr.skytasul.quests.players;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractQuesterImplementation implements Quester {

	public static final List<String> FORBIDDEN_DATA_ID = Arrays.asList("identifier", "quests", "pools");

	protected final Map<Integer, PlayerQuestDatasImplementation> questDatas = new HashMap<>();
	protected final Map<Integer, PlayerPoolDatasImplementation> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();

	private @Nullable PlaceholderRegistry placeholders;

	@Override
	public boolean hasQuestDatas(@NotNull Quest quest) {
		return questDatas.containsKey(quest.getId());
	}

	@Override
	public @Nullable PlayerQuestDatasImplementation getQuestDatasIfPresent(@NotNull Quest quest) {
		return questDatas.get(quest.getId());
	}

	@Override
	public @NotNull PlayerQuestDatasImplementation getQuestDatas(@NotNull Quest quest) {
		PlayerQuestDatasImplementation datas = questDatas.get(quest.getId());
		if (datas == null) {
			datas = createQuestDatas(quest);
			questDatas.put(quest.getId(), datas);
		}
		return datas;
	}

	protected abstract PlayerQuestDatasImplementation createQuestDatas(@NotNull Quest quest);

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(@NotNull Quest quest) {
		return removeQuestDatas(quest.getId());
	}

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(int id) {
		PlayerQuestDatasImplementation removed = questDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return questDatasRemoved(removed).thenApply(__ -> removed);
	}

	protected CompletableFuture<Void> questDatasRemoved(PlayerQuestDatasImplementation datas) {
		return CompletableFuture.completedFuture(null);
	}

	protected @Nullable PlayerQuestDatasImplementation removeQuestDatasSilently(int id) {
		return questDatas.remove(id);
	}

	@Override
	public @UnmodifiableView @NotNull Collection<@NotNull PlayerQuestDatasImplementation> getQuestsDatas() {
		return questDatas.values();
	}

	@Override
	public boolean hasPoolDatas(@NotNull QuestPool pool) {
		return poolDatas.containsKey(pool.getId());
	}

	@Override
	public @NotNull PlayerPoolDatasImplementation getPoolDatas(@NotNull QuestPool pool) {
		PlayerPoolDatasImplementation datas = poolDatas.get(pool.getId());
		if (datas == null) {
			datas = createPoolDatas(pool);
			poolDatas.put(pool.getId(), datas);
		}
		return datas;
	}

	protected abstract PlayerPoolDatasImplementation createPoolDatas(@NotNull QuestPool pool);

	@Override
	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(@NotNull QuestPool pool) {
		return removePoolDatas(pool.getId());
	}

	@Override
	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(int id) {
		PlayerPoolDatasImplementation removed = poolDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return poolDatasRemoved(removed).thenApply(__ -> removed);
	}

	protected CompletableFuture<Void> poolDatasRemoved(PlayerPoolDatasImplementation datas) {
		return CompletableFuture.completedFuture(null);
	}

	protected @Nullable PlayerPoolDatasImplementation removePoolDatasSilently(int id) {
		return poolDatas.remove(id);
	}

	@Override
	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterPoolData> getPoolDatas() {
		return poolDatas.values();
	}

	@Override
	public <T> @Nullable T getData(@NotNull SavableData<T> data) {
		if (!BeautyQuests.getInstance().getPlayersManager().getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		return (T) additionalDatas.getOrDefault(data, data.getDefaultValue());
	}

	@Override
	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value) {
		if (!BeautyQuests.getInstance().getPlayersManager().getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		additionalDatas.put(data, value);
	}

	@Override
	public void resetDatas() {
		additionalDatas.clear();
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry();
			createdPlaceholdersRegistry(placeholders);
		}
		return placeholders;
	}

	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		placeholders
				.registerIndexed("player", this::getNameAndID)
				.register("player_name", this::getName);
	}

	public void unload() {
		// left for override
	}

}
