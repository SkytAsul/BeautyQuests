package fr.skytasul.quests.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.nms.NMS;

public class QuestUtils {

	public static void openBook(Player p, ItemStack book) {
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);

		NMS.getNMS().openBookInHand(p);
		p.getInventory().setItem(slot, old);
	}

	private static boolean cachedScoreboardPresent = false;
	private static long cachedScoreboardPresenceExp = 0;

	public static Location upLocationForEntity(LivingEntity en, double value) {
		double height = value;
		height += QuestsConfigurationImplementation.getConfiguration().getHologramsHeight();
		height += NMS.getNMS().entityNameplateHeight(en);
		if (en instanceof Player) {
			if (cachedScoreboardPresenceExp < System.currentTimeMillis()) {
				cachedScoreboardPresenceExp = System.currentTimeMillis() + 60_000;
				cachedScoreboardPresent =
						Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME) != null;
				// as a new Objective object is allocated each time we check this,
				// it is better to cache the boolean for memory consumption.
				// scoreboards are not intended to change frequently, therefore it is
				// not a problem to cache this value for a minute.
			}
			if (cachedScoreboardPresent)
				height += 0.24;
		}
		return en.getLocation().add(0, height, 0);
	}

	public static boolean isSimilar(ItemStack item1, ItemStack item2) {
		if (item2.getType() == item1.getType() && item2.getDurability() == item1.getDurability()) {
			try {
				return NMS.getNMS().equalsWithoutNBT(item1.getItemMeta(), item2.getItemMeta());
			} catch (ReflectiveOperationException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while attempting to compare items using NMS", ex);
			}
		}
		return false;
	}

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
