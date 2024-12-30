package fr.skytasul.quests.api.players;

import java.util.Collection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;

public interface PlayersManager {

	public abstract void save();

	public void addAccountData(@NotNull SavableData<?> data);

	public @NotNull Collection<@NotNull SavableData<?>> getAccountDatas();

	public @UnknownNullability Quester getAccount(@NotNull Player p);

	public static @UnknownNullability Quester getPlayerAccount(@NotNull Player p) {
		return QuestsPlugin.getPlugin().getPlayersManager().getAccount(p);
	}

}
