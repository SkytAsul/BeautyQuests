package fr.skytasul.quests.utils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.FileUtils;
import org.bukkit.entity.EntityType;

import com.google.gson.GsonBuilder;

import fr.skytasul.quests.BeautyQuests;

public class MinecraftNames {

	private static Map<String, Object> map;
	
	private static Map<EntityType, String> cachedEntities = new HashMap<>();
	private static Map<XMaterial, String> cachedMaterials = new HashMap<>();
	
	public static boolean intialize(String fileName){
		try {
			if (!fileName.contains(".")) fileName = fileName + ".json";
			File file = new File(BeautyQuests.getInstance().getDataFolder(), fileName);
			if (!file.exists()) {
				BeautyQuests.logger.warning("File " + fileName + " not found for loading translations.");
				return false;
			}
			if (!FileUtils.getFileExtension(file).toLowerCase().equals("json")) {
				BeautyQuests.logger.warning("File " + fileName + " is not a JSON file.");
				return false;
			}
			map = new GsonBuilder().create().fromJson(new FileReader(file), new HashMap<String, Object>().getClass());
			BeautyQuests.logger.info("Loaded vanilla translation file for language: " + map.get("language.name") + ". Sorting values.");
			for (Entry<String, Object> en : map.entrySet()) {
				String key = en.getKey();
				if (key.startsWith("entity.minecraft.")) {
					cachedEntities.put(EntityType.fromName(key.substring(17)), (String) en.getValue());
				}else if (key.startsWith("block.minecraft.")) {
					cachedMaterials.put(XMaterial.fromString(key.substring(16)), (String) en.getValue());
				}else if (key.startsWith("item.minecraft.")) {
					cachedMaterials.put(XMaterial.fromString(key.substring(15)), (String) en.getValue());
				} //TODO test en 1.13
			}
		}catch (Exception e) {
			BeautyQuests.logger.severe("Problem when loading Minecraft Translations.");
			e.printStackTrace();
		}
		return true;
	}
	
	public static Object getRaw(String path) {
		return map.get(path);
	}
	
	public static String getEntityName(EntityType type) {
		if (map == null) return defaultFormat(type.getName());
		String name = cachedEntities.get(type);
		if (name == null) return defaultFormat(type.getName());
		return name;
	}
	
	public static String getMaterialName(XMaterial type) {
		if (map == null) return defaultFormat(type.name());
		String name = cachedMaterials.get(type);
		if (name == null) return defaultFormat(type.name());
		return name;
	}
	
	public static String defaultFormat(String value){
		return value.toLowerCase().replace("_", "");
	}
	
}