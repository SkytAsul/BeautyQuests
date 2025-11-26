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
	 * The result of a {@link QuestPoolController#canGive(Player)} operation.
	 *
	 * @param result <code>true</code> if a quest can be given from this pool
	 * @param reason a potential reason why the pool could not give a quest. If {@link #result} is
	 *        <code>true</code>, then this should be <code>null</code>
	 */
	record CanGiveResult(boolean result, @Nullable String reason) {
	}

}
