package fr.skytasul.quests.utils.compatibility.worldguard;

import java.util.Set;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;

public class WorldGuardEntryHandler extends Handler {
	
	public WorldGuardEntryHandler(Session session) {
		super(session);
	}
	
	@Override
	public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
		Bukkit.getPluginManager().callEvent(new WorldGuardEntryEvent(BukkitAdapter.adapt(player), entered));
		return super.onCrossBoundary(player, from, to, toSet, entered, exited, moveType);
	}
	
}
