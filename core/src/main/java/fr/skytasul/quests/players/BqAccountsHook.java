package fr.skytasul.quests.players;

import fr.skytasul.accounts.Account;
import fr.skytasul.accounts.AccountService;
import fr.skytasul.accounts.events.AccountLeaveEvent;
import fr.skytasul.accounts.events.AccountUseEvent;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.accounts.HookedAccount;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.UUID;

public class BqAccountsHook implements Listener {

	public static final AccountService service = Bukkit.getServicesManager().getRegistration(AccountService.class).getProvider();
	
	public static HookedAccount getPlayerAccount(Player p){
		return new HookedAccount(service.getAccountForPlayer(p));
	}

	public static HookedAccount getAccountFromIdentifier(String identifier){
		return new HookedAccount(service.getAccountFromIdentifier(identifier));
	}
	
	public static HookedAccount createAccountFromUUID(UUID id){
		Account acc = service.createAccountFromUUID(id);
		if (acc == null) return null;
		return new HookedAccount(acc);
	}
	
	public static String getPlayerCurrentIdentifier(Player p) {
		return service.getAccountForPlayer(p).getIdentifier();
	}

	@EventHandler
	public void onAccountUse(AccountUseEvent e) {
		BeautyQuests.getInstance().getPlayersManager().loadPlayer(e.getPlayer());
	}

	@EventHandler
	public void onAccountLeave(AccountLeaveEvent e) {
		BeautyQuests.getInstance().getPlayersManager().unloadPlayer(e.getPlayer());
	}
	
}
