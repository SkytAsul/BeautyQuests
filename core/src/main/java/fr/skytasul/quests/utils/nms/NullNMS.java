package fr.skytasul.quests.utils.nms;

import fr.skytasul.quests.utils.ReflectUtils;
import org.bukkit.inventory.meta.ItemMeta;

public class NullNMS extends NMS {

	@Override
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		return meta1.equals(meta2);
	}

	public ReflectUtils getReflect(){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	public Object getIChatBaseComponent(String text){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	public Object getEnumChatFormat(int value){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

}
