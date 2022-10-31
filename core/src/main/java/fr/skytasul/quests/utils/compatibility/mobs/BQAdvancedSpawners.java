package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.function.Consumer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

import gcspawners.AdvancedEntityDeathEvent;

public class BQAdvancedSpawners implements MobFactory<String> {
	
	@Override
	public String getID() {
		return "advanced-spawners";
	}
	
	@Override
	public boolean bukkitMobApplies(String first, Entity entity) {
		return false;
	}
	
	@Override
	public ItemStack getFactoryItem() {
		return ItemUtils.item(XMaterial.SPAWNER, Lang.advancedSpawners.toString());
	}
	
	@Override
	public void itemClick(Player p, Consumer<String> run) {
		Lang.ADVANCED_SPAWNERS_MOB.send(p);
		new TextEditor<>(p, () -> run.accept(null), run).enter();
	}
	
	@Override
	public String fromValue(String value) {
		return value;
	}
	
	@Override
	public String getValue(String data) {
		return data;
	}
	
	@Override
	public String getName(String data) {
		return data;
	}
	
	@Override
	public EntityType getEntityType(String data) {
		return EntityType.PIG;
	}
	
	@EventHandler
	public void onMobDeath(AdvancedEntityDeathEvent event) {
		if (!(event.getDamager() instanceof Player)) return;
		callEvent(event, event.getEntityType(), null, (Player) event.getDamager());
	}
	
}
