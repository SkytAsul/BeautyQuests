package fr.skytasul.quests.integrations.npcs;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.NpcClickType;
import lol.pyr.znpcsplus.api.NpcApiProvider;
import lol.pyr.znpcsplus.api.event.NpcInteractEvent;
import lol.pyr.znpcsplus.api.interaction.InteractionType;
import lol.pyr.znpcsplus.api.npc.NpcEntry;
import lol.pyr.znpcsplus.util.NpcLocation;

public class BQZNPCsPlus implements BqInternalNpcFactoryCreatable, Listener {

	@Override
	public int getTimeToWaitForNPCs() {
		return 45;
	}

	@Override
	public Collection<String> getIDs() {
		return NpcApiProvider.get().getNpcRegistry().getAllPlayerMadeIds();
	}

	@Override
	public boolean isNPC(Entity entity) {
		return false;
	}

	@Override
	public BqInternalNpc fetchNPC(String internalId) {
		NpcEntry npc = NpcApiProvider.get().getNpcRegistry().getById(internalId);
		return npc == null ? null : new BQZnpcPlus(npc);
	}

	@Override
	public boolean isValidEntityType(EntityType type) {
		return NpcApiProvider.get().getNpcTypeRegistry().getByName(type.name()) != null;
	}

	@Override
	public @NotNull BqInternalNpc create(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
			@Nullable String skin) {
		String id;
		int i = 1;
		while (NpcApiProvider.get().getNpcRegistry().getById(id = name + "-" + i) != null) {
			i++;
		}

		NpcEntry npc = NpcApiProvider.get().getNpcRegistry().create(
				id,
				location.getWorld(),
				NpcApiProvider.get().getNpcTypeRegistry().getByName(type.name()),
				new NpcLocation(location));
		npc.enableEverything();

		return new BQZnpcPlus(npc);
	}

	@EventHandler
	public void onInteract(NpcInteractEvent e) {
		npcClicked(null, e.getEntry().getId(), e.getPlayer(),
				NpcClickType.of(e.getClickType() == InteractionType.LEFT_CLICK, e.getPlayer().isSneaking()));
	}

	public static class BQZnpcPlus implements BqInternalNpc {

		private final NpcEntry npc;

		private BQZnpcPlus(NpcEntry npc) {
			this.npc = npc;
		}

		@Override
		public String getInternalId() {
			return npc.getId();
		}

		@Override
		public String getName() {
			return npc.getNpc().getHologram().lineCount() == 0
					? "ID: " + npc.getId()
					: npc.getNpc().getHologram().getLine(0);
		}

		@Override
		public boolean isSpawned() {
			return npc.getNpc().isEnabled();
		}

		@Override
		public Entity getEntity() {
			return null;
		}

		@Override
		public Location getLocation() {
			return npc.getNpc().getLocation().toBukkitLocation(npc.getNpc().getWorld());
		}

		@Override
		public boolean setNavigationPaused(boolean paused) {
			return true;
		}

	}

}
