package fr.skytasul.quests.api.data;

import java.util.Objects;
import java.util.OptionalInt;

public class SavableData<T> {
	
	private final String id;
	private final Class<T> dataType;
	private final T defaultValue;
	private final OptionalInt maxLength;
	
	private String columnName;
	
	public SavableData(String id, Class<T> dataType, T defaultValue) {
		this(id, dataType, defaultValue, OptionalInt.empty());
	}

	public SavableData(String id, Class<T> dataType, T defaultValue, OptionalInt maxLength) {
		if (id == null || id.isEmpty()) throw new IllegalArgumentException("Data id cannot be null or empty");
		if (dataType == null) throw new IllegalArgumentException("Data type cannot be null");
		if (maxLength == null) throw new IllegalArgumentException("Data max length cannot be a null optional");
		this.id = id;
		this.dataType = dataType;
		this.defaultValue = defaultValue;
		this.maxLength = maxLength;
	}
	
	public String getId() {
		return id;
	}
	
	public Class<T> getDataType() {
		return dataType;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public OptionalInt getMaxLength() {
		return maxLength;
	}
	
	public String getColumnName() {
		return columnName == null ? id : columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		
		hash = hash * 23 + id.hashCode();
		hash = hash * 23 + dataType.hashCode();
		hash = hash * 23 + maxLength.hashCode();
		hash = hash * 23 + Objects.hashCode(defaultValue);
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SavableData)) return false;
		SavableData oth = (SavableData) obj;
		return oth.id.equals(id)
				&& oth.dataType.equals(dataType)
				&& Objects.equals(oth.defaultValue, defaultValue)
				&& oth.maxLength.equals(maxLength);
	}
	
}
