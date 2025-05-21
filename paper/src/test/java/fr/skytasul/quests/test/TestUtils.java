package fr.skytasul.quests.test;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.events.internal.BeautyQuestsLoadedEvent;
import org.bukkit.event.Event;
import org.mockbukkit.mockbukkit.MockBukkit;

public class TestUtils {

	public static <T extends Event> EventWaiter<T> waitForEvent(Class<T> eventClass) {
		return new EventWaiter<>(eventClass);
	}

	public static BeautyQuests loadPlugin() {
		var loadedWaiter = new EventWaiter<>(BeautyQuestsLoadedEvent.class);
		var plugin = MockBukkit.load(BeautyQuests.class, Boolean.TRUE);
		loadedWaiter.assertFired(10);
		return plugin;
	}

}
