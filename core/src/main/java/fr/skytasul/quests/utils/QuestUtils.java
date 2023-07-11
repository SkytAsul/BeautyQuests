package fr.skytasul.quests.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.MinecraftVersion;

public class QuestUtils {

	public static void runOrSync(Runnable run) {
		if (Bukkit.isPrimaryThread()) {
			run.run();
		} else
			Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}

	public static <T> BiConsumer<T, Throwable> runSyncConsumer(Runnable run) {
		return (__, ___) -> runSync(run);
	}

	public static void runSync(Runnable run) {
		Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}

	public static void runAsync(Runnable run) {
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), run);
	}

	public static void playPluginSound(Player p, String sound, float volume) {
		playPluginSound(p, sound, volume, 1);
	}

	public static void playPluginSound(Player p, String sound, float volume, float pitch) {
		if (!QuestsConfigurationImplementation.getConfiguration().getQuestsConfig().sounds())
			return;
		if ("none".equals(sound))
			return;
		try {
			p.playSound(p.getLocation(), Sound.valueOf(sound), volume, pitch);
		} catch (Exception ex) {
			if (MinecraftVersion.MAJOR > 8)
				p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}

	public static void playPluginSound(Location lc, String sound, float volume) {
		if (!QuestsConfigurationImplementation.getConfiguration().getQuestsConfig().sounds())
			return;
		try {
			lc.getWorld().playSound(lc, Sound.valueOf(sound), volume, 1);
		} catch (Exception ex) {
			if (MinecraftVersion.MAJOR > 8)
				lc.getWorld().playSound(lc, sound, volume, 1);
		}
	}

	public static void spawnFirework(Location lc, FireworkMeta meta) {
		if (!QuestsConfiguration.getConfig().getQuestsConfig().fireworks() || meta == null)
			return;
		runOrSync(() -> {
			Consumer<Firework> fwConsumer = fw -> {
				fw.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
				fw.setFireworkMeta(meta);
			};
			if (MinecraftVersion.MAJOR >= 12) {
				lc.getWorld().spawn(lc, Firework.class, fw -> fwConsumer.accept(fw));
				// much better to use the built-in since 1.12 method to do operations on entity
				// before it is sent to the players, as it will not create flickering
			} else {
				fwConsumer.accept(lc.getWorld().spawn(lc, Firework.class));
			}
		});
	}

	public static void autoRegister(Object object) {
		if (!object.getClass().isAnnotationPresent(AutoRegistered.class))
			throw new IllegalArgumentException("The class " + object.getClass().getName()
					+ " does not have the @AutoRegistered annotation and thus cannot be automatically registed as evenet listener.");

		if (object instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) object, BeautyQuests.getInstance());
		}
	}

	public static void autoUnregister(Object object) {
		if (!object.getClass().isAnnotationPresent(AutoRegistered.class))
			throw new IllegalArgumentException("The class " + object.getClass().getName()
					+ " does not have the @AutoRegistered annotation and thus cannot be automatically registed as evenet listener.");

		if (object instanceof Listener) {
			HandlerList.unregisterAll((Listener) object);
		}
	}

}
