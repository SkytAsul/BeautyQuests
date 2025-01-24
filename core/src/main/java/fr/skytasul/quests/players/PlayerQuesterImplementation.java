package fr.skytasul.quests.players;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.questers.QuesterPoolDataImplementation;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerQuesterImplementation extends AbstractPlayerQuesterImplementation implements ForwardingAudience {

	private final @NotNull UUID uuid;

	private OfflinePlayer offPlayer;
	private Player player;

	protected PlayerQuesterImplementation(
			@NotNull PlayersManager playersManager, @NotNull QuesterData dataHandler,
			@NotNull UUID uuid) {
		super(playersManager, dataHandler);
		this.uuid = uuid;
	}

	@Override
	public @NotNull OfflinePlayer getOfflinePlayer() {
		if (offPlayer == null)
			offPlayer = Bukkit.getOfflinePlayer(uuid);
		return offPlayer;
	}

	@Override
	public @NotNull Optional<Player> getPlayer() {
		if (player == null) {
			player = Bukkit.getPlayer(uuid);
			if (player != null)
				offPlayer = player;
		}
		return Optional.ofNullable(player);
	}

	@Override
	public boolean isOnline() {
		return getOfflinePlayer().isOnline();
	}

	// TODO improve memory usage of lists
	@Override
	public @NotNull Collection<Player> getOnlinePlayers() {
		return getPlayer().map(List::of).orElse(List.of());
	}

	@Override
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers() {
		return List.of(getOfflinePlayer());
	}

	@SuppressWarnings("resource")
	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return getPlayer().map(QuestsPlugin.getPlugin().getAudiences()::player).map(List::of).orElse(List.of());
	}

	@Override
	protected QuesterPoolDataImplementation createPoolDatas(@NotNull QuestPool pool) {
		return new QuesterPoolDataImplementation(this, pool.getId());
	}

	@Override
	protected void createdPointers(@NotNull Pointers.Builder builder) {
		super.createdPointers(builder);
		builder.withStatic(Identity.UUID, uuid);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (!(object instanceof PlayerQuesterImplementation quester))
			return false;
		return uuid.equals(quester.uuid);
	}

	@Override
	public int hashCode() {
		return 7 * uuid.hashCode();
	}

	@Override
	public @NotNull String getFriendlyName() {
		String name = getOfflinePlayer().getName();
		if (name == null)
			name = uuid.toString();
		return name;
	}

	@Override
	public @NotNull String getDetailedName() {
		return "player " + getFriendlyName();
	}

	@Override
	public @NotNull String getIdentifier() {
		return uuid.toString();
	}

}
