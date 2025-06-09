package fr.skytasul.quests.questers.data.yaml;

import static fr.skytasul.quests.test.TestUtils.loadPlugin;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.structure.QuestImplementation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;
import java.nio.file.Path;

class YamlQuesterDataTest {

	@TempDir
	private Path dataPath;
	private BeautyQuests plugin;

	private YamlDataManager dataManagerMock;

	@BeforeEach
	void setUp() {
		plugin = loadPlugin();

		dataManagerMock = Mockito.mock(YamlDataManager.class);
		Mockito.when(dataManagerMock.getDataPath()).thenReturn(dataPath);
	}

	@AfterEach
	void tearDown() throws Exception {
		MockBukkit.unmock();
	}

	@Test
	void testUnload() {
		var questerData = new YamlQuesterData(0, dataManagerMock);
		questerData.unload();
		Mockito.verify(dataManagerMock).uncache(questerData);
	}

	@Test
	void testPersistence() {
		var quest = new QuestImplementation(plugin.getQuestsManager(), 8, dataPath.resolve("qu.yml").toFile());

		var questerData = new YamlQuesterData(4, dataManagerMock);
		var questData = questerData.getQuestData(quest);
		questData.incrementFinished();

		assertDoesNotThrow(questerData::save);

		questerData = new YamlQuesterData(4, dataManagerMock);
		assertTrue(questerData.hasQuestData(quest));
		questData = questerData.getQuestDataIfPresent(quest).orElseThrow();

		assertEquals(1, questData.getTimesFinished());
	}

}
