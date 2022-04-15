package fr.skytasul.quests.api.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public abstract class QuestObject implements Cloneable {
	
	private final QuestObjectCreator creator;
	
	private Quest quest;
	
	protected QuestObject(QuestObjectsRegistry registry) {
		this.creator = registry.getByClass(getClass());
		if (creator == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a reward via the API.");
	}
	
	public QuestObjectCreator getCreator() {
		return creator;
	}
	
	public void attach(Quest quest) {
		this.quest = quest;
	}
	
	public void detach() {
		this.quest = null;
	}
	
	public Quest getAttachedQuest() {
		return quest;
	}
	
	public String getName() {
		return getCreator().id;
	}
	
	public String debugName() {
		return getClass().getSimpleName() + (quest == null ? ", unknown quest" : (", quest " + quest.getID()));
	}
	
	@Override
	public abstract QuestObject clone();
	
	protected abstract void save(Map<String, Object> datas);
	
	protected abstract void load(Map<String, Object> savedDatas);
	
	public String getDescription(Player p) { // will maybe eventually be abstract (and therefore needs to be implemented)
		return null;
	}
	
	public String[] getLore() {
		return new String[] { Lang.RemoveMid.toString() };
	}
	
	public ItemStack getItemStack() {
		return ItemUtils.lore(getCreator().item.clone(), getLore());
	}
	
	public abstract void itemClick(QuestObjectClickEvent event);
	
	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("id", creator.id);
		
		return map;
	}
	
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
				BeautyQuests.logger.severe("An exception occured while deserializing a quest object (class " + objectMap.get("class") + ").", e);
				BeautyQuests.loadingFailure = true;
			}
		}
		return objects;
	}
	
	public static <T extends QuestObject, C extends QuestObjectCreator<T>> T deserialize(Map<String, Object> map, QuestObjectsRegistry<T, C> registry) {
		QuestObjectCreator<T> creator = null;
		
		String id = (String) map.get("id");
		if (id != null) creator = registry.getByID(id);
		
		if (creator == null && map.containsKey("class")) {
			String className = (String) map.get("class");
			try {
				creator = registry.getByClass(Class.forName(className));
			}catch (ClassNotFoundException e) {}
			
			if (creator == null) {
				BeautyQuests.logger.severe("Cannot find object class " + className);
				return null;
			}
		}
		if (creator == null) {
			BeautyQuests.logger.severe("Cannot find object creator with id: " + id);
			return null;
		}
		T reward = creator.newObjectSupplier.get();
		reward.load(map);
		return reward;
	}
	
}