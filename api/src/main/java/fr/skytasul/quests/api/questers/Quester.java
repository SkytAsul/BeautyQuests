package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public interface Quester extends HasPlaceholders, Audience {

	/**
	 * @return the provider of this quester.
	 */
	public @NotNull QuesterProvider getProvider();

	/**
	 * @return the object that holds this quester's data.
	 */
	public @NotNull QuesterData getDataHolder();

	/**
	 * @return an identifier that uniquely describe this quester for the provider.
	 */
	public @NotNull String getIdentifier();

	/**
	 * @return a friendly name describing this quester. This name cannot be used to uniquely describe
	 *         the quester.
	 */
	public @NotNull String getFriendlyName();

	/**
	 * @return a detailed name describing this quester. This name should only be used for logging
	 *         purpose.
	 */
	public @NotNull String getDetailedName();

	/**
	 * @return the OfflinePlayer instances associated with this quester.
	 */
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers();

	/**
	 * @return the Player instances associated with this quester, only for online players.
	 */
	public @NotNull Collection<Player> getOnlinePlayers();

}
