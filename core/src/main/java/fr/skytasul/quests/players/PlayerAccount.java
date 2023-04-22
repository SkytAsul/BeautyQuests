package fr.skytasul.quests.players;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Utils;

public class PlayerAccount {

	public static final List<String> FORBIDDEN_DATA_ID = Arrays.asList("identifier", "quests", "pools");
	
	public final AbstractAccount abstractAcc;
	protected final Map<Integer, PlayerQuestDatas> questDatas = new HashMap<>();
	protected final Map<Integer, PlayerPoolDatas> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();
	protected final int index;
	
	protected PlayerAccount(@NotNull AbstractAccount account, int index) {
		this.abstractAcc = account;
		this.index = index;
	}
	
	/**
	 * @return if this account is currently used by the player (if true, {@link #getPlayer()} cannot return a null player)
	 */
	public boolean isCurrent() {
		return abstractAcc.isCurrent();
	}
	
	/**
	 * @return the OfflinePlayer instance attached to this account (no matter if the player is online or not, or if the account is the currently used)
	 */
	public @NotNull OfflinePlayer getOfflinePlayer() {
		return abstractAcc.getOfflinePlayer();
	}
	
	/**
	 * @return the Player instance who own this account. If the account is not which in use by the player ({@link #isCurrent()}), this will return null.
	 */
	public @Nullable Player getPlayer() {
		return abstractAcc.getPlayer();
	}
	
	public boolean hasQuestDatas(@NotNull Quest quest) {
		return questDatas.containsKey(quest.getID());
	}
	
	public @Nullable PlayerQuestDatas getQuestDatasIfPresent(@NotNull Quest quest) {
		return questDatas.get(quest.getID());
	}

	public @NotNull PlayerQuestDatas getQuestDatas(@NotNull Quest quest) {
		PlayerQuestDatas datas = questDatas.get(quest.getID());
		if (datas == null) {
			datas = BeautyQuests.getInstance().getPlayersManager().createPlayerQuestDatas(this, quest);
			questDatas.put(quest.getID(), datas);
		}
		return datas;
	}

	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(@NotNull Quest quest) {
		return removeQuestDatas(quest.getID());
	}
	
	public @NotNull CompletableFuture<PlayerQuestDatas> removeQuestDatas(int id) {
		PlayerQuestDatas removed = questDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return BeautyQuests.getInstance().getPlayersManager().playerQuestDataRemoved(removed).thenApply(__ -> removed);
	}
	
	protected @Nullable PlayerQuestDatas removeQuestDatasSilently(int id) {
		return questDatas.remove(id);
	}
	
	public @UnmodifiableView @NotNull Collection<@NotNull PlayerQuestDatas> getQuestsDatas() {
		return questDatas.values();
	}
	
	public boolean hasPoolDatas(@NotNull QuestPool pool) {
		return poolDatas.containsKey(pool.getID());
	}
	
	public @NotNull PlayerPoolDatas getPoolDatas(@NotNull QuestPool pool) {
		PlayerPoolDatas datas = poolDatas.get(pool.getID());
		if (datas == null) {
			datas = BeautyQuests.getInstance().getPlayersManager().createPlayerPoolDatas(this, pool);
			poolDatas.put(pool.getID(), datas);
		}
		return datas;
	}
	
	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(@NotNull QuestPool pool) {
		return removePoolDatas(pool.getID());
	}
	
	public @NotNull CompletableFuture<PlayerPoolDatas> removePoolDatas(int id) {
		PlayerPoolDatas removed = poolDatas.remove(id);
		if (removed == null)
			return CompletableFuture.completedFuture(null);

		return BeautyQuests.getInstance().getPlayersManager().playerPoolDataRemoved(removed).thenApply(__ -> removed);
	}
	
	public @UnmodifiableView @NotNull Collection<@NotNull PlayerPoolDatas> getPoolDatas() {
		return poolDatas.values();
	}
	
	public <T> @Nullable T getData(@NotNull SavableData<T> data) {
		if (!BeautyQuests.getInstance().getPlayersManager().getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		return (T) additionalDatas.getOrDefault(data, data.getDefaultValue());
	}

	public <T> void setData(@NotNull SavableData<T> data, @Nullable T value) {
		if (!BeautyQuests.getInstance().getPlayersManager().getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		additionalDatas.put(data, value);
	}
	
	public void resetDatas() {
		additionalDatas.clear();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != this.getClass())
			return false;
		return abstractAcc.equals(((PlayerAccount) object).abstractAcc);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 31 + index;
		hash = hash * 31 + abstractAcc.hashCode();
		
		return hash;
	}
	
	public @NotNull String getName() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName();
	}
	
	public @NotNull String getNameAndID() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName() + " (# " + index + ")";
	}
	
	public @NotNull String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(@NotNull ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", questDatas.isEmpty() ? null : Utils.serializeList(questDatas.values(), PlayerQuestDatas::serialize));
		config.set("pools", poolDatas.isEmpty() ? null : Utils.serializeList(poolDatas.values(), PlayerPoolDatas::serialize));
		additionalDatas.entrySet().forEach(entry -> {
			config.set(entry.getKey().getId(), entry.getValue());
		});
	}
	
}
