package fr.skytasul.quests.utils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.core.util.FileUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

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
					String item = key.substring(15);
					if (item.startsWith(".potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(15));
						potion.normal = (String) en.getValue();
					}else if (item.startsWith(".splash_potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(22));
						potion.splash = (String) en.getValue();
					}else if (item.startsWith(".lingering_potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(25));
						potion.lingering = (String) en.getValue();
					}else cachedMaterials.put(XMaterial.fromString(item), (String) en.getValue());
				}
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
		String defaultName = type.getName();
		if (defaultName == null) defaultName = type.name();
		if (map == null) return defaultFormat(defaultName);
		String name = cachedEntities.get(type);
		if (name == null) return defaultFormat(defaultName);
		return name;
	}
	
	public static String getMaterialName(ItemStack item) {
		XMaterial type = XMaterial.fromItemStack(item);
		if (type == XMaterial.POTION || type == XMaterial.LINGERING_POTION || type == XMaterial.SPLASH_POTION) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			return defaultFormat(XPotion.matchXPotion(meta.getBasePotionData().getType().getEffectType()).getTranslated(type));
		}
		return getMaterialName(type);
	}

	public static String getMaterialName(XMaterial type) {
		if (map == null) return defaultFormat(type.name());
		String name = cachedMaterials.get(type);
		if (name == null) return defaultFormat(type.name());
		return name;
	}
	
	public static String defaultFormat(String value){
		return value.toLowerCase().replace("_", " ");
	}
	
}