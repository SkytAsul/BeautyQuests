package fr.skytasul.quests.api.pools;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerPoolDatas;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;

public interface QuestPool extends HasPlaceholders {

	int getId();

	String getNpcId();

	String getHologram();

	int getMaxQuests();

	int getQuestsPerLaunch();

	boolean isRedoAllowed();

	long getTimeDiff();

	boolean doAvoidDuplicates();

	RequirementList getRequirements();

	List<Quest> getQuests();

	void addQuest(Quest quest);

	void removeQuest(Quest quest);

	ItemStack getItemStack(String action);

	CompletableFuture<PlayerPoolDatas> resetPlayer(PlayerAccount acc);

	void resetPlayerTimer(PlayerAccount acc);

	boolean canGive(Player p);

	CompletableFuture<String> give(Player p);

}
