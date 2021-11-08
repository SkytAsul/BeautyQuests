package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.editors.CancellableEditor;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

public class CitizensFactory implements MobFactory<NPC> {

	@Override
	public String getID() {
		return "citizensNPC";
	}

	@Override
	public ItemStack getFactoryItem() {
		return SelectGUI.selectNPC;
	}

	@Override
	public void itemClick(Player p, Consumer<NPC> run) {
		Lang.SELECT_KILL_NPC.send(p);
		new CancellableEditor(p, () -> run.accept(null)) {
			
			@EventHandler (priority = EventPriority.LOW)
			private void onNPCClick(NPCRightClickEvent e) {
				if (e.getClicker() != p) return;
				e.setCancelled(true);
				leave(e.getClicker());
				run.accept(e.getNPC());
			}
			
			@Override
			public void begin() {
				super.begin();
				Utils.sendMessage(p, Lang.NPC_EDITOR_ENTER.toString());
			}
			
		}.enter();
	}

	@Override
	public NPC fromValue(String value) {
		return CitizensAPI.getNPCRegistry().getById(Integer.parseInt(value));
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
