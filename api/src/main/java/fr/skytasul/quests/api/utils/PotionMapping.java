package fr.skytasul.quests.api.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// In a separate file because it uses >= 1.20.2 API
class PotionMapping {

	private static final List<PotionMapping> MAPPINGS = new ArrayList<>();

	private static void addMapping(PotionType base, String translationKey, int baseDuration, int longDuration,
			int strongDuration) {
		MAPPINGS.add(new PotionMapping(base, translationKey, baseDuration, false));

		try {
			MAPPINGS.add(
					new PotionMapping(PotionType.valueOf("LONG_" + base.name()), translationKey, longDuration, true));
		} catch (IllegalArgumentException ex) {
		}

		try {
			MAPPINGS.add(new PotionMapping(PotionType.valueOf("STRONG_" + base.name()), translationKey, strongDuration, true));
		} catch (IllegalArgumentException ex) {
		}
	}

	static {
		addMapping(PotionType.FIRE_RESISTANCE, "fire_resistance", 3600, 9600, -1);
		addMapping(PotionType.HARMING, "harming", -1, -1, -1);
		addMapping(PotionType.HEALING, "healing", -1, -1, -1);
		addMapping(PotionType.STRENGTH, "strength", 3600, 9600, 1800);
		addMapping(PotionType.INVISIBILITY, "invisibility", 3600, 9600, -1);
		addMapping(PotionType.LEAPING, "leaping", 3600, 9600, 1800);
		// addMapping(PotionType.LEVITATION, "levitation", -1, -1, -1);
		// does not exist anymore?
		addMapping(PotionType.LUCK, "luck", 6000, -1, -1);
		addMapping(PotionType.NIGHT_VISION, "night_vision", 3600, 9600, -1);
		addMapping(PotionType.POISON, "poison", 900, 1800, 432);
		addMapping(PotionType.REGENERATION, "regeneration", 900, 1800, 450);
		addMapping(PotionType.SLOWNESS, "slowness", 1800, 4800, 400);
		addMapping(PotionType.SLOW_FALLING, "slow_falling", 1800, 4800, -1);
		addMapping(PotionType.SWIFTNESS, "swiftness", 3600, 9600, 1800);
		addMapping(PotionType.WATER_BREATHING, "water_breathing", 3600, 9600, -1);
		addMapping(PotionType.WEAKNESS, "weakness", 1800, 4800, -1);
		addMapping(PotionType.TURTLE_MASTER, "turtle_master", 400, 800, 400);
		// experimental : wind_charged, weaving, oozing, infested
	}

	private final @Nullable PotionType mappedPotion;
	private final @NotNull String key;
	private final @Nullable String duration;
	private final boolean strong;

	private @NotNull String normalName, splashName, lingeringName;

	protected PotionMapping(@NotNull PotionType mappedPotion, @NotNull String key, int duration, boolean strong) {
		this.mappedPotion = mappedPotion;
		this.key = key;
		this.duration = duration == -1 ? null : " (" + Utils.ticksToElapsedTime(duration) + ")";
		this.strong = strong;

		this.normalName = "potion of " + key;
		this.splashName = "splash potion of " + key;
		this.lingeringName = "lingering potion of " + key;
	}

	public @NotNull String getTranslated(XMaterial material) {
		String name;
		if (material == XMaterial.POTION)
			name = normalName;
		else if (material == XMaterial.SPLASH_POTION)
			name = splashName;
		else if (material == XMaterial.LINGERING_POTION)
			name = lingeringName;
		else
			throw new IllegalArgumentException("Argument is not a potion material");

		if (strong)
			name += " II";
		if (duration != null)
			name += duration;
		return name;
	}

	public void setNormalName(@NotNull String normalName) {
		this.normalName = normalName;
	}

	public void setSplashName(@NotNull String splashName) {
		this.splashName = splashName;
	}

	public void setLingeringName(@NotNull String lingeringName) {
		this.lingeringName = lingeringName;
	}

	public static @NotNull Iterator<PotionMapping> matchFromTranslationKey(String key) {
		return MAPPINGS.stream().filter(mapping -> mapping.key.equals(key)).iterator();
		// we return an iterator because there can be multiple mappings (one for "normal", one for
		// "extended", one for "strong"...
	}

	public static @NotNull PotionMapping matchFromPotionType(PotionType potion) {
		for (PotionMapping mapping : MAPPINGS) {
			if (potion.equals(mapping.mappedPotion))
				return mapping;
		}
		addMapping(potion, MinecraftNames.defaultFormat(potion.name()), -1, -1, -1);
		return matchFromPotionType(potion);
	}

}