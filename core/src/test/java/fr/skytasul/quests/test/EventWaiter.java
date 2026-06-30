package fr.skytasul.quests.test;

import static fr.skytasul.quests.test.TestUtils.awaitFuture;
import static org.junit.jupiter.api.Assertions.fail;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.util.concurrent.CompletableFuture;

public class EventWaiter<T extends Event> implements Listener {

	private final CompletableFuture<T> future = new CompletableFuture<>();
	private final Class<T> eventClass;

	public EventWaiter(Class<T> eventClass) {
		this.eventClass = eventClass;
		Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.NORMAL, (__, event) -> {
			future.complete(eventClass.cast(event));
			HandlerList.unregisterAll(this);
		}, MockBukkit.createMockPlugin());
	}

	public T assertFired() {
		var event = future.getNow(null);
		if (event == null)
			fail("Event %s has not been called".formatted(eventClass.getSimpleName()));
		return event;
	}

	public T assertFired(long millis) {
		return awaitFuture(future, millis, "Event %s has not been called".formatted(eventClass.getSimpleName()));
	}

	public void assertNotFired() {
		if (future.isDone())
			fail("Event %s has been called".formatted(eventClass.getSimpleName()));
	}

}