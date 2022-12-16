package fr.skytasul.quests.utils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import com.google.gson.GsonBuilder;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.nms.NMS;

public class MinecraftNames {
	
	public static final String LANG_DOWNLOAD_URL = "https://github.com/InventivetalentDev/minecraft-assets/raw/%version%/assets/minecraft/lang/%language%.json";
	
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
			
			int lastPoint = file.getName().lastIndexOf('.');
			String extension = lastPoint == -1 ? "" : file.getName().substring(lastPoint + 1);
			
			if (!extension.equalsIgnoreCase("json")) {
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
					cachedMaterials.put(XMaterial.matchXMaterial(key.substring(16)).orElse(null), (String) en.getValue());
				}else if (key.startsWith("item.minecraft.")) {
					String item = key.substring(15);
					if (item.startsWith("potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(14));
						if (potion != null) potion.normal = (String) en.getValue();
					}else if (item.startsWith("splash_potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(21));
						if (potion != null) potion.splash = (String) en.getValue();
					}else if (item.startsWith("lingering_potion.effect.")) {
						XPotion potion = XPotion.matchFromTranslationKey(item.substring(24));
						if (potion != null) potion.lingering = (String) en.getValue();
					}else cachedMaterials.put(XMaterial.matchXMaterial(item).orElse(null), (String) en.getValue());
				}
			}
		}catch (Exception e) {
			BeautyQuests.logger.severe("Problem when loading Minecraft Translations.", e);
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
		XMaterial type = XMaterial.matchXMaterial(item);
		if (NMS.getMCVersion() > 8
				&& (type == XMaterial.POTION || type == XMaterial.LINGERING_POTION || type == XMaterial.SPLASH_POTION)) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			try {
				PotionData basePotion = meta.getBasePotionData();
				XPotion potion = XPotion.matchXPotion(basePotion.getType().getEffectType());
				String string = potion.getTranslated(type);
				if (basePotion.isUpgraded()) {
					string += " II" + potion.strongDuration;
				}else if (potion.baseDuration != null) string += basePotion.isExtended() ? potion.extendedDuration : potion.baseDuration;
				return string;
			}catch (NullPointerException ex) {} // happens with potions with no effect
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
		return WordUtils.capitalize(value.toLowerCase().replace('_', ' '));
	}

}