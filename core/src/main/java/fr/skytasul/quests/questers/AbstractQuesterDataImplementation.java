package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractQuesterDataImplementation implements QuesterData {

	protected final Map<Integer, QuesterQuestData> questData = new HashMap<>();
	protected final Map<Integer, QuesterPoolDataImplementation> poolData = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalData = new HashMap<>();

	@Override
	public boolean hasQuestDatas(@NotNull Quest quest) {
		return questData.containsKey(quest.getId());
	}

	@Override
	public @NotNull Optional<QuesterQuestData> getQuestDataIfPresent(@NotNull Quest quest) {
		return Optional.ofNullable(questData.get(quest.getId()));
	}

	@Override
	public @NotNull QuesterQuestData getQuestData(@NotNull Quest quest) {
		QuesterQuestData data = questData.get(quest.getId());
		if (data == null) {
			data = createQuestData(quest);
			questData.put(quest.getId(), data);
		}
		return data;
	}

	protected abstract QuesterQuestData createQuestData(@NotNull Quest quest);

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestData(@NotNull Quest quest) {
		return removeQuestData(quest.getId());
	}

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestData(int id) {
		QuesterQuestData removed = questData.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return removed.remove().thenApply(__ -> removed);
	}


	@Override
	public @UnmodifiableView @NotNull Collection<QuesterQuestData> getQuestsDatas() {
		return questData.values();
	}

	@Override
	public boolean hasPoolDatas(@NotNull QuestPool pool) {
		return poolData.containsKey(pool.getId());
	}

	@Override
	public @NotNull QuesterPoolDataImplementation getPoolDatas(@NotNull QuestPool pool) {
		QuesterPoolDataImplementation datas = poolData.get(pool.getId());
		if (datas == null) {
			datas = createPoolDatas(pool);
			poolData.put(pool.getId(), datas);
		}
		return datas;
	}

	protected abstract QuesterPoolDataImplementation createPoolDatas(@NotNull QuestPool pool);

	@Override
	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(@NotNull QuestPool pool) {
		return removePoolDatas(pool.getId());
	}

	@Override
	public @NotNull CompletableFuture<QuesterPoolData> removePoolDatas(int id) {
		QuesterPoolDataImplementation removed = poolData.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return poolDatasRemoved(removed).thenApply(__ -> removed);
	}

	protected CompletableFuture<Void> poolDatasRemoved(QuesterPoolDataImplementation datas) {
		return CompletableFuture.completedFuture(null);
	}

	protected @Nullable QuesterPoolDataImplementation removePoolDatasSilently(int id) {
		return poolData.remove(id);
	}

	@Override
	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterPoolData> getPoolDatas() {
		return poolData.values();
	}

	@Override
	public <T> @Nullable T getData(@NotNull SavableData<T> data) {
		if (!QuestsAPI.getAPI().getQuesterManager().getSavableData().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		return (T) additionalData.getOrDefault(data, data.getDefaultValue());
	}

	@Override
	public <T> CompletableFuture<T> setData(@NotNull SavableData<T> data, @Nullable T value) {
		if (!QuestsAPI.getAPI().getQuesterManager().getSavableData().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		T old = (T) additionalData.put(data, value);
		return setDataInternal(data, value).thenApply(__ -> old);
	}

	protected abstract <T> CompletableFuture<Void> setDataInternal(@NotNull SavableData<T> data, @Nullable T value);

	@Override
	public CompletableFuture<Void> resetData() {
		var future = CompletableFuture.allOf(
				additionalData.keySet().stream().map(data -> setDataInternal(data, null)).toArray(CompletableFuture[]::new));
		additionalData.clear();
		return future;
	}

}
