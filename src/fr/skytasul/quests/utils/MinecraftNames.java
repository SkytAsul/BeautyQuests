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
	
	private static Map<String, Object> cachedEntities = new HashMap<>();
	private static Map<String, Object> cachedMaterials = new HashMap<>();
	
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
				if (en.getKey().startsWith("entity.minecraft.")) {
					cachedEntities.put(en.getKey().substring(17), en.getValue());
				}else if (en.getKey().startsWith("block.minecraft.")) {
					cachedMaterials.put(en.getKey().substring(16), en.getValue());
				}else if (en.getKey().startsWith("item.minecraft.")) {
					cachedMaterials.put(en.getKey().substring(15), en.getValue());
				}
			}
		}catch (Throwable e) {
			BeautyQuests.logger.severe("Problem when loading Minecraft Translations.");
			e.printStackTrace();
		}
		return true;
	}
	
	public static Object getRaw(String path) {
		return map.get(path);
	}
	
	public static String getEntityName(EntityType type) {
		if (map == null) return type.getName().toLowerCase().replace("_", " ");
		String name = (String) cachedEntities.get(type.getName().toLowerCase());
		if (name == null) return type.getName().toLowerCase().replace("_", " ");
		return name;
	}
	
	public static String getMaterialName(XMaterial type) {
		if (map == null) return type.name().toLowerCase().replace("_", " ");
		String name = (String) cachedMaterials.get(type.name().toLowerCase());
		if (name == null) return type.name().toLowerCase().replace("_", " ");
		return name;
	}
	
}