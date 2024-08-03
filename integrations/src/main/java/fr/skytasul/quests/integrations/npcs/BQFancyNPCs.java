package fr.skytasul.quests.integrations.npcs;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.NpcClickType;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

public class BQFancyNPCs implements BqInternalNpcFactoryCreatable, Listener {

    @Override
    public int getTimeToWaitForNPCs() {
        return 45;
    }

    @Override
    public Collection<String> getIDs() {
        return FancyNpcsPlugin.get().getNpcManager().getAllNpcs().stream().map(npc -> npc.getData().getName()).collect(Collectors.toList());
    }

    @Override
    public boolean isNPC(Entity entity) {
        return false;
    }

    @Override
    public BqInternalNpc fetchNPC(String internalId) {
        Npc npc = FancyNpcsPlugin.get().getNpcManager().getNpc(internalId);
        return npc == null ? null : new BQFancyNpcs(npc);
    }

    @Override
    public boolean isValidEntityType(EntityType type) {
        return true;
    }

    @Override
    public @NotNull BqInternalNpc create(@NotNull Location location, @NotNull EntityType type, @NotNull String name,
                                         @Nullable String skin) {
        String id;
        int i = 1;
        while (FancyNpcsPlugin.get().getNpcManager().getNpc(id = name + "-" + i) != null) {
            i++;
        }

        NpcData npcData = new NpcData(id, null, location);
        npcData.setType(type);


        Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);

        return new BQFancyNpcs(npc);
    }

    @EventHandler
    public void onInteract(NpcInteractEvent e) {
        npcClicked(null, e.getNpc().getData().getName(), e.getPlayer(),
            NpcClickType.of(e.getInteractionType() == NpcInteractEvent.InteractionType.LEFT_CLICK, e.getPlayer().isSneaking()));
    }

    public static class BQFancyNpcs implements BqInternalNpc {

        private final Npc npc;

        private BQFancyNpcs(Npc npc) {
            this.npc = npc;
        }

        @Override
        public String getInternalId() {
            return npc.getData().getName();
        }

        @Override
        public String getName() {
            return npc.getData().getDisplayName();
        }

        @Override
        public boolean isSpawned() {
            return true;
        }

        @Override
        public Entity getEntity() {
            return null;
        }

        @Override
        public Location getLocation() {
            return npc.getData().getLocation();
        }

        @Override
        public boolean setNavigationPaused(boolean paused) {
            return true;
        }

    }

}