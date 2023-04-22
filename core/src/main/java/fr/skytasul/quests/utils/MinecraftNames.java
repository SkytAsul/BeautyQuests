package fr.skytasul.quests.utils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.google.gson.GsonBuilder;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.nms.NMS;

public class MinecraftNames {
	
	public static final String LANG_DOWNLOAD_URL = "https://github.com/InventivetalentDev/minecraft-assets/raw/%version%/assets/minecraft/lang/%language%.json";
	
	private static Map<String, Object> map;
	
	private static Map<EntityType, String> cachedEntities = new HashMap<>();
	private static Map<XMaterial, String> cachedMaterials = new HashMap<>();
	
	public static boolean intialize(@NotNull String fileName) {
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
						PotionMapping potion = PotionMapping.matchFromTranslationKey(item.substring(14));
						if (potion != null) potion.normal = (String) en.getValue();
					}else if (item.startsWith("splash_potion.effect.")) {
						PotionMapping potion = PotionMapping.matchFromTranslationKey(item.substring(21));
						if (potion != null) potion.splash = (String) en.getValue();
					}else if (item.startsWith("lingering_potion.effect.")) {
						PotionMapping potion = PotionMapping.matchFromTranslationKey(item.substring(24));
						if (potion != null) potion.lingering = (String) en.getValue();
					}else cachedMaterials.put(XMaterial.matchXMaterial(item).orElse(null), (String) en.getValue());
				}
			}
		}catch (Exception e) {
			BeautyQuests.logger.severe("Problem when loading Minecraft Translations.", e);
		}
		return true;
	}
	
	public static @Nullable Object getRaw(@Nullable String path) {
		return map.get(path);
	}
	
	public static @NotNull String getEntityName(@NotNull EntityType type) {
		String defaultName = type.getName();
		if (defaultName == null) defaultName = type.name();
		if (map == null) return defaultFormat(defaultName);
		String name = cachedEntities.get(type);
		if (name == null) return defaultFormat(defaultName);
		return name;
	}
	
	public static @NotNull String getMaterialName(ItemStack item) {
		XMaterial type = XMaterial.matchXMaterial(item);
		if (NMS.getMCVersion() > 8
				&& (type == XMaterial.POTION || type == XMaterial.LINGERING_POTION || type == XMaterial.SPLASH_POTION)) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			try {
				PotionData basePotion = meta.getBasePotionData();
				PotionMapping mapping = basePotion.getType().name().equals("TURTLE_MASTER") ? PotionMapping.TURTLE_MASTER
						: PotionMapping.matchFromXPotion(XPotion.matchXPotion(basePotion.getType().getEffectType()));
				String string = mapping.getTranslated(type);
				if (basePotion.isUpgraded()) {
					string += " II" + mapping.strongDuration;
				} else if (mapping.baseDuration != null)
					string += basePotion.isExtended() ? mapping.extendedDuration : mapping.baseDuration;
				return string;
			}catch (NullPointerException ex) {} // happens with potions with no effect
		}
		return getMaterialName(type);
	}

	public static @NotNull String getMaterialName(@NotNull XMaterial type) {
		if (map == null) return defaultFormat(type.name());
		String name = cachedMaterials.get(type);
		if (name == null) return defaultFormat(type.name());
		return name;
	}
	
	public static @NotNull String defaultFormat(@NotNull String value) {
		return WordUtils.capitalize(value.toLowerCase().replace('_', ' '));
	}
	
	private static class PotionMapping {

		private static final List<PotionMapping> MAPPINGS = new ArrayList<>();
		public static final PotionMapping TURTLE_MASTER;

		static {
			MAPPINGS.add(new PotionMapping(XPotion.FIRE_RESISTANCE, "fire_resistance", 3600, 9600, -1));
			MAPPINGS.add(new PotionMapping(XPotion.HARM, "harming", -1, -1, -1));
			MAPPINGS.add(new PotionMapping(XPotion.HEAL, "healing", -1, -1, -1));
			MAPPINGS.add(new PotionMapping(XPotion.INCREASE_DAMAGE, "strength", 3600, 9600, 1800));
			MAPPINGS.add(new PotionMapping(XPotion.INVISIBILITY, "invisibility", 3600, 9600, -1));
			MAPPINGS.add(new PotionMapping(XPotion.JUMP, "leaping", 3600, 9600, 1800));
			MAPPINGS.add(new PotionMapping(XPotion.LEVITATION, "levitation", -1, -1, -1));
			MAPPINGS.add(new PotionMapping(XPotion.LUCK, "luck", 6000, -1, -1));
			MAPPINGS.add(new PotionMapping(XPotion.NIGHT_VISION, "night_vision", 3600, 9600, -1));
			MAPPINGS.add(new PotionMapping(XPotion.POISON, "poison", 900, 1800, 432));
			MAPPINGS.add(new PotionMapping(XPotion.REGENERATION, "regeneration", 900, 1800, 450));
			MAPPINGS.add(new PotionMapping(XPotion.SLOW, "slowness", 1800, 4800, 400));
			MAPPINGS.add(new PotionMapping(XPotion.SLOW_FALLING, "slow_falling", 1800, 4800, -1));
			MAPPINGS.add(new PotionMapping(XPotion.SPEED, "swiftness", 3600, 9600, 1800));
			MAPPINGS.add(new PotionMapping(XPotion.WATER_BREATHING, "water_breathing", 3600, 9600, -1));
			MAPPINGS.add(new PotionMapping(XPotion.WEAKNESS, "weakness", 1800, 4800, -1));
			MAPPINGS.add(TURTLE_MASTER = new PotionMapping(null, "turtle_master", 400, 800, 400));
		}

		private final @Nullable XPotion mappedPotion;
		private final @NotNull String key;
		private final @Nullable String baseDuration, extendedDuration, strongDuration;
		private @NotNull String normal, splash, lingering;

		private PotionMapping(@Nullable XPotion mappedPotion, @NotNull String key, int baseDuration, int extendedDuration,
				int strongDuration) {
			this.mappedPotion = mappedPotion;
			this.key = key;
			this.baseDuration = baseDuration == 0 ? null : " (" + Utils.ticksToElapsedTime(baseDuration) + ")";
			this.extendedDuration = extendedDuration == 0 ? null : " (" + Utils.ticksToElapsedTime(extendedDuration) + ")";
			this.strongDuration = strongDuration == 0 ? "" : " (" + Utils.ticksToElapsedTime(strongDuration) + ")";
			this.normal = "potion of " + key;
			this.splash = "splash potion of " + key;
			this.lingering = "lingering potion of " + key;
		}

		public @NotNull String getTranslated(XMaterial material) {
			if (material == XMaterial.POTION)
				return normal;
			if (material == XMaterial.SPLASH_POTION)
				return splash;
			if (material == XMaterial.LINGERING_POTION)
				return lingering;
			throw new IllegalArgumentException("Argument is not a potion material");
		}

		public static @Nullable PotionMapping matchFromTranslationKey(String key) {
			for (PotionMapping potion : MAPPINGS) {
				if (key.equals(potion.key))
					return potion;
			}
			return null;
		}
		
		public static @NotNull PotionMapping matchFromXPotion(XPotion xpotion) {
			for (PotionMapping potion : MAPPINGS) {
				if (xpotion.equals(potion.mappedPotion))
					return potion;
			}
			PotionMapping potion = new PotionMapping(xpotion, defaultFormat(xpotion.name()), -1, -1, -1);
			MAPPINGS.add(potion);
			return potion;
		}

	}

}