package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.utils.Lang;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.MobType;

public class CitizensFactory implements MobFactory<NPC> {

	public String getID() {
		return "citizensNPC";
	}

	public ItemStack getFactoryItem() {
		return SelectGUI.selectNPC;
	}

	public void itemClick(Player p, Consumer<NPC> run) {
		Lang.SELECT_KILL_NPC.send(p);
		new SelectNPC(p, () -> run.accept(null), run).enterOrLeave(p);
	}

	public NPC fromValue(String value) {
		return CitizensAPI.getNPCRegistry().getById(Integer.parseInt(value));
	}

	public String getValue(NPC data) {
		return Integer.toString(data.getId());
	}

	public String getName(NPC data) {
		return data.getName();
	}

	public EntityType getEntityType(NPC data) {
		return data.getTrait(MobType.class).getType();
	}

	public List<String> getDescriptiveLore(NPC data) {
		return Arrays.asList("NPC ID: " + data.getId());
	}

	@EventHandler
	public void onNPCKilled(NPCDeathEvent e) {
		LivingEntity en = (LivingEntity) e.getNPC().getEntity();
		if (en.getKiller() == null) return;
		callEvent(e, e.getNPC(), en, en.getKiller());
	}

}
