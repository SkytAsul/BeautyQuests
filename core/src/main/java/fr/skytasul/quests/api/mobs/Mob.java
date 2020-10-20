package fr.skytasul.quests.api.mobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;

public class Mob<Data> implements Cloneable {

	protected final MobFactory<Data> factory;
	protected final Data data;

	public Mob(MobFactory<Data> factory, Data data) {
		Validate.notNull(factory, "Mob factory cannot be null");
		Validate.notNull(data, "Mob data cannot be null");
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
		XMaterial mobItem;
		try {
			mobItem = XMaterial.mobItem(factory.getEntityType(data));
		}catch (Exception ex) {
			mobItem = XMaterial.SPONGE;
			BeautyQuests.logger.warning("Unknow entity type for mob " + factory.getName(data));
			ex.printStackTrace();
		}
		return ItemUtils.item(mobItem, getName(), lore.toArray(new String[0]));
	}
	
	public boolean applies(Object data) {
		return factory.mobApplies(this.data, data);
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
			}else if (map.containsKey("mmob") && DependenciesManager.mm) {
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
