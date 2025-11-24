package fr.skytasul.quests;

import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static fr.skytasul.quests.test.TestUtils.waitForEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public class MessagingTest {

	private ServerMock server;

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		loadPlugin();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void testQuesterNamePlaceholder() {
		var questerJoinEventWaiter = waitForEvent(QuesterJoinEvent.class);

		var player = server.addPlayer("RealName");
		player.setDisplayName("DisplayName");

		var questerJoinEvent = questerJoinEventWaiter.assertFired(1000);
		var quester = questerJoinEvent.getQuester();

		MessageUtils.sendMessage(player, "{player};{player_display_name}", MessageType.DefaultMessageType.UNPREFIXED);
		assertEquals("§6RealName;DisplayName", player.nextMessage());

		MessageUtils.sendRawMessage(quester, "{player};{player_display_name};{quester_identifier}",
				null, PlaceholdersContext.of(quester, true, MessageType.DefaultMessageType.UNPREFIXED));
		assertEquals("§6RealName;DisplayName;" + player.getUniqueId().toString(), player.nextMessage());
	}

}
