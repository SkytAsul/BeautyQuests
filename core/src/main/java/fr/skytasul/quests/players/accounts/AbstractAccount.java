package fr.skytasul.quests.players.accounts;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAccount {
	
	protected AbstractAccount(){}
	
	public abstract @NotNull OfflinePlayer getOfflinePlayer();
	
	public abstract @Nullable Player getPlayer();
	
	public abstract boolean isCurrent();
	
	public abstract @NotNull String getIdentifier();
	
	protected abstract boolean equalsAccount(@NotNull AbstractAccount acc);
	
	@Override
	public abstract int hashCode();
	
	@Override
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != this.getClass())
			return false;
		return equalsAccount((AbstractAccount) object);
	}
	
}
