package fr.skytasul.quests.integrations.npcs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.NpcClickType;
import io.github.gonalez.znpcs.ServersNPC;
import io.github.gonalez.znpcs.configuration.ConfigurationConstants;
import io.github.gonalez.znpcs.npc.NPC;
import io.github.gonalez.znpcs.npc.NPCModel;
import io.github.gonalez.znpcs.npc.NPCSkin;
import io.github.gonalez.znpcs.npc.NPCType;
import io.github.gonalez.znpcs.npc.event.NPCInteractEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BQServerNPCs implements BqInternalNpcFactoryCreatable, Listener {

	private Cache<Integer, Boolean> cachedNpcs = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	@Override
	public int getTimeToWaitForNPCs() {
		return 45;
	}

	@Override
	public Collection<String> getIDs() {
		return NPC.all().stream()
				.map(x -> Integer.toString(x.getNpcPojo().getId()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isNPC(Entity entity) {
		Boolean result = cachedNpcs.getIfPresent(entity.getEntityId());
		if (result == null) {
			result = NPC.all().stream().anyMatch(npc1 -> npc1.getEntityID() == entity.getEntityId());
			cachedNpcs.put(entity.getEntityId(), result);
		}
		return result;
	}

	@Override
	public BqInternalNpc fetchNPC(String internalId) {
		NPC npc = NPC.find(Integer.parseInt(internalId));
		return npc == null ? null : new BQServerNPC(npc);
	}

	@Override
	public boolean isValidEntityType(EntityType type) {
		return Arrays.stream(NPCType.values()).map(NPCType::name).anyMatch(name -> name.equals(type.name()));
	}

	@Override
	public @NotNull BqInternalNpc create(Location location, EntityType type, String name, @Nullable String skin) {
		List<Integer> ids = ConfigurationConstants.NPC_LIST.stream().map(NPCModel::getId).collect(Collectors.toList());
		int id = ids.size();
		while (ids.contains(id)) id++;
		NPC npc = ServersNPC.createNPC(id, NPCType.valueOf(type.name()), location, name);
		npc.getNpcPojo().getFunctions().put("look", true);

		if (type == EntityType.PLAYER)
			NPCSkin.forName(skin, (values, exception) -> npc.changeSkin(NPCSkin.forValues(values)));

		return new BQServerNPC(npc);
	}

	@EventHandler
	public void onInteract(NPCInteractEvent e) {
		npcClicked(null, Integer.toString(e.getNpc().getNpcPojo().getId()), e.getPlayer(),
				NpcClickType.of(e.isLeftClick(), e.getPlayer().isSneaking()));
	}

	public static class BQServerNPC implements BqInternalNpc {

		private final NPC npc;

		private BQServerNPC(NPC npc) {
			this.npc = npc;
		}

		public NPC getServerNPC() {
			return npc;
		}

		@Override
		public String getInternalId() {
			return Integer.toString(npc.getNpcPojo().getId());
		}

		@Override
		public String getName() {
			return npc.getNpcPojo().getHologramLines().isEmpty() ? "ID: " + npc.getNpcPojo().getId()
					: npc.getNpcPojo().getHologramLines().get(0);
		}

		@Override
		public boolean isSpawned() {
			return npc.getBukkitEntity() != null;
		}

		@Override
		public Entity getEntity() {
			return (Entity) npc.getBukkitEntity();
		}

		@Override
		public Location getLocation() {
			return npc.getLocation();
		}

		@Override
		public boolean setNavigationPaused(boolean paused) {
			return true;
		}

	}

}
