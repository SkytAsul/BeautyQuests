package fr.skytasul.quests.utils;

import com.cryptomorin.xseries.XSound;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.nms.NMS;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.jetbrains.annotations.NotNull;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class QuestUtils {

	private QuestUtils() {}

	private static boolean cachedScoreboardPresent = false;
	private static long cachedScoreboardPresenceExp = 0;

	public static Location upLocationForEntity(LivingEntity en, double value) {
		double height = value;
		height += QuestsConfigurationImplementation.getConfiguration().getHologramsHeight();
		height += en.getHeight();
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

	public static void tunnelEventCancelling(@NotNull Cancellable eventFrom, @NotNull Event eventTo) {
		Cancellable eventToCancellable = (Cancellable) eventTo; // to force type checking at the beginning

		CompletableFuture<Boolean> cancelled = new CompletableFuture<>();
		QuestUtils.runOrSync(() -> {
			try {
				Bukkit.getPluginManager().callEvent(eventTo);
				cancelled.complete(eventToCancellable.isCancelled());
			} catch (Exception ex) {
				cancelled.completeExceptionally(ex);
			}
		});
		try {
			eventFrom.setCancelled(cancelled.get());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
	}

	private static XSound.Record getSoundRecord(String sound) {
		var xsoundOpt = XSound.of(sound);
		if (xsoundOpt.isPresent())
			return xsoundOpt.get().record();

		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot find sound {0}", sound);
		return new XSound.Record().withSound(sound);
	}

	private static Optional<Key> getSoundKey(String sound) {
		if (Key.parseable(sound))
			return Optional.of(Key.key(sound));

		var xsoundOpt = XSound.of(sound);
		if (xsoundOpt.isPresent() && xsoundOpt.get().isSupported())
			return Optional.of(Key.key(xsoundOpt.get().get().getKey().toString()));

		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot find sound {0}", sound);
		return Optional.empty();
	}

	public static void playPluginSound(Audience audience, String sound, float volume) {
		playPluginSound(audience, sound, volume, 1);
	}

	public static void playPluginSound(Audience audience, String sound, float volume, float pitch) {
		if (!QuestsConfigurationImplementation.getConfiguration().getQuestsConfig().sounds())
			return;
		if ("none".equals(sound))
			return;

		// ugly-ass mix of Adventure and XSeries code to have both Spigot/Paper compatibility
		// and pre/post-registry flattening
		var players = new ArrayList<Player>();
		audience.forEachAudience(aud -> aud.get(Identity.UUID).map(Bukkit::getPlayer).ifPresent(players::add));

		var soundRecord = getSoundRecord(sound).withPitch(pitch).withVolume(volume);
		for (Player p : players) {
			// we cannot directly play it to the collection of players since we want to play it at the
			// location of each of them independently.
			soundRecord.soundPlayer().forPlayers(p).play();
		}
	}

	public static void playPluginSound(Location lc, String sound, float volume) {
		if (!QuestsConfigurationImplementation.getConfiguration().getQuestsConfig().sounds())
			return;
		if ("none".equals(sound))
			return;

		var soundRecord = getSoundRecord(sound).withVolume(volume);
		soundRecord.soundPlayer().atLocation(lc).play();
	}

	public static void spawnFirework(Location lc, FireworkMeta meta) {
		if (!QuestsConfiguration.getConfig().getQuestsConfig().fireworks() || meta == null)
			return;
		runOrSync(() -> {
			Consumer<Firework> fwConsumer = fw -> {
				fw.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
				fw.setFireworkMeta(meta);
			};
			if (MinecraftVersion.isHigherThan(20, 6)) {
				lc.getWorld().spawn(lc, Firework.class, fw -> fwConsumer.accept(fw));
				// Much better to use the built-in method to do operations on entity
				// before it is sent to the players, as it will not create flickering.
				// There was some weird shit done between 1.17 and 1.20 with this method
				// so we will keep it like that
			} else {
				fwConsumer.accept(lc.getWorld().spawn(lc, Firework.class));
			}
		});
	}

	public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
		if (clazz.isAnnotationPresent(annotation))
			return true;

		if (!annotation.isAnnotationPresent(Inherited.class))
			return false;

		for (Class<?> interf : clazz.getInterfaces()) {
			if (hasAnnotation(interf, annotation))
				return true;
		}

		return false;
	}

	public static void autoRegister(Object object) {
		if (!hasAnnotation(object.getClass(), AutoRegistered.class))
			throw new IllegalArgumentException("The class " + object.getClass().getName()
					+ " does not have the @AutoRegistered annotation and thus cannot be automatically registered as an events listener.");

		if (object instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) object, BeautyQuests.getInstance());
		}
	}

	public static void autoUnregister(Object object) {
		if (!hasAnnotation(object.getClass(), AutoRegistered.class))
			throw new IllegalArgumentException("The class " + object.getClass().getName()
					+ " does not have the @AutoRegistered annotation and thus cannot be automatically registered as an events listener.");

		if (object instanceof Listener) {
			HandlerList.unregisterAll((Listener) object);
		}
	}

}
