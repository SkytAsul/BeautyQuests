package fr.skytasul.quests.api.serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fr.skytasul.quests.BeautyQuests;

public abstract class SerializableObject {
	
	protected final SerializableCreator creator;

	protected SerializableObject(SerializableRegistry registry) {
		this.creator = registry.getByClass(getClass());
		if (creator == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as an object.");
	}
	
	protected SerializableObject(SerializableCreator creator) {
		this.creator = creator;
		if (creator == null) throw new IllegalArgumentException("Creator cannot be null.");
	}

	public SerializableCreator getCreator() {
		return creator;
	}

	public String getName() {
		return getCreator().id;
	}

	@Override
	public abstract SerializableObject clone();

	protected abstract void save(Map<String, Object> datas);

	protected abstract void load(Map<String, Object> savedDatas);

	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("id", creator.id);
		
		return map;
	}

	public static <T extends SerializableObject, C extends SerializableCreator<T>> T deserialize(Map<String, Object> map, SerializableRegistry<T, C> registry) {
		SerializableCreator<T> creator = null;
		
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

	public static <T extends SerializableObject> List<T> deserializeList(List<Map<?, ?>> objectList, Function<Map<String, Object>, T> deserializeFunction) {
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
	
}