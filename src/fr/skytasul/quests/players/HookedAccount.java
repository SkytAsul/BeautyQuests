package fr.skytasul.quests.players;

import org.apache.commons.lang3.Validate;
import org.bukkit.OfflinePlayer;

import fr.skytasul.accounts.Account;

public class HookedAccount extends AbstractAccount {

	private Account hook;
	
	@Deprecated
	public HookedAccount(Account acc){
		this.hook = Validate.notNull(acc, "Account is null");
	}

	public OfflinePlayer getOfflinePlayer() {
		return hook.getOfflinePlayer();
	}

	public boolean isCurrent(){
		return hook.isCurrent();
	}
	
	public String getIdentifier() {
		return "Hooked|" + hook.getIdentifier();
	}

	
	protected boolean equalsAccount(AbstractAccount acc) {
		return ((HookedAccount) acc).hook.equals(this.hook);
	}

	
	public int hashCode() {
		return hook.hashCode();
	}

}
