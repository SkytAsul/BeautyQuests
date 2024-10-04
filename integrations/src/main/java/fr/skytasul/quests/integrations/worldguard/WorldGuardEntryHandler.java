package fr.skytasul.quests.integrations.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;
import fr.skytasul.quests.api.QuestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;

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
		// EDIT: actually no, idk if it has changed over time
		Set<ProtectedRegion> regions = set.getRegions();
		if (current.getExtent() instanceof World) {
			ProtectedRegion global = WorldGuard.getInstance().getPlatform().getRegionContainer()
					.get((World) current.getExtent()).getRegion("__global__");
			if (global != null) {
				regions = new HashSet<>(regions);
				regions.add(global);
			}
		}

		final Set<ProtectedRegion> finalRegions = regions;
		Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(), () -> {
			Bukkit.getPluginManager().callEvent(new WorldGuardEntryEvent(BukkitAdapter.adapt(player), finalRegions));
		}, 1L);
	}

	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
		Player bukkitPlayer = BukkitAdapter.adapt(player);
		if (!QuestsPlugin.getPlugin().getNpcManager().isNPC(bukkitPlayer)) {
			// entered and exited do not have global regions
			if (!from.getExtent().equals(to.getExtent())
					&& (from.getExtent() instanceof World && to.getExtent() instanceof World)) {
				ProtectedRegion fromGlobal = WorldGuard.getInstance().getPlatform().getRegionContainer()
						.get((World) from.getExtent()).getRegion("__global__");
				ProtectedRegion toGlobal = WorldGuard.getInstance().getPlatform().getRegionContainer()
						.get((World) to.getExtent()).getRegion("__global__");
				// a world does not necessarily have a global region
				if (toGlobal != null) {
					entered = new HashSet<>(entered);
					entered.add(toGlobal);
				}
				if (fromGlobal != null) {
					exited = new HashSet<>(exited);
					exited.add(fromGlobal);
				}
			}

			if (!entered.isEmpty() || !exited.isEmpty()) {
				final Set<ProtectedRegion> enteredFinal = entered;
				final Set<ProtectedRegion> exitedFinal = exited;
				Bukkit.getScheduler().runTask(QuestsPlugin.getPlugin(), () -> {
					// We must wait for the end of this tick to fire the entry/exit events
					// otherwise the player might be between worlds or still physically
					// in the "old" regions.
					if (!enteredFinal.isEmpty())
						Bukkit.getPluginManager().callEvent(new WorldGuardEntryEvent(bukkitPlayer, enteredFinal));
					if (!exitedFinal.isEmpty())
						Bukkit.getPluginManager().callEvent(new WorldGuardExitEvent(bukkitPlayer, exitedFinal));
				});
			}
		}
		return true;
	}

}
