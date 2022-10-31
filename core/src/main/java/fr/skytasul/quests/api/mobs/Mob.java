package fr.skytasul.quests.api.mobs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class Mob<D> implements Cloneable {

	protected final MobFactory<D> factory;
	protected final D data;
	protected String customName;

	public Mob(MobFactory<D> factory, D data) {
		Validate.notNull(factory, "Mob factory cannot be null");
		Validate.notNull(data, "Mob data cannot be null");
		this.factory = factory;
		this.data = data;
	}
	
	public MobFactory<D> getFactory() {
		return factory;
	}
	
	public D getData() {
		return data;
	}
	
	public String getName() {
		return customName == null ? factory.getName(data) : customName;
	}
	
	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public XMaterial getMobItem() {
		try {
			return Utils.mobItem(factory.getEntityType(data));
		}catch (Exception ex) {
			BeautyQuests.logger.warning("Unknow entity type for mob " + factory.getName(data), ex);
			return XMaterial.SPONGE;
		}
	}
	
	public boolean applies(Object data) {
		return factory.mobApplies(this.data, data);
	}
	
	public boolean appliesEntity(Entity entity) {
		return factory.bukkitMobApplies(data, entity);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash * 27 + factory.hashCode();
		hash = hash * 27 + data.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Mob) {
			Mob<?> mob = (Mob<?>) obj;
			return this.factory.equals(mob.factory) && this.data.equals(mob.data);
		}
		return false;
	}

	@Override
	public Mob<D> clone() {
		try {
			return (Mob<D>) super.clone();
		}catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("factoryName", factory.getID());
		map.put("value", factory.getValue(data));
		if (customName != null) map.put("name", customName);
		
		return map;
	}
	
	@SuppressWarnings ("rawtypes")
	public static Mob<?> deserialize(Map<String, Object> map) {
		String factoryName = (String) map.get("factoryName");
		String value = (String) map.get("value");
		
		MobFactory<?> factory = MobFactory.getMobFactory(factoryName);
		if (factory == null) throw new IllegalArgumentException("The factory " + factoryName + " is not installed in BeautyQuests.");
		Object object = factory.fromValue(value);
		if (object == null) throw new IllegalArgumentException("Can't find the mob " + value + " for factory " + factoryName);
		Mob<?> mob = new Mob(factory, object);
		if (map.containsKey("name")) mob.setCustomName((String) map.get("name"));
		return mob;
	}
	
}
