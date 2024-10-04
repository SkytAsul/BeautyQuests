package fr.skytasul.quests.integrations.npcs;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.NpcClickType;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BQCitizens implements BqInternalNpcFactoryCreatable, Listener {

	@Override
	public int getTimeToWaitForNPCs() {
		return 2;
	}

	@Override
	public boolean isNPC(Entity entity) {
		return CitizensAPI.getNPCRegistry().isNPC(entity);
	}

	@Override
	public BqInternalNpc fetchNPC(String internalId) {
		NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.parseInt(internalId));
		return npc == null ? null : new BQCitizensNPC(npc);
	}

	@Override
	public Collection<String> getIDs() {
		return StreamSupport.stream(CitizensAPI.getNPCRegistry().sorted().spliterator(), false)
				.map(npc -> Integer.toString(npc.getId()))
				.collect(Collectors.toList());
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcClicked(e, Integer.toString(e.getNPC().getId()), e.getClicker(),
				e.getClicker().isSneaking() ? NpcClickType.SHIFT_RIGHT : NpcClickType.RIGHT);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcClicked(e, Integer.toString(e.getNPC().getId()), e.getClicker(),
				e.getClicker().isSneaking() ? NpcClickType.SHIFT_LEFT : NpcClickType.LEFT);
	}

	@EventHandler
	public void onNPCRemove(NPCRemoveEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcRemoved(Integer.toString(e.getNPC().getId()));
	}

	@EventHandler
	public void onCitizensReload(CitizensReloadEvent e) {
		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Citizens has been reloaded whereas it is highly not recommended for plugins compatibilities. Unexpected behaviors may happen.");
		npcsReloaded();
	}

	@Override
	public boolean isValidEntityType(EntityType type) {
		return true;
	}

	@Override
	public BqInternalNpc create(Location location, EntityType type, String name, @Nullable String skin) {
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(type, name);
		if (!Settings.Setting.DEFAULT_LOOK_CLOSE.asBoolean()) npc.getOrAddTrait(LookClose.class).toggle();
		npc.spawn(location);

		if (skin == null) {
			if (npc.hasTrait(SkinTrait.class))
				npc.getTraitNullable(SkinTrait.class).clearTexture();
		} else if (type == EntityType.PLAYER) {
			npc.getOrAddTrait(SkinTrait.class).setSkinName(skin);
		}

		return new BQCitizensNPC(npc);
	}

	public static class BQCitizensNPC implements BqInternalNpc {

		private NPC npc;

		private BQCitizensNPC(NPC npc) {
			this.npc = npc;
		}

		public NPC getCitizensNPC() {
			return npc;
		}

		@Override
		public String getInternalId() {
			return Integer.toString(npc.getId());
		}

		@Override
		public String getName() {
			return npc.getName();
		}

		@Override
		public boolean isSpawned() {
			return npc.isSpawned();
		}

		@Override
		public @NotNull Entity getEntity() {
			return npc.getEntity();
		}

		@Override
		public @NotNull Location getLocation() {
			return npc.getStoredLocation();
		}

		@Override
		public boolean setNavigationPaused(boolean paused) {
			boolean b = npc.getNavigator().isPaused();
			npc.getNavigator().setPaused(paused);
			return b;
		}

	}

}
