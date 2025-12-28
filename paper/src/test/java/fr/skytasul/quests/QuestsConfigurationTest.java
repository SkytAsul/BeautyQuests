package fr.skytasul.quests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import java.io.IOException;
import java.io.InputStreamReader;

class QuestsConfigurationTest {

	private YamlConfiguration config;

	@BeforeAll
	static void beforeAll() {
		MockBukkit.mock();
	}

	@AfterAll
	static void afterAll() {
		MockBukkit.unmock();
	}

	@BeforeEach
	void setUp() {
		config = new YamlConfiguration();
		try (var configStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
			assertNotNull(configStream);

			assertDoesNotThrow(() -> {
				config.load(new InputStreamReader(configStream));
			});
		} catch (IOException ex) {
			fail(ex);
		}
	}

	@Test
	void testProperLoading() {
		var data = new MemoryConfiguration(); // empty data.yml
		var questsConfig = new QuestsConfigurationImplementation(config, data);

		assertFalse(questsConfig.update());

		questsConfig.init();
	}

	@Test
	void testHologramsMigration() {
		config.set("disableTextHologram", true);
		config.set("hologramsHeight", 1.7);
		config.set("holoLaunchItemName", "IRON_INGOT");
		config.set("holoTalkItemName", "DIAMOND");
		config.set("showCustomHologramName", false);

		var questsConfig = new QuestsConfigurationImplementation(config, new MemoryConfiguration());

		assertTrue(questsConfig.update());
		questsConfig.init();

		assertEquals(true, questsConfig.getHologramsConfig().disableTextHologram());
		assertEquals(1.7, questsConfig.getHologramsConfig().additionalHeight());
		assertEquals(new ItemStack(Material.IRON_INGOT), questsConfig.getHologramsConfig().launchItem());
		assertEquals(new ItemStack(Material.DIAMOND), questsConfig.getHologramsConfig().talkItem());
		assertEquals(false, questsConfig.getHologramsConfig().customItemName());
	}

}
