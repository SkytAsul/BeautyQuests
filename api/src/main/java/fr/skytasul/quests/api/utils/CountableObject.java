package fr.skytasul.quests.api.utils;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public interface CountableObject<T> extends HasPlaceholders {

	@NotNull
	UUID getUUID();

	@NotNull
	T getObject();

	int getAmount();

	default @NotNull MutableCountableObject<T> toMutable() {
		return createMutable(getUUID(), getObject(), getAmount());
	}

	@Override
	default @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		return PlaceholderRegistry.of("amount", getAmount());
	}

	public interface MutableCountableObject<T> extends CountableObject<T> {

		void setObject(@NotNull T object);

		void setAmount(int newAmount);

		default @NotNull CountableObject<T> toImmutable() {
			return create(getUUID(), getObject(), getAmount());
		}

	}

	static <T> CountableObject<T> create(@NotNull UUID uuid, @NotNull T object, int amount) {
		return new DummyCountableObject<>(uuid, object, amount);
	}

	static <T> @NotNull MutableCountableObject<T> createMutable(@NotNull UUID uuid, @NotNull T object, int amount) {
		return new DummyMutableCountableObject<>(uuid, object, amount);
	}

	class DummyCountableObject<T> implements CountableObject<T> {

		protected final UUID uuid;
		protected T object;
		protected int amount;

		public DummyCountableObject(UUID uuid, T object, int amount) {
			this.uuid = Objects.requireNonNull(uuid);
			this.object = Objects.requireNonNull(object);
			this.amount = amount;
		}

		@Override
		public UUID getUUID() {
			return uuid;
		}

		@Override
		public T getObject() {
			return object;
		}

		@Override
		public int getAmount() {
			return amount;
		}

		@Override
		public int hashCode() {
			return uuid.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CountableObject))
				return false;
			CountableObject<T> other = (CountableObject<T>) obj;
			return other.getUUID().equals(uuid);
		}

	}

	class DummyMutableCountableObject<T> extends DummyCountableObject<T> implements MutableCountableObject<T> {

		public DummyMutableCountableObject(UUID uuid, T object, int amount) {
			super(uuid, object, amount);
		}

		@Override
		public void setObject(T object) {
			this.object = object;
		}

		@Override
		public void setAmount(int amount) {
			this.amount = amount;
		}

	}

}
