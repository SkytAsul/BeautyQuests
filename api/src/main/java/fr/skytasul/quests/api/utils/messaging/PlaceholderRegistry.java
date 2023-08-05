package fr.skytasul.quests.api.utils.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderRegistry implements HasPlaceholders {

	private final List<Placeholder> placeholders;
	private final List<Placeholder> indexed = new ArrayList<>(2);

	public PlaceholderRegistry() {
		// effectively the same as the other constructor with no argument
		// but without the overhead of creating an empty array and then an empty list
		this.placeholders = new ArrayList<>(3);
	}

	public PlaceholderRegistry(Placeholder... placeholders) {
		this.placeholders = new ArrayList<>(Arrays.asList(placeholders));
	}

	public @Nullable Placeholder getPlaceholder(@NotNull String key) {
		try {
			int index = Integer.parseInt(key);
			if (index < indexed.size())
				return indexed.get(index);
		} catch (NumberFormatException ex) {
			// means the key is not indexed
		}

		for (Placeholder placeholder : placeholders) {
			if (placeholder.matches(key))
				return placeholder;
		}
		return null;
	}

	public boolean hasPlaceholder(@NotNull String key) {
		return getPlaceholder(key) == null;
	}

	public @Nullable String resolve(@NotNull String key, @NotNull PlaceholdersContext context) {
		@Nullable
		Placeholder placeholder = getPlaceholder(key);

		if (placeholder == null)
			return null;

		return placeholder.resolve(key, context);
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		return this;
	}

	public @NotNull PlaceholderRegistry register(Placeholder placeholder) {
		placeholders.add(placeholder);
		return this;
	}

	public @NotNull PlaceholderRegistry register(String key, Object value) {
		return register(Placeholder.of(key, value));
	}

	public @NotNull PlaceholderRegistry register(String key, Supplier<String> valueSupplier) {
		return register(Placeholder.ofSupplier(key, valueSupplier));
	}

	public @NotNull <T extends PlaceholdersContext> PlaceholderRegistry registerContextual(@NotNull String key,
			@NotNull Class<T> contextClass, @NotNull Function<T, String> valueFunction) {
		return register(Placeholder.ofContextual(key, contextClass, valueFunction));
	}

	private @NotNull PlaceholderRegistry registerIndexed(Placeholder placeholder) {
		placeholders.add(placeholder);
		indexed.add(placeholder);
		return this;
	}

	public @NotNull PlaceholderRegistry registerIndexed(String key, Object value) {
		return registerIndexed(Placeholder.of(key, value));
	}

	public @NotNull PlaceholderRegistry registerIndexed(String key, Supplier<String> valueSupplier) {
		return registerIndexed(Placeholder.ofSupplier(key, valueSupplier));
	}

	public @NotNull <T extends PlaceholdersContext> PlaceholderRegistry registerIndexedContextual(@NotNull String key,
			@NotNull Class<T> contextClass, @NotNull Function<T, String> valueFunction) {
		return registerIndexed(Placeholder.ofContextual(key, contextClass, valueFunction));
	}

	/**
	 * Adds all the placeholders from the passed placeholders holders to this placeholders registry.
	 * 
	 * @param placeholdersHolders holders to get the placeholders from
	 * @return this placeholder registry
	 */
	public @NotNull PlaceholderRegistry compose(@NotNull HasPlaceholders @NotNull... placeholdersHolders) {
		for (HasPlaceholders holder : placeholdersHolders) {
			this.placeholders.addAll(holder.getPlaceholdersRegistry().placeholders);
			if (!holder.getPlaceholdersRegistry().indexed.isEmpty())
				this.indexed.addAll(holder.getPlaceholdersRegistry().indexed);
		}
		return this;
	}

	/**
	 * Creates a <i>new</i> placeholders registry containing the placeholders of this instance in
	 * addition with those from the passed placeholders holders.
	 * 
	 * @param placeholdersHolders holders to get the placeholders from
	 * @return a new placeholder registry containing all placeholders
	 * @see #combine(HasPlaceholders...)
	 */
	public @NotNull PlaceholderRegistry with(@NotNull HasPlaceholders @NotNull... placeholdersHolders) {
		HasPlaceholders[] others = new HasPlaceholders[placeholdersHolders.length + 1];
		others[0] = this;
		System.arraycopy(placeholdersHolders, 0, others, 1, placeholdersHolders.length);
		return combine(others);
	}

	/**
	 * Creates a <i>new</i> placeholders registry containing the same placeholders as this instance but
	 * with the indexed placeholers being shifted so that the passed placeholder is the first one.
	 * 
	 * @param placeholder first placeholder to be indexed
	 * @return a copy of this registry with the indexed placeholders shifted
	 */
	public @NotNull PlaceholderRegistry shifted(@NotNull Placeholder placeholder) {
		int index = indexed.indexOf(placeholder);
		if (index == -1)
			throw new IllegalArgumentException();

		PlaceholderRegistry shifted = new PlaceholderRegistry();
		shifted.placeholders.addAll(this.placeholders);
		Placeholder[] indexedShifted = new Placeholder[this.indexed.size() - index];
		System.arraycopy(this.indexed.toArray(new Placeholder[this.indexed.size()]), index, indexedShifted, 0,
				this.indexed.size() - index);
		shifted.indexed.addAll(Arrays.asList(indexedShifted));

		return shifted;
	}

	public @NotNull PlaceholderRegistry shifted(@NotNull String key) {
		return shifted(Objects.requireNonNull(getPlaceholder(key)));
	}

	public static @NotNull PlaceholderRegistry of(@NotNull String key1, @Nullable Object value1) {
		return new PlaceholderRegistry()
				.registerIndexed(key1, value1);
	}

	public static @NotNull PlaceholderRegistry of(@NotNull String key1, @Nullable Object value1, @NotNull String key2,
			@Nullable Object value2) {
		return new PlaceholderRegistry()
				.registerIndexed(key1, value1)
				.registerIndexed(key2, value2);
	}

	public static @NotNull PlaceholderRegistry of(@NotNull String key1, @Nullable Object value1, @NotNull String key2,
			@Nullable Object value2, @NotNull String key3, @Nullable Object value3) {
		return new PlaceholderRegistry()
				.registerIndexed(key1, value1)
				.registerIndexed(key2, value2)
				.registerIndexed(key3, value3);
	}

	/**
	 * Creates a placeholders registry containing the placeholders of all the passed placeholder
	 * holders.
	 * 
	 * @param placeholdersHolders holders to get the placeholders from
	 * @return a new placeholder registry containing all placeholders
	 */
	public static @NotNull PlaceholderRegistry combine(@NotNull HasPlaceholders @NotNull... placeholdersHolders) {
		PlaceholderRegistry result = new PlaceholderRegistry();
		for (HasPlaceholders holder : placeholdersHolders) {
			result.placeholders.addAll(holder.getPlaceholdersRegistry().placeholders);
			if (!holder.getPlaceholdersRegistry().indexed.isEmpty())
				result.indexed.addAll(holder.getPlaceholdersRegistry().indexed);
		}
		return result;
	}

}
