package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.questers.data.QuesterData;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public interface Quester extends HasPlaceholders, Audience {

	/**
	 * Gets whether the quester is active or not.
	 * <p>
	 * A quester is "active" if it has online players AND if those players' actions are to be considered
	 * in relation with the quester (e.g. the party a player is actively in for a Party quester).
	 * <p>
	 * Only one quester should be active at a time for a given player in a {@link QuesterProvider}.
	 *
	 * @return <code>true</code> if this quester is active
	 */
	boolean isActive();

	/**
	 * @return the provider of this quester.
	 */
	@NotNull
	QuesterProvider getProvider();

	/**
	 * @return the object that holds this quester's data.
	 */
	@NotNull
	QuesterData getDataHolder();

	/**
	 * @return an identifier that uniquely describe this quester for the provider.
	 */
	@NotNull
	String getIdentifier();

	/**
	 * @return a friendly name describing this quester. This name cannot be used to uniquely describe
	 *         the quester.
	 */
	@NotNull
	String getFriendlyName();

	/**
	 * @return a detailed name describing this quester. This name should only be used for logging
	 *         purpose.
	 */
	@NotNull
	String getDetailedName();

	/**
	 * @return the OfflinePlayer instances associated with this quester.
	 */
	@NotNull
	Collection<OfflinePlayer> getOfflinePlayers();

	/**
	 * @return the Player instances associated with this quester, only for online players.
	 */
	@NotNull
	Collection<Player> getOnlinePlayers();

}
