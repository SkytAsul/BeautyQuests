package fr.skytasul.quests.api.utils.messaging;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class PlaceholderRegistryTest {

	@Test
	void testGetPlaceholder() {
		var registry = PlaceholderRegistry.of("a", "a");

		assertNotNull(registry.getPlaceholder("a"));
		assertNull(registry.getPlaceholder("b"));
	}

	@Test
	void testCombination() {
		var registry1 = PlaceholderRegistry.of("a", "a");
		var registry2 = PlaceholderRegistry.of("b", "b");

		var composed = PlaceholderRegistry.combine(registry1, registry2);
		assertNotNull(composed.getPlaceholder("a"));
		assertNotNull(composed.getPlaceholder("b"));

		composed = registry1.with(registry2);
		assertNotNull(composed.getPlaceholder("a"));
		assertNotNull(composed.getPlaceholder("b"));
	}

}
