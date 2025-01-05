package fr.skytasul.quests.api.players;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public interface PlayerQuester extends Quester {

	public boolean isOnline();

	public @NotNull Optional<Player> getPlayer();

	public @NotNull OfflinePlayer getOfflinePlayer();

}
