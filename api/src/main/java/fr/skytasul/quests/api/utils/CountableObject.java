package fr.skytasul.quests.api.utils;

import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface CountableObject<T> {

	@NotNull
	UUID getUUID();

	@NotNull
	T getObject();

	int getAmount();

	default @NotNull MutableCountableObject<T> toMutable() {
		return createMutable(getUUID(), getObject(), getAmount());
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

		private final UUID uuid;
		private final T object;
		private final int amount;

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

	}

	class DummyMutableCountableObject<T> implements MutableCountableObject<T> {

		private final UUID uuid;
		private T object;
		private int amount;

		public DummyMutableCountableObject(UUID uuid, T object, int amount) {
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
		public void setObject(T object) {
			this.object = object;
		}

		@Override
		public int getAmount() {
			return amount;
		}

		@Override
		public void setAmount(int amount) {
			this.amount = amount;
		}

	}

}
