package fr.skytasul.quests.api.pools;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public interface QuestPoolController extends HasPlaceholders {

	int getId();

	@NotNull
	QuestPoolData getPoolData();

	@NotNull
	@UnmodifiableView
	Collection<@NotNull ? extends Quest> getQuests();

	@NotNull
	ItemStack getItemStack();

	@NotNull
	CompletableFuture<Boolean> resetPlayer(@NotNull Quester acc);

	void resetPlayerTimer(@NotNull Quester acc);

	@NotNull
	CanGiveResult canGive(@NotNull Player p);

	@NotNull
	CompletableFuture<String> give(@NotNull Player p);

	/**
	 * Get remaining time before the quester can get new quests from this pool.
	 *
	 * @param quester
	 * @return time in milliseconds or empty optional if the cooldown is over
	 */
	@NotNull
	OptionalLong getRemainingCooldown(@NotNull Quester quester);

	/**
	 * Get all quests that the player has not started nor completed yet.
	 *
	 * @param quester
	 * @return a list of quests
	 */
	Collection<Quest> getQuestsRemaining(@NotNull Quester quester);

	/**
	 * Get all quests that the player is currently doing.
	 * <p>
	 * This list contains quests whether or not they have been started specifically for this pool or
	 * independently.
	 *
	 * @param quester
	 * @return a list of quests
	 */
	Collection<Quest> getQuestsInProgress(@NotNull Quester quester);

	/**
	 * Get all quests that the player has completed.
	 * <p>
	 * This list contains quests whether or not they have been started specifically for this pool or
	 * independently.
	 *
	 * @param quester
	 * @return a list of quests
	 */
	Collection<Quest> getQuestsCompleted(@NotNull Quester quester);

	/**
	 * The result of a {@link QuestPoolController#canGive(Player)} operation.
	 *
	 * @param result <code>true</code> if a quest can be given from this pool
	 * @param reason a potential reason why the pool could not give a quest. If {@link #result} is
	 *        <code>true</code>, then this should be <code>null</code>
	 */
	record CanGiveResult(boolean result, @Nullable String reason) {
	}

}
