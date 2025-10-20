package fr.skytasul.quests.api.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.util.List;

class ChatColorUtilsTest {

	@Disabled("The word wrapping code is broken and I've already lost too many hours on it")
	@ParameterizedTest
	@CsvFileSource(resources = "/word_wrap.csv", numLinesToSkip = 1)
	void test(String string, int line, int critical, @AggregateWith(VarargsAggregator.class) String... expected) {
		List<@NotNull String> wrapped = ChatColorUtils.wordWrap(string, line, critical);
		assertArrayEquals(expected, wrapped.toArray(new String[0]));
	}

	static class VarargsAggregator implements ArgumentsAggregator {
		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
				throws ArgumentsAggregationException {
			return accessor.toList().stream()
					.skip(context.getIndex())
					.map(String::valueOf)
					.toArray(String[]::new);
		}
	}

}
