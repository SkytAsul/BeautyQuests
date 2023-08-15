package fr.skytasul.quests.integrations.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.CancellableEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

public class CitizensFactory implements MobFactory<NPC>, Listener {

	private final ItemStack item = ItemUtils.item(XMaterial.STICK, Lang.selectNPC.toString());

	@Override
	public String getID() {
		return "citizensNPC";
	}

	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<NPC> run) {
		Lang.SELECT_KILL_NPC.send(p);
		// we cannot use the SelectNPC editor as it uses the BQNPCManager
		// and if it is registered to another NPC plugin it wouldn't work
		new CancellableEditor(p, () -> run.accept(null)) {

			@EventHandler(priority = EventPriority.LOW)
			private void onNPCClick(NPCRightClickEvent e) {
				if (e.getClicker() != player)
					return;
				e.setCancelled(true);
				stop();
				run.accept(e.getNPC());
			}

			@Override
			public void begin() {
				super.begin();
				Lang.NPC_EDITOR_ENTER.send(p);
			}

		}.start();
	}

	@Override
	public NPC fromValue(String value) {
		return CitizensAPI.getNPCRegistry().getById(Integer.parseInt(value));
	}
	
	@Override
	public boolean bukkitMobApplies(NPC first, Entity entity) {
		return first.isSpawned() && first.getEntity().equals(entity);
	}

	@Override
	public String getValue(NPC data) {
		return Integer.toString(data.getId());
	}

	@Override
	public String getName(NPC data) {
		return data.getName();
	}

	@Override
	public EntityType getEntityType(NPC data) {
		return data.getOrAddTrait(MobType.class).getType();
	}

	@Override
	public List<String> getDescriptiveLore(NPC data) {
		return Arrays.asList("§8NPC ID: §7§l" + data.getId());
	}

	@EventHandler
	public void onNPCKilled(NPCDeathEvent e) {
		LivingEntity en = (LivingEntity) e.getNPC().getEntity();
		if (en.getKiller() == null) return;
		callEvent(e, e.getNPC(), en, en.getKiller());
	}

}
