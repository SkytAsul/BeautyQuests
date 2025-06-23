package fr.skytasul.quests.api.pools;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface QuestPool extends HasPlaceholders {

	int getId();

	@Nullable
	String getNpcId();

	@Nullable
	String getHologram();

	int getMaxQuests();

	int getQuestsPerLaunch();

	boolean isRedoAllowed();

	long getTimeDiff();

	boolean doAvoidDuplicates();

	@NotNull
	RequirementList getRequirements();

	@NotNull
	List<@NotNull Quest> getQuests();

	void addQuest(@NotNull Quest quest);

	void removeQuest(@NotNull Quest quest);

	@NotNull
	ItemStack getItemStack(@NotNull String action);

	@NotNull
	CompletableFuture<Boolean> resetPlayer(@NotNull Quester acc);

	void resetPlayerTimer(@NotNull Quester acc);

	@NotNull
	CanGiveResult canGive(@NotNull Player p);

	@NotNull
	CompletableFuture<String> give(@NotNull Player p);

	/**
	 * The result of a {@link QuestPool#canGive(Player)} operation.
	 *
	 * @param result <code>true</code> if a quest can be given from this pool
	 * @param reason a potential reason why the pool could not give a quest. If {@link #result} is
	 *        <code>true</code>, then this should be <code>null</code>
	 */
	record CanGiveResult(boolean result, @Nullable String reason) {
	}

}
