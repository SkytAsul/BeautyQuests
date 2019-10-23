package fr.skytasul.quests.players;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.skytasul.accounts.Account;

public class HookedAccount extends AbstractAccount {

	private Account hook;
	
	@Deprecated
	public HookedAccount(Account acc){
		Validate.notNull(acc, "Account is null");
		this.hook = acc;
	}

	public OfflinePlayer getOfflinePlayer() {
		return hook.getOfflinePlayer();
	}

	public Player getPlayer(){
		return hook.getPlayer();
	}
	
	public boolean isCurrent(){
		return hook.isCurrent();
	}
	
	public String getIdentifier() {
		String identifier = hook.getIdentifier();
		if (identifier == null) return null;
		return "Hooked|" + identifier;
	}

	
	protected boolean equalsAccount(AbstractAccount acc) {
		return ((HookedAccount) acc).hook.equals(this.hook);
	}

	
	public int hashCode() {
		return hook.hashCode();
	}

}
