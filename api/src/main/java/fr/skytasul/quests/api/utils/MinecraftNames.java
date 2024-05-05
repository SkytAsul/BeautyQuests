package fr.skytasul.quests.api.utils;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.GsonBuilder;
import fr.skytasul.quests.api.QuestsPlugin;
import org.apache.commons.lang.WordUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

public class MinecraftNames {

	public static final String LANG_DOWNLOAD_URL = "https://github.com/InventivetalentDev/minecraft-assets/raw/%version%/assets/minecraft/lang/%language%.json";

	private static Map<String, Object> map;

	private static Map<EntityType, String> cachedEntities = new HashMap<>();
	private static Map<XMaterial, String> cachedMaterials = new HashMap<>();

	public static boolean intialize(@NotNull Path path) {
		try {
			if (!Files.exists(path)) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("File " + path.getFileName() + " not found for translations.");
				return false;
			}

			map = new GsonBuilder().create().fromJson(Files.newBufferedReader(path, StandardCharsets.UTF_8), HashMap.class);
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Loaded vanilla translation file for language: " + map.get("language.name") + ". Sorting values.");
			for (Entry<String, Object> en : map.entrySet()) {
				String key = en.getKey();
				String value = (String) en.getValue();
				if (key.startsWith("entity.minecraft.")) {
					cachedEntities.put(EntityType.fromName(key.substring(17)), value);
				}else if (key.startsWith("block.minecraft.")) {
					cachedMaterials.put(XMaterial.matchXMaterial(key.substring(16)).orElse(null), value);
				}else if (key.startsWith("item.minecraft.")) {
					String item = key.substring(15);
					if (item.startsWith("potion.effect.")) {
						PotionMapping.matchFromTranslationKey(item.substring(14))
								.forEachRemaining(potion -> potion.setNormalName(value));
					}else if (item.startsWith("splash_potion.effect.")) {
						PotionMapping.matchFromTranslationKey(item.substring(21))
								.forEachRemaining(potion -> potion.setSplashName(value));
					}else if (item.startsWith("lingering_potion.effect.")) {
						PotionMapping.matchFromTranslationKey(item.substring(24))
								.forEachRemaining(potion -> potion.setLingeringName(value));
					} else
						cachedMaterials.put(XMaterial.matchXMaterial(item).orElse(null), value);
				}
			}
		}catch (Exception e) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Problem when loading Minecraft Translations.", e);
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
		if (MinecraftVersion.isHigherThan(20, 2)
				&& (type == XMaterial.POTION || type == XMaterial.LINGERING_POTION || type == XMaterial.SPLASH_POTION)) {
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			if (meta.getBasePotionType() != null)
				return PotionMapping.matchFromPotionType(meta.getBasePotionType()).getTranslated(type);
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

}