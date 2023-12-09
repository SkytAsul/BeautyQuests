package fr.skytasul.quests.utils.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import fr.skytasul.quests.utils.ReflectUtils;

public class NullNMS extends NMS {

	@Override
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		return meta1.equals(meta2);
	}
	
	@Override
	public void openBookInHand(Player p) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}
	
	public ReflectUtils getReflect(){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	@Override
	public double entityNameplateHeight(Entity en){
		return en instanceof LivingEntity ? ((LivingEntity) en).getEyeHeight() + 1 : 1;
	}

	public Object getIChatBaseComponent(String text){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	public Object getEnumChatFormat(int value){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

}
