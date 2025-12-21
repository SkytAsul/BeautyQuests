package fr.skytasul.quests.utils.nms;

import org.bukkit.inventory.meta.ItemMeta;

public class NullNMS extends NMS {

	@Override
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		return meta1.equals(meta2);
	}

}
