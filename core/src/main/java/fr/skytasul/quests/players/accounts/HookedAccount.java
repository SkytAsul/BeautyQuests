package fr.skytasul.quests.players.accounts;

import fr.skytasul.accounts.Account;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class HookedAccount extends AbstractAccount {

	private Account hook;
	
	public HookedAccount(Account acc){
		Validate.notNull(acc, "Account is null");
		this.hook = acc;
	}

	@Override
	public OfflinePlayer getOfflinePlayer() {
		return hook.getOfflinePlayer();
	}

	@Override
	public Player getPlayer(){
		return hook.getPlayer();
	}
	
	@Override
	public boolean isCurrent(){
		return hook.isCurrent();
	}
	
	@Override
	public String getIdentifier() {
		String identifier = hook.getIdentifier();
		if (identifier == null) return null;
		return "Hooked|" + identifier;
	}
	
	@Override
	protected boolean equalsAccount(AbstractAccount acc) {
		return ((HookedAccount) acc).hook.equals(this.hook);
	}
	
	@Override
	public int hashCode() {
		return hook.hashCode();
	}

}
