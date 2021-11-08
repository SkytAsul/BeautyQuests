package fr.skytasul.quests.api.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public interface QuestObject extends Cloneable {
	
	QuestObjectCreator<? extends QuestObject> getCreator();
	
	String getName();
	
	void attach(Quest quest);
	
	void detach();
	
	Quest getAttachedQuest();
	
	QuestObject clone();
	
	default String getDescription(Player p) { // will maybe eventually be abstract (and therefore needs to be implemented)
		return null;
	}
	
	default String[] getLore() {
		return new String[] { Lang.Remove.toString() };
	}
	
	default ItemStack getItemStack() {
		return ItemUtils.lore(getCreator().item.clone(), getLore());
	}
	
	void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked);
	
	public static <T extends QuestObject> List<T> deserializeList(List<Map<?, ?>> objectList, Function<Map<String, Object>, T> deserializeFunction) {
		List<T> objects = new ArrayList<>(objectList.size());
		for (Map<?, ?> objectMap : objectList) {
			try {
				T object = deserializeFunction.apply((Map<String, Object>) objectMap);
				if (object == null) {
					BeautyQuests.loadingFailure = true;
					BeautyQuests.getInstance().getLogger().severe("The quest object for class " + String.valueOf(objectMap.get("class")) + " has not been deserialized.");
				}else objects.add(object);
			}catch (Exception e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a quest object (class " + objectMap.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
				continue;
			}
		}
		return objects;
	}
	
}