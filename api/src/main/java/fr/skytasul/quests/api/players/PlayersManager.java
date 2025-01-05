package fr.skytasul.quests.api.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import java.util.Collection;

public interface PlayersManager {

	public abstract void save();

	public void addAccountData(@NotNull SavableData<?> data);

	public @NotNull Collection<@NotNull SavableData<?>> getAccountDatas();

	public @UnknownNullability PlayerQuester getAccount(@NotNull Player p);

	public static @UnknownNullability PlayerQuester getPlayerAccount(@NotNull Player p) {
		return QuestsPlugin.getPlugin().getPlayersManager().getAccount(p);
	}

}
