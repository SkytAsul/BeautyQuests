package fr.skytasul.quests.test;

import static org.junit.jupiter.api.Assertions.fail;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.events.internal.BeautyQuestsLoadedEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class TestUtils {

	public static <T extends Event> EventWaiter<T> waitForEvent(Class<T> eventClass) {
		return new EventWaiter<>(eventClass);
	}

	public static BeautyQuests loadPlugin() {
		var config = YamlConfiguration.loadConfiguration(new InputStreamReader(
				BeautyQuests.class.getClassLoader().getResourceAsStream("config.yml"), StandardCharsets.UTF_8));
		config.set("sounds", false);
		config.set("fireworks", false);
		config.set("scoreboards", false);

		MockBukkit.getOrCreateMock();
		var loadedWaiter = new EventWaiter<>(BeautyQuestsLoadedEvent.class);
		var plugin = MockBukkit.loadWithConfig(BeautyQuests.class, config, Boolean.TRUE);
		loadedWaiter.assertFired(10);
		return plugin;
	}

	public static <T> Callable<T> doAsyncThings(Callable<T> inner) {
		return () -> {
			MockBukkit.getOrCreateMock().getScheduler().waitAsyncTasksFinished();
			MockBukkit.getOrCreateMock().getScheduler().waitAsyncEventsFinished();
			return inner.call();
		};
	}

	public static <T> T awaitFuture(CompletableFuture<T> future, long millis, String reason) {
		long lastTime = System.currentTimeMillis() + millis;

		try {
			do {
				MockBukkit.getOrCreateMock().getScheduler().waitAsyncTasksFinished();
				MockBukkit.getOrCreateMock().getScheduler().waitAsyncEventsFinished();
				try {
					return future.get(Math.min(lastTime - System.currentTimeMillis(), 20), TimeUnit.MILLISECONDS);
				} catch (TimeoutException __) {
					// ignore, continue the loop
				}
			} while (!future.isDone() && lastTime > System.currentTimeMillis());

			fail(reason);
		} catch (InterruptedException | ExecutionException ex) {
			fail(ex);
		}
		return null; // never reached
	}

}
