package fr.skytasul.quests.utils.compatibility.npcs;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.skytasul.quests.QuestsConfiguration.ClickType;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.npcs.BQNPCsManager;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;

public class BQCitizens extends BQNPCsManager {
	
	@Override
	public int getTimeToWaitForNPCs() {
		return 2;
	}
	
	@Override
	public boolean isNPC(Entity entity) {
		return CitizensAPI.getNPCRegistry().isNPC(entity);
	}
	
	@Override
	protected BQNPC fetchNPC(int id) {
		NPC npc = CitizensAPI.getNPCRegistry().getById(id);
		return npc == null ? null : new BQCitizensNPC(npc);
	}
	
	@Override
	public Collection<Integer> getIDs() {
		return StreamSupport.stream(CitizensAPI.getNPCRegistry().sorted().spliterator(), false).map(NPC::getId).collect(Collectors.toList());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCRightClick(NPCRightClickEvent e) {
		super.clickEvent(e, e.getNPC().getId(), e.getClicker(), ClickType.RIGHT);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		super.clickEvent(e, e.getNPC().getId(), e.getClicker(), ClickType.LEFT);
	}
	
	@EventHandler
	public void onNPCRemove(NPCRemoveEvent e) {
		if (e.getNPC().getOwningRegistry() != CitizensAPI.getNPCRegistry()) return;
		super.removeEvent(e.getNPC().getId());
	}
	
	@Override
	public boolean isValidEntityType(EntityType type) {
		return true;
	}
	
	@Override
	protected BQNPC create(Location location, EntityType type, String name) {
		NPC npc = CitizensAPI.getNPCRegistry().createNPC(type, name);
		if (!Settings.Setting.DEFAULT_LOOK_CLOSE.asBoolean()) npc.getOrAddTrait(LookClose.class).toggle();
		npc.spawn(location);
		return new BQCitizensNPC(npc);
	}
	
	public static class BQCitizensNPC implements BQNPC {
		
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
		public Entity getEntity() {
			return npc.getEntity();
		}
		
		@Override
		public Location getLocation() {
			return npc.getStoredLocation();
		}
		
		@Override
		public void setSkin(String skin) {
			npc.getOrAddTrait(SkinTrait.class).setSkinName(skin);
		}
		
		@Override
		public boolean setNavigationPaused(boolean paused) {
			boolean b = npc.getNavigator().isPaused();
			npc.getNavigator().setPaused(paused);
			return b;
		}
		
	}
	
}
