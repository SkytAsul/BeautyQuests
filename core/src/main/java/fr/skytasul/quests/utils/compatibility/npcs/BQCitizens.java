package fr.skytasul.quests.utils.compatibility.npcs;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
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

public class BQCitizens implements BqInternalNpcFactory {
	
	@Override
	public int getTimeToWaitForNPCs() {
		return 2;
	}
	
	@Override
	public boolean isNPC(Entity entity) {
		return CitizensAPI.getNPCRegistry().isNPC(entity);
	}
	
	@Override
	public BqInternalNpc fetchNPC(int id) {
		NPC npc = CitizensAPI.getNPCRegistry().getById(id);
		return npc == null ? null : new BQCitizensNPC(npc);
	}
	
	@Override
	public Collection<Integer> getIDs() {
		return StreamSupport.stream(CitizensAPI.getNPCRegistry().sorted().spliterator(), false).map(NPC::getId).collect(Collectors.toList());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCRightClick(NPCRightClickEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcClicked(e, e.getNPC().getId(), e.getClicker(),
				e.getClicker().isSneaking() ? NpcClickType.SHIFT_RIGHT : NpcClickType.RIGHT);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcClicked(e, e.getNPC().getId(), e.getClicker(),
				e.getClicker().isSneaking() ? NpcClickType.SHIFT_LEFT : NpcClickType.LEFT);
	}
	
	@EventHandler
	public void onNPCRemove(NPCRemoveEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		npcRemoved(e.getNPC().getId());
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
	public BqInternalNpc create(Location location, EntityType type, String name) {
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(type, name);
		if (!Settings.Setting.DEFAULT_LOOK_CLOSE.asBoolean()) npc.getOrAddTrait(LookClose.class).toggle();
		npc.spawn(location);
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
		public int getId() {
			return npc.getId();
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
		public void setSkin(@Nullable String skin) {
			if (skin == null) {
				if (npc.hasTrait(SkinTrait.class))
					npc.getTraitNullable(SkinTrait.class).clearTexture();
			} else {
				npc.getOrAddTrait(SkinTrait.class).setSkinName(skin);
			}
		}
		
		@Override
		public boolean setNavigationPaused(boolean paused) {
			boolean b = npc.getNavigator().isPaused();
			npc.getNavigator().setPaused(paused);
			return b;
		}
		
	}
	
}
