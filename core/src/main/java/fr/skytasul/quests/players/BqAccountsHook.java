package fr.skytasul.quests.players;

import fr.skytasul.accounts.AccountsProvider;
import fr.skytasul.accounts.events.AccountJoinEvent;
import fr.skytasul.accounts.events.AccountLeaveEvent;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.accounts.HookedAccount;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.Optional;

public class BqAccountsHook implements Listener {

	private static @NotNull AccountsProvider getAccountsProvider() {
		return Objects.requireNonNull(Bukkit.getServicesManager().load(AccountsProvider.class));
	}

	public static HookedAccount getPlayerAccount(Player p){
		return new HookedAccount(getAccountsProvider().getCurrentAccount(p));
	}

	public static Optional<HookedAccount> getAccountFromIdentifier(String identifier) {
		return getAccountsProvider().getFromIdentifier(NamespacedKey.fromString(identifier)).map(HookedAccount::new);
	}

	public static String getPlayerCurrentIdentifier(Player p) {
		return getAccountsProvider().getCurrentAccount(p).getIdentifier().toString();
	}

	@EventHandler
	public void onAccountUse(AccountJoinEvent e) {
		if (!e.isFromDefaultProvider())
			return;
		BeautyQuests.getInstance().getPlayersManager().loadPlayer(e.getPlayer());
	}

	@EventHandler
	public void onAccountLeave(AccountLeaveEvent e) {
		if (!e.isFromDefaultProvider())
			return;
		BeautyQuests.getInstance().getPlayersManager().unloadPlayer(e.getPlayer());
	}

}
