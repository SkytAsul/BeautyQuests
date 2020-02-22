package fr.skytasul.quests.api.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Dependencies;

public class Mob<Data> implements Cloneable {

	protected final MobFactory<Data> factory;
	protected final Data data;

	public Mob(MobFactory<Data> factory, Data data) {
		this.factory = factory;
		this.data = data;
	}
	
	public String getName() {
		return factory.getName(data);
	}

	public ItemStack createItemStack(int amount) {
		List<String> lore = new ArrayList<>();
		lore.add(Lang.Amount.format(amount));
		lore.addAll(factory.getDescriptiveLore(data));
		lore.add("");
		lore.add(Lang.click.toString());
		return ItemUtils.item(XMaterial.mobItem(factory.getEntityType(data)), getName(), lore.toArray(new String[0]));
	}

	public boolean isNull() {
		return data == null;
	}
	
	public boolean applies(Object data) {
		return this.data.equals(data);
	}
	
	public int hashCode() {
		int hash = 1;
		hash = hash * 27 + factory.hashCode();
		hash = hash * 27 + data.hashCode();
		return hash;
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Mob) {
			Mob<?> mob = (Mob<?>) obj;
			return this.factory.equals(mob.factory) && this.data.equals(mob.data);
		}
		return false;
	}

	public Mob<Data> clone(){
		return new Mob<Data>(factory, data);
	}

	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("factoryName", factory.getID());
		map.put("value", factory.getValue(data));
		
		return map;
	}
	
	@SuppressWarnings ("rawtypes")
	public static Mob<?> deserialize(Map<String, Object> map) {
		String factoryName = (String) map.get("factoryName");
		String value = (String) map.get("value");
		if (factoryName == null) { // TODO remove on 0.19
			if (map.containsKey("bmob")) {
				factoryName = "bukkitEntity";
				value = (String) map.get("bmob");
			}else if (map.containsKey("mmob") && Dependencies.mm) {
				factoryName = "mythicMobs";
				value = (String) map.get("mmob");
			}else if (map.containsKey("npc")) {
				factoryName = "citizensNPC";
				value = Integer.toString((int) map.get("npc"));
			}else {
				throw new IllegalArgumentException("Old mob data has failed to load.");
			}
		}
		
		MobFactory<?> factory = MobFactory.getMobFactory(factoryName);
		Mob<?> mob = new Mob(factory, factory.fromValue(value));
		return mob;
	}
	
}
