package fr.skytasul.quests.players.accounts;

import fr.skytasul.accounts.AccountsProvider;
import fr.skytasul.accounts.events.AccountJoinEvent;
import fr.skytasul.accounts.events.AccountLeaveEvent;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.data.QuesterData;
import fr.skytasul.quests.players.AbstractPlayerQuesterImplementation;
import fr.skytasul.quests.players.PlayerManagerImplementation;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.Optional;

public class PlayerManagerAccountsHookImplementation extends PlayerManagerImplementation {

	private static final @NotNull Key KEY = Key.key("BeautyQuests", "players-accounts-hook");

	public PlayerManagerAccountsHookImplementation(@NotNull QuestsPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Key key() {
		return KEY;
	}

	@Override
	public @NotNull Optional<String> getQuesterName(@NotNull String identifier) {
		return getAccountsProvider().getFromIdentifier(NamespacedKey.fromString(identifier))
				.map(acc -> acc.getOfflinePlayer().getName());
	}

	@Override
	protected @NotNull Optional<String> getIdentifier(@NotNull OfflinePlayer p) {
		if (!p.isOnline()) {
			QuestsPlugin.getPlugin().getLogger()
					.warning("Cannot fetch player identifier of an offline player with AccountsHook");
			return Optional.empty();
		}
		return getAccountsProvider().getCurrentAccount(p.getPlayer()).map(acc -> acc.getIdentifier().toString());
	}

	@Override
	protected AbstractPlayerQuesterImplementation createQuester(@NotNull String identifier,
			@NotNull QuesterData dataHandler) {
		return new PlayerQuesterAccountsHookImplementation(this, dataHandler,
				getAccountsProvider().getFromIdentifier(NamespacedKey.fromString(identifier)).orElseThrow());
	}

	private @NotNull AccountsProvider getAccountsProvider() {
		return Objects.requireNonNull(Bukkit.getServicesManager().load(AccountsProvider.class));
	}

	@Override
	public void onJoin(PlayerJoinEvent e) {
		// super.onJoin(e);
		// explicitely disabled
	}

	@Override
	public void onQuit(PlayerQuitEvent e) {
		// super.onQuit(e);
		// explicitely disabled
	}

	@EventHandler
	public void onAccountUse(AccountJoinEvent e) {
		if (!e.isFromDefaultProvider())
			return;
		super.load(e.getPlayer());
	}

	@EventHandler
	public void onAccountLeave(AccountLeaveEvent e) {
		if (!e.isFromDefaultProvider())
			return;
		super.unload(e.getPlayer());
	}

}
