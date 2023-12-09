package fr.skytasul.quests.api.utils.messaging;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Placeholder {

	boolean matches(@NotNull String key);

	@Nullable
	String resolve(@NotNull String key, @NotNull PlaceholdersContext context);

	static Placeholder of(@NotNull String key, @Nullable Object value) {
		String string = Objects.toString(value);
		return ofSupplier(key, () -> string);
	}

	static Placeholder ofSupplier(@NotNull String key, @NotNull Supplier<@Nullable String> valueSupplier) {
		return new Placeholder() {

			@Override
			public @Nullable String resolve(@NotNull String key, @NotNull PlaceholdersContext context) {
				return valueSupplier.get();
			}

			@Override
			public boolean matches(@NotNull String keyToMatch) {
				return keyToMatch.equals(key);
			}
		};
	}

	static Placeholder ofPattern(@NotNull String regex,
			@NotNull Function<@NotNull Matcher, @Nullable String> valueFunction) {
		return new Placeholder() {
			private Pattern pattern = Pattern.compile(regex);
			private Map<String, Matcher> matchers = new ConcurrentHashMap<>();

			@Override
			public @Nullable String resolve(@NotNull String key, @NotNull PlaceholdersContext context) {
				Matcher matcher = matchers.remove(key);
				if (matcher == null) {
					matcher = pattern.matcher(key);
					if (!matcher.matches())
						throw new IllegalStateException();
				}
				return valueFunction.apply(matcher);
			}

			@Override
			public boolean matches(@NotNull String key) {
				Matcher matcher = pattern.matcher(key);
				if (matcher.matches()) {
					matchers.put(key, matcher);
					return true;
				}
				return false;
			}
		};
	}

	static <T extends PlaceholdersContext> Placeholder ofContextual(@NotNull String key, @NotNull Class<T> contextClass,
			@NotNull Function<T, String> valueFunction) {
		return new ContextualizedPlaceholder<T>() {

			@Override
			public boolean matches(@NotNull String keyToMatch) {
				return keyToMatch.equals(key);
			}

			@Override
			public @NotNull Class<T> getContextClass() {
				return contextClass;
			}

			@Override
			public @Nullable String resolveContextual(@NotNull String key, T context) {
				return valueFunction.apply(context);
			}
		};
	}

	static <T extends PlaceholdersContext> Placeholder ofPatternContextual(@NotNull String regex,
			@NotNull Class<T> contextClass,
			@NotNull BiFunction<@NotNull Matcher, @NotNull T, @Nullable String> valueFunction) {
		return new ContextualizedPlaceholder<T>() {
			private Pattern pattern = Pattern.compile(regex);
			private Map<String, Matcher> matchers = new ConcurrentHashMap<>();

			@Override
			public @Nullable String resolveContextual(@NotNull String key, @NotNull T context) {
				Matcher matcher = matchers.remove(key);
				if (matcher == null) {
					matcher = pattern.matcher(key);
					if (!matcher.matches())
						throw new IllegalStateException();
				}
				return valueFunction.apply(matcher, context);
			}

			@Override
			public boolean matches(@NotNull String key) {
				Matcher matcher = pattern.matcher(key);
				if (matcher.matches()) {
					matchers.put(key, matcher);
					return true;
				}
				return false;
			}

			@Override
			public @NotNull Class<T> getContextClass() {
				return contextClass;
			}
		};
	}

	public interface ContextualizedPlaceholder<T extends PlaceholdersContext> extends Placeholder {

		@NotNull
		Class<T> getContextClass();

		@Override
		default @Nullable String resolve(@NotNull String key, @NotNull PlaceholdersContext context) {
			if (!getContextClass().isInstance(context))
				throw new IllegalArgumentException("Context is not of type " + getContextClass().getName());

			return resolveContextual(key, getContextClass().cast(context));
		}

		@Nullable
		String resolveContextual(@NotNull String key, T context);

	}

}
