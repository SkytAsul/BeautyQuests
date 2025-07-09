package fr.skytasul.quests.questers.data.yaml;

import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.questers.data.QuesterDataManager;
import fr.skytasul.quests.api.questers.data.QuesterDataManager.QuesterFetchRequest;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class YamlDataManagerTest {

	private BeautyQuests plugin;
	private YamlDataManager instance;

	@BeforeEach
	void setUp() throws Exception {
		plugin = loadPlugin();
		instance = (YamlDataManager) plugin.getQuesterManager().getDataManager();
	}

	@AfterEach
	void tearDown() throws Exception {
		MockBukkit.unmock();
	}

	@Test
	void testNewQuesterNoCreation() {
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, false, false)).join();

		assertEquals(QuesterDataManager.QuesterFetchResult.Type.FAILED_NOT_FOUND, result.type());
		assertNull(result.dataHandler());
	}

	@Test
	void testNewQuesterCreation() {
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		// first time: creation
		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, false)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_CREATED, result.type());
		assertNotNull(result.dataHandler());

		// second time: loading
		result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, false)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_LOADED, result.type());
		assertNotNull(result.dataHandler());
	}

}
