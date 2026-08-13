package fr.skytasul.quests.questers.data.sql;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.utils.Database;

public class SqlHandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {"jdbc:h2:mem:beautyquests", "jdbc:sqlite::memory:"})
    void testCreation(String connectionString) {
		var mockQuestersManager = Mockito.mock(QuesterManager.class);
		Mockito.when(mockQuestersManager.getSavableData()).thenReturn(List.of());

        var config = new FakeDatabaseConfig(connectionString);
        assertDoesNotThrow(() -> {
            try (var db = new Database(config)) {
                db.testConnection();

                new SqlHandler(db).createTables(mockQuestersManager);
            }
        });
    }

}
