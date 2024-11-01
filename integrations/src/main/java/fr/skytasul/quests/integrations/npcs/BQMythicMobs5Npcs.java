package fr.skytasul.quests.integrations.npcs;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.NpcClickType;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobInteractEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Optional;

public class BQMythicMobs5Npcs implements BqInternalNpcFactory, Listener {

	@Override
	public int getTimeToWaitForNPCs() {
		return 0; // TODO find accurate value
	}

	@Override
	public boolean isNPC(@NotNull Entity entity) {
		return MythicBukkit.inst().getMobManager().isMythicMob(entity);
	}

	@Override
	public @NotNull Collection<@NotNull String> getIDs() {
		return MythicBukkit.inst().getMobManager().getMobNames();
	}

	@Override
	public @Nullable BqInternalNpc fetchNPC(String id) {
		return MythicBukkit.inst().getMobManager().getMythicMob(id).map(BQMythicMobs5Npc::new).orElse(null);
	}

	@EventHandler
	public void onMythicInteract(MythicMobInteractEvent event) {
		npcClicked(new MythicInteractEventProxy(event), event.getActiveMobType().getInternalName(), event.getPlayer(),
				NpcClickType.RIGHT);
	}

	@EventHandler
	public void onMythicReloaded(MythicReloadedEvent event) {
		npcsReloaded();
	}

	private class BQMythicMobs5Npc implements BqInternalNpc {

		private final @NotNull MythicMob mob;

		public BQMythicMobs5Npc(@NotNull MythicMob mob) {
			this.mob = mob;
		}

		@Override
		public String getInternalId() {
			return mob.getInternalName();
		}

		@Override
		public @NotNull String getName() {
			return mob.getDisplayName() == null ? mob.getInternalName() : mob.getDisplayName().get();
		}

		private @NotNull Optional<ActiveMob> getOneActiveMob() {
			ActiveMob oneActiveMob = null;
			for (ActiveMob activeMob : MythicBukkit.inst().getMobManager().getActiveMobs()) {
				if (!activeMob.isDead() && activeMob.getType().equals(mob)) {
					if (oneActiveMob == null) {
						oneActiveMob = activeMob;
					} else {
						QuestsPlugin.getPlugin().getLogger()
								.warning("Found two active mobs for the same Mythic Mobs NPC " + mob.getInternalName());
						break;
					}
				}
			}
			return Optional.ofNullable(oneActiveMob);
		}

		@Override
		public boolean isSpawned() {
			return getOneActiveMob().isPresent();
		}

		@Override
		public @Nullable Entity getEntity() {
			return getOneActiveMob().map(mob -> mob.getEntity().getBukkitEntity()).orElse(null);
		}

		@Override
		public @NotNull Location getLocation() {
			return getOneActiveMob().map(mob -> BukkitAdapter.adapt(mob.getLocation())).orElse(null);
		}

		@Override
		public boolean setNavigationPaused(boolean paused) {
			return false;
		}

	}

	private static class MythicInteractEventProxy extends Event implements Cancellable {

		private final @NotNull MythicMobInteractEvent event;

		public MythicInteractEventProxy(@NotNull MythicMobInteractEvent event) {
			this.event = event;
		}

		@Override
		public boolean isCancelled() {
			return event.isCancelled();
		}

		@Override
		public void setCancelled(boolean cancel) {
			if (cancel)
				event.setCancelled();
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return null;
		}

	}

}
