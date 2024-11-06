package fr.skytasul.quests.players;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerPoolDatas;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerAccountImplementation implements PlayerAccount {

	public static final List<String> FORBIDDEN_DATA_ID = Arrays.asList("identifier", "quests", "pools");

	public final AbstractAccount abstractAcc;
	protected final Map<Integer, PlayerQuestDatasImplementation> questDatas = new HashMap<>();
	protected final Map<Integer, PlayerPoolDatasImplementation> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();
	public final int index;

	private @Nullable PlaceholderRegistry placeholders;

	protected PlayerAccountImplementation(@NotNull AbstractAccount account, int index) {
		this.abstractAcc = account;
		this.index = index;
	}

	@Override
	public boolean isCurrent() {
		return abstractAcc.isCurrent();
	}

	@Override
	public @NotNull OfflinePlayer getOfflinePlayer() {
		return abstractAcc.getOfflinePlayer();
	}

	@Override
	public @Nullable Player getPlayer() {
		return abstractAcc.getPlayer();
	}

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

	protected PlayerQuestDatasImplementation createQuestDatas(@NotNull Quest quest) {
		return new PlayerQuestDatasImplementation(this, quest.getId());
	}

	@Override
	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(@NotNull Quest quest) {
		return removeQuestDatas(quest.getId());
	}

	@Override
	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(int id) {
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

	protected PlayerPoolDatasImplementation createPoolDatas(@NotNull QuestPool pool) {
		return new PlayerPoolDatasImplementation(this, pool.getId());
	}

	@Override
	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(@NotNull QuestPool pool) {
		return removePoolDatas(pool.getId());
	}

	@Override
	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(int id) {
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
	public @UnmodifiableView @NotNull Collection<@NotNull ? extends PlayerPoolDatas> getPoolDatas() {
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
			placeholders = new PlaceholderRegistry()
					.registerIndexed("player", this::getNameAndID)
					.register("player_name", this::getName)
					.register("account_id", index)
					.register("account_identifier", abstractAcc::getIdentifier);
		}
		return placeholders;
	}

	public void unload() {
		// left for override
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != this.getClass())
			return false;
		return abstractAcc.equals(((PlayerAccountImplementation) object).abstractAcc);
	}

	@Override
	public int hashCode() {
		int hash = 1;

		hash = hash * 31 + index;
		hash = hash * 31 + abstractAcc.hashCode();

		return hash;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public @NotNull String getName() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName();
	}

	@Override
	public @NotNull String getNameAndID() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName() + " (# " + index + ")";
	}

	@Override
	public @NotNull String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(@NotNull ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", questDatas.isEmpty() ? null : Utils.serializeList(questDatas.values(), PlayerQuestDatasImplementation::serialize));
		config.set("pools", poolDatas.isEmpty() ? null : Utils.serializeList(poolDatas.values(), PlayerPoolDatasImplementation::serialize));
		additionalDatas.entrySet().forEach(entry -> {
			config.set(entry.getKey().getId(), entry.getValue());
		});
	}

}
