package fr.skytasul.quests.players;

import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static fr.skytasul.quests.test.TestUtils.waitForEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.players.PlayerQuester;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.questers.events.QuesterLeaveEvent;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

@Tag("integration")
class PlayerManagerTest implements Listener {

	private ServerMock server;
	private BeautyQuests plugin;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = loadPlugin();
	}

	@Test
	void testPlayerQuesterLifecycle() {
		assertEquals(0, plugin.getPlayersManager().getLoadedQuesters().size());

		var questerJoinEventWaiter = waitForEvent(QuesterJoinEvent.class);
		var questerLeaveEventWaiter = waitForEvent(QuesterLeaveEvent.class);

		var player = server.addPlayer();
		// will load the quester
		var questerJoinEvent = questerJoinEventWaiter.assertFired(1000);

		assertTrue(questerJoinEvent.isFirstJoin());
		assertSame(player, questerJoinEvent.getPlayer());
		assertInstanceOf(PlayerQuester.class, questerJoinEvent.getQuester());

		var quester = (PlayerQuester) questerJoinEvent.getQuester();
		assertTrue(quester.isOnline());
		assertSame(player, quester.getPlayer().get());
		assertSame(player, quester.getOfflinePlayer());

		assertEquals(1, plugin.getPlayersManager().getLoadedQuesters().size());
		assertSame(quester, plugin.getPlayersManager().getQuester(player));

		questerLeaveEventWaiter.assertNotFired();

		player.disconnect();
		var questerLeaveEvent = questerLeaveEventWaiter.assertFired(1000);

		assertSame(quester, questerLeaveEvent.getQuester());
		assertFalse(quester.isOnline());
		assertFalse(quester.getPlayer().isPresent());
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

}
