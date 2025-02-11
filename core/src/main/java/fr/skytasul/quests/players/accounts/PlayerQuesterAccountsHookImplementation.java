package fr.skytasul.quests.players.accounts;

import fr.skytasul.accounts.Account;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.players.AbstractPlayerQuesterImplementation;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PlayerQuesterAccountsHookImplementation extends AbstractPlayerQuesterImplementation
		implements ForwardingAudience {

	public final @NotNull Account acc;

	protected PlayerQuesterAccountsHookImplementation(
			@NotNull PlayersManager playersManager, @NotNull QuesterData dataHandler,
			@NotNull Account account) {
		super(playersManager, dataHandler);
		this.acc = account;
	}

	@Override
	public @NotNull OfflinePlayer getOfflinePlayer() {
		return acc.getOfflinePlayer();
	}

	@Override
	public @NotNull Optional<Player> getPlayer() {
		return acc.getPlayer();
	}

	@Override
	public boolean isOnline() {
		return acc.isCurrent();
	}

	// TODO improve memory usage of lists
	@Override
	public @NotNull Collection<Player> getOnlinePlayers() {
		return acc.getPlayer().map(List::of).orElse(List.of());
	}

	@Override
	public @NotNull Collection<OfflinePlayer> getOfflinePlayers() {
		return List.of(acc.getOfflinePlayer());
	}

	@SuppressWarnings("resource")
	@Override
	public @NotNull Iterable<? extends Audience> audiences() {
		return acc.getPlayer().map(QuestsPlugin.getPlugin().getAudiences()::player).map(List::of).orElse(List.of());
	}

	@Override
	protected void createdPointers(@NotNull Pointers.Builder builder) {
		super.createdPointers(builder);
		builder.withStatic(Identity.UUID, acc.getOfflinePlayer().getUniqueId());
	}

	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != this.getClass())
			return false;
		return acc.equals(((PlayerQuesterAccountsHookImplementation) object).acc);
	}

	@Override
	public int hashCode() {
		return 11 * acc.hashCode();
	}

	@Override
	public @NotNull String getFriendlyName() {
		return acc.getOfflinePlayer().getName();
	}

	@Override
	public @NotNull String getDetailedName() {
		return "player account " + (acc.isCurrent() ? acc.getPlayer().get().getName() : acc.getIdentifier().toString());
	}

	@Override
	public @NotNull String getIdentifier() {
		return acc.getIdentifier().toString();
	}

}
