package fr.skytasul.quests.utils.types;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {
	private static final long serialVersionUID = -5836480055910467672L;
	private K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public void setValue(V value) {
		this.value = value;
	}

	public String toString() {
		return this.key + "=" + this.value;
	}

	public int hashCode() {
		return this.key.hashCode() * 13 + (this.value == null ? 0 : this.value.hashCode());
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof Pair)) {
			return false;
		} else {
			Pair<?, ?> pair = (Pair<?, ?>) obj;
			if (this.key != null) {
				if (!this.key.equals(pair.key)) {
					return false;
				}
			} else if (pair.key != null) {
				return false;
			}

			if (this.value != null) {
				if (!this.value.equals(pair.value)) {
					return false;
				}
			} else if (pair.value != null) {
				return false;
			}

			return true;
		}
	}
}