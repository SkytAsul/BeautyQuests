package fr.skytasul.quests.api.pools;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerPoolDatas;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;

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
	CompletableFuture<PlayerPoolDatas> resetPlayer(@NotNull PlayerAccount acc);

	void resetPlayerTimer(@NotNull PlayerAccount acc);

	boolean canGive(@NotNull Player p);

	@NotNull
	CompletableFuture<String> give(@NotNull Player p);

}
