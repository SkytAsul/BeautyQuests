package fr.skytasul.quests.api.npcs;

import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.function.Predicate;

public interface BqNpc extends Locatable.Located.LocatedEntity, HasPlaceholders {

	String getId();

	BqInternalNpc getNpc();

	Set<Quest> getQuests();

	boolean hasQuestStarted(Player p);

	Set<? extends QuestPoolController> getPools();

	void hideForPlayer(Player p, Object holder);

	void removeHiddenForPlayer(Player p, Object holder);

	boolean canGiveSomething(Player p);

	void addStartablePredicate(Predicate<Player> predicate, Object holder);

	void removeStartablePredicate(Object holder);

}
