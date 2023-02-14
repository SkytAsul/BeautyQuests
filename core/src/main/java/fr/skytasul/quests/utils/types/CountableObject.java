package fr.skytasul.quests.utils.types;

import java.util.UUID;

public interface CountableObject<T> {

	UUID getUUID();

	T getObject();

	int getAmount();

	default MutableCountableObject<T> toMutable() {
		return createMutable(getUUID(), getObject(), getAmount());
	}

	public interface MutableCountableObject<T> extends CountableObject<T> {

		void setObject(T object);

		void setAmount(int newAmount);

		default CountableObject<T> toImmutable() {
			return create(getUUID(), getObject(), getAmount());
		}

	}

	static <T> CountableObject<T> create(UUID uuid, T object, int amount) {
		return new DummyCountableObject<>(uuid, object, amount);
	}

	static <T> MutableCountableObject<T> createMutable(UUID uuid, T object, int amount) {
		return new DummyMutableCountableObject<>(uuid, object, amount);
	}

	class DummyCountableObject<T> implements CountableObject<T> {

		private final UUID uuid;
		private final T object;
		private final int amount;

		public DummyCountableObject(UUID uuid, T object, int amount) {
			this.uuid = uuid;
			this.object = object;
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
			this.uuid = uuid;
			this.object = object;
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
