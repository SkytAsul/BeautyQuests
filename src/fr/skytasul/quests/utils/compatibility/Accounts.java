package fr.skytasul.quests.utils.compatibility;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skytasul.accounts.Account;
import fr.skytasul.accounts.AccountService;
import fr.skytasul.quests.players.HookedAccount;

public class Accounts {

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
	
}
