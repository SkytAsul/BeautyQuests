package fr.skytasul.quests.utils.compatibility.npcs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;

import fr.skytasul.quests.QuestsConfiguration.ClickType;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.npcs.BQNPCsManager;

import io.github.znetworkw.znpcservers.ServersNPC;
import io.github.znetworkw.znpcservers.configuration.ConfigurationConstants;
import io.github.znetworkw.znpcservers.npc.NPC;
import io.github.znetworkw.znpcservers.npc.NPCModel;
import io.github.znetworkw.znpcservers.npc.NPCSkin;
import io.github.znetworkw.znpcservers.npc.NPCType;
import io.github.znetworkw.znpcservers.npc.event.NPCInteractEvent;

public class BQServerNPCs extends BQNPCsManager {
	
	@Override
	public int getTimeToWaitForNPCs() {
		return 45;
	}
	
	@Override
	public Collection<Integer> getIDs() {
		return NPC.all().stream().map(x -> x.getNpcPojo().getId()).collect(Collectors.toList());
	}
	
	@Override
	public boolean isNPC(Entity entity) {
		return NPC.all().stream().anyMatch(npc1 -> npc1.getEntityID() == entity.getEntityId());
	}
	
	@Override
	protected BQNPC fetchNPC(int id) {
		NPC npc = NPC.find(id);
		return npc == null ? null : new BQServerNPC(npc);
	}
	
	@Override
	public boolean isValidEntityType(EntityType type) {
		return Arrays.stream(NPCType.values()).map(NPCType::name).anyMatch(name -> name.equals(type.name()));
	}
	
	@Override
	protected BQNPC create(Location location, EntityType type, String name) {
		List<Integer> ids = ConfigurationConstants.NPC_LIST.stream().map(NPCModel::getId).collect(Collectors.toList());
		int id = ids.size();
		while (ids.contains(id)) id++;
		NPC npc = ServersNPC.createNPC(id, NPCType.valueOf(type.name()), location, name);
		npc.getNpcPojo().getFunctions().put("look", true);
		return new BQServerNPC(npc);
	}
	
	@EventHandler
	public void onInteract(NPCInteractEvent e) {
		super.clickEvent(null, e.getNpc().getNpcPojo().getId(), e.getPlayer(), e.isLeftClick() ? ClickType.LEFT : ClickType.RIGHT);
	}
	
	public static class BQServerNPC implements BQNPC {
		
		private final NPC npc;
		
		private BQServerNPC(NPC npc) {
			this.npc = npc;
		}
		
		public NPC getServerNPC() {
			return npc;
		}
		
		@Override
		public int getId() {
			return npc.getNpcPojo().getId();
		}
		
		@Override
		public String getName() {
			return npc.getNpcPojo().getHologramLines().isEmpty() ? npc.getGameProfile().getName() : npc.getNpcPojo().getHologramLines().get(0);
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
		public void setSkin(String skin) {
			NPCSkin.forName(skin, (values, exception) -> npc.changeSkin(NPCSkin.forValues(values)));
		}
		
	}
	
}
