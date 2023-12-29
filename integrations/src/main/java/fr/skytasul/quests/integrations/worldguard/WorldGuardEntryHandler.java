package fr.skytasul.quests.integrations.worldguard;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;
import fr.skytasul.quests.api.QuestsPlugin;

public class WorldGuardEntryHandler extends Handler {

	public static final class BQFactory extends Handler.Factory<WorldGuardEntryHandler> {
		@Override
		public WorldGuardEntryHandler create(Session session) {
			return new WorldGuardEntryHandler(session);
		}

		public boolean register(SessionManager sessionManager) {
			return sessionManager.registerHandler(this, null);
		}

		public void registerSessions(SessionManager sessionManager) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				BukkitPlayer bukkitPlayer = new BukkitPlayer(WorldGuardPlugin.inst(), player);
				Session session = sessionManager.getIfPresent(bukkitPlayer);
				if (session != null) {
					session.register(create(session));
					session.resetState(bukkitPlayer);
				}
			}
		}

		public void unregister(SessionManager sessionManager) {
			sessionManager.unregisterHandler(this);
		}
	}

	public static final BQFactory FACTORY = new BQFactory();

	public WorldGuardEntryHandler(Session session) {
		super(session);
	}

	@Override
	public void initialize(LocalPlayer player, Location current, ApplicableRegionSet set) {
		super.initialize(player, current, set);
		// no need to test that the set is not empty: there is always the __global__ region
		Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(), () -> {
			Bukkit.getPluginManager().callEvent(new WorldGuardEntryEvent(BukkitAdapter.adapt(player), set.getRegions()));
		}, 1L);
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
		Player bukkitPlayer = BukkitAdapter.adapt(player);
		if (!QuestsPlugin.getPlugin().getNpcManager().isNPC(bukkitPlayer)) {
			if (!entered.isEmpty())
				Bukkit.getPluginManager().callEvent(new WorldGuardEntryEvent(bukkitPlayer, entered));
			if (!exited.isEmpty())
				Bukkit.getPluginManager().callEvent(new WorldGuardExitEvent(bukkitPlayer, exited));
		}
		return super.onCrossBoundary(player, from, to, toSet, entered, exited, moveType);
	}

}
