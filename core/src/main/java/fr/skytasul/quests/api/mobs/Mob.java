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

	protected MobFactory<Data> factory;
	protected Data data;
	public int amount;

	public Mob(MobFactory<Data> factory, Data data, int amount) {
		this.factory = factory;
		this.data = data;
		this.amount = amount;
	}
	
	public String getName() {
		return factory.getName(data);
	}

	public ItemStack createItemStack() {
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
	
	public Mob<Data> clone(){
		return new Mob<Data>(factory, data, amount);
	}

	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("factoryName", factory.getID());
		map.put("value", factory.getValue(data));
		map.put("amount", amount);
		
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
		Mob<?> mob = new Mob(factory, factory.fromValue(value), (int) map.get("amount"));
		return mob;
	}
	
}
