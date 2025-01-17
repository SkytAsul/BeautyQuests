package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.questers.QuesterProvider;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.questers.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataHandler;
import fr.skytasul.quests.questers.data.QuesterQuestDataHandler;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractQuesterImplementation implements Quester {

	protected final Map<Integer, QuesterQuestDataImplementation> questDatas = new HashMap<>();
	protected final Map<Integer, QuesterPoolDataImplementation> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();

	private final @NotNull QuesterProvider provider;
	private final @NotNull QuesterDataHandler dataHandler;

	private @Nullable PlaceholderRegistry placeholders;
	private @Nullable Pointers audiencePointers;

	protected AbstractQuesterImplementation(@NotNull QuesterProvider provider, @NotNull QuesterDataHandler dataHandler) {
		this.provider = provider;
		this.dataHandler = dataHandler;
	}

	@Override
	public @NotNull QuesterProvider getProvider() {
		return provider;
	}

	@Override
	public boolean hasQuestDatas(@NotNull Quest quest) {
		return questDatas.containsKey(quest.getId());
	}

	public void loadQuestData(int questId, @NotNull QuesterQuestDataHandler handler) {
		var quester = new QuesterQuestDataImplementation(this, handler, questId);
		questDatas.put(questId, quester);
		handler.load(quester);
	}

	@Override
	public @Nullable QuesterQuestDataImplementation getQuestDatasIfPresent(@NotNull Quest quest) {
		return questDatas.get(quest.getId());
	}

	@Override
	public @NotNull QuesterQuestDataImplementation getQuestDatas(@NotNull Quest quest) {
		QuesterQuestDataImplementation datas = questDatas.get(quest.getId());
		if (datas == null) {
			datas = new QuesterQuestDataImplementation(this, dataHandler.createQuestHandler(quest.getId()), quest.getId());
			questDatas.put(quest.getId(), datas);
		}
		return datas;
	}

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(@NotNull Quest quest) {
		return removeQuestDatas(quest.getId());
	}

	@Override
	public @NotNull CompletableFuture<QuesterQuestData> removeQuestDatas(int id) {
		QuesterQuestDataImplementation removed = questDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return removed.dataHandler.remove().thenApply(__ -> removed);
	}

	// TODO investigate relevance
	private @Nullable QuesterQuestDataImplementation removeQuestDatasSilently(int id) {
		return questDatas.remove(id);
	}

	@Override
	public @UnmodifiableView @NotNull Collection<QuesterQuestDataImplementation> getQuestsDatas() {
		return questDatas.values();
	}

	@Override
	public boolean hasPoolDatas(@NotNull QuestPool pool) {
		return poolDatas.containsKey(pool.getId());
	}

	@Override
	public @NotNull QuesterPoolDataImplementation getPoolDatas(@NotNull QuestPool pool) {
		QuesterPoolDataImplementation datas = poolDatas.get(pool.getId());
		if (datas == null) {
			datas = createPoolDatas(pool);
			poolDatas.put(pool.getId(), datas);
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
		QuesterPoolDataImplementation removed = poolDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return poolDatasRemoved(removed).thenApply(__ -> removed);
	}

	protected CompletableFuture<Void> poolDatasRemoved(QuesterPoolDataImplementation datas) {
		return CompletableFuture.completedFuture(null);
	}

	protected @Nullable QuesterPoolDataImplementation removePoolDatasSilently(int id) {
		return poolDatas.remove(id);
	}

	@Override
	public @UnmodifiableView @NotNull Collection<@NotNull ? extends QuesterPoolData> getPoolDatas() {
		return poolDatas.values();
	}

	@Override
	public <T> @Nullable T getData(@NotNull SavableData<T> data) {
		if (!QuestsAPI.getAPI().getQuesterManager().getSavableData().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		return (T) additionalDatas.getOrDefault(data, data.getDefaultValue());
	}

	@Override
	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value) {
		if (!QuestsAPI.getAPI().getQuesterManager().getSavableData().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		additionalDatas.put(data, value);
		dataHandler.setData(data, value);
	}

	public void resetData() {
		additionalDatas.clear();
		dataHandler.resetData();
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
				.registerIndexed("quester_name", this::getFriendlyName)
				.register("quester_identifier", this::getIdentifier)
				.register("quester_detailed_name", this::getDetailedName);

		// TODO eventually remove: for backward compatibility (2.0)
		placeholders
				.register("player_name", this::getFriendlyName)
				.register("player", this::getDetailedName);
	}

	protected void createdPointers(@NotNull Pointers.Builder builder) {
		builder
				.withDynamic(Identity.NAME, this::getDetailedName)
				.withDynamic(Identity.DISPLAY_NAME, () -> Component.text(getFriendlyName()))
				.build();
	}

	@Override
	public @NotNull Pointers pointers() {
		if (audiencePointers == null) {
			var builder = Pointers.builder();
			createdPointers(builder);
			this.audiencePointers = builder.build();
		}
		return audiencePointers;
	}

	public void save() throws DataSavingException {
		dataHandler.save();
	}

	public void unload() {
		dataHandler.unload();
	}

}
