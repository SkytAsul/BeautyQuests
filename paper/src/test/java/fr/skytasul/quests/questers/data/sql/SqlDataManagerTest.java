package fr.skytasul.quests.questers.data.sql;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.data.QuesterDataManager;
import fr.skytasul.quests.api.questers.data.QuesterDataManager.QuesterFetchRequest;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.questers.QuesterDataStub;
import fr.skytasul.quests.utils.Database;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;

class SqlDataManagerTest {

	final SavableData<Integer> dummySavableData = new SavableData<>("dummy", Integer.class, 1);

	SqlDataManager instance;

	@BeforeEach
	void setUp() throws InvalidConfigurationException, IOException, SQLException {
		Database db = new Database(new FakeDatabaseConfig());
		db.testConnection();

		instance = new SqlDataManager(db);
	}

	@AfterEach
	void tearDown() {
		instance.getSqlHandler().getDatabase().close();
	}

	@Test
	void testLoading() {
		var mockQuestersManager = Mockito.mock(QuesterManager.class);
		Mockito.when(mockQuestersManager.getSavableData()).thenReturn(List.of(dummySavableData));
		assertDoesNotThrow(() -> {
			instance.load(mockQuestersManager);
		});
	}

	void initDataManager() {
		var mockQuestersManager = Mockito.mock(QuesterManager.class);
		Mockito.when(mockQuestersManager.getSavableData()).thenReturn(List.of(dummySavableData));
		try {
			instance.load(mockQuestersManager);
		} catch (DataLoadingException ex) {
			abort("Failed to load data manager. " + ex);
		}
	}

	@Test
	void testNewQuesterNoCreation() {
		initDataManager();
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, false, false)).join();

		assertEquals(QuesterDataManager.QuesterFetchResult.Type.FAILED_NOT_FOUND, result.type());
		assertNull(result.dataHandler());
	}

	@Test
	void testNewQuesterCreationAndPersistence() throws InterruptedException, ExecutionException {
		initDataManager();
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		// first time: creation
		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, true)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_CREATED, result.type());
		assertNotNull(result.dataHandler());

		var questerData = result.dataHandler();
		var quest = Mockito.mock(Quest.class);
		Mockito.when(quest.getId()).thenReturn(1);
		assertFalse(questerData.hasQuestData(quest));
		questerData.getQuestData(quest).setStage(OptionalInt.of(1));
		questerData.unload();

		Thread.sleep(100); // wait db flush

		// second time: loading
		result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, false)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_LOADED, result.type());
		assertNotNull(result.dataHandler());

		questerData = result.dataHandler();
		assertTrue(questerData.hasQuestData(quest));
		assertEquals(OptionalInt.of(1), questerData.getQuestData(quest).getStage());
	}

	@Test
	@Disabled
	void testDataHandling() throws InterruptedException, ExecutionException {
		// TODO find a way to have a list of additional data in the quester class

		initDataManager();
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		// first time: creation
		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, true)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_CREATED, result.type());
		assertNotNull(result.dataHandler());

		var questerData = result.dataHandler();
		assertEquals(dummySavableData.getDefaultValue(), questerData.getData(dummySavableData));
		int newData = 10;
		int oldData = questerData.setData(dummySavableData, newData).get();
		assertEquals(dummySavableData.getDefaultValue(), oldData);
		assertEquals(newData, questerData.getData(dummySavableData));

		questerData.unload();

		// second time: loading
		result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, true, false)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_LOADED, result.type());
		assertNotNull(result.dataHandler());

		questerData = result.dataHandler();
		assertEquals(newData, questerData.getData(dummySavableData));
	}

	@Test
	void testDataImport() throws InterruptedException, ExecutionException {
		initDataManager();
		var providerKey = Key.key("some-provider");
		var identifier = "some-identifier";

		var quest = Mockito.mock(Quest.class);
		Mockito.when(quest.getId()).thenReturn(1);

		var data = new QuesterDataStub(providerKey, identifier);
		data.getQuestData(quest).setStage(OptionalInt.of(1));
		var questers = List.of(data);

		// actual import
		var importResult = instance.importAll(questers.iterator()).get();
		assertEquals(1, importResult.questers());
		assertEquals(0, importResult.failures());

		var result = instance.fetchQuester(new QuesterFetchRequest(providerKey, identifier, false, false)).join();
		assertEquals(QuesterDataManager.QuesterFetchResult.Type.SUCCESS_LOADED, result.type());
		assertNotNull(result.dataHandler());

		var newData = result.dataHandler();
		assertTrue(newData.hasQuestData(quest));
		assertEquals(OptionalInt.of(1), newData.getQuestData(quest).getStage());
	}

	static class FakeDatabaseConfig implements QuestsConfiguration.Database {
		@Override
		public boolean enabled() {
			return true;
		}

		@Override
		public @Nullable String host() {
			return null;
		}

		@Override
		public int port() {
			return 0;
		}

		@Override
		public @NotNull String databaseName() {
			return "beautyquests";
		}

		@Override
		public @Nullable String username() {
			return null;
		}

		@Override
		public @Nullable String password() {
			return null;
		}

		@Override
		public boolean sslEnabled() {
			return false;
		}

		@Override
		public @Nullable String connectionString() {
			return "jdbc:h2:mem:beautyquests";
		}

		@Override
		public @NotNull Map<String, String> tables() {
			return Map.of("questers", "questers", "questers quests", "questers_quests", "questers pools", "questers_pools");
		}

	}

}
