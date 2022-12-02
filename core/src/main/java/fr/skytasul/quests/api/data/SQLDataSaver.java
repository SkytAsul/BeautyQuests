package fr.skytasul.quests.api.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

import fr.skytasul.quests.utils.CustomizedObjectTypeAdapter;

public class SQLDataSaver<T> {
	
	private static final SQLType<Date> TYPE_DATE = new SQLType<Date>(Types.TIMESTAMP, "TIMESTAMP", (resultSet, column) -> new Date(resultSet.getTimestamp(column).getTime())) {
		@Override
		public Object convert(Date obj) {
			return new Timestamp(obj.getTime());
		}
	};
	private static final SQLType<Character> TYPE_CHAR = new SQLType<Character>(Types.CHAR, "CHAR(1)", (resultSet, column) -> resultSet.getString(column).charAt(0)) {
		@Override
		public Object convert(Character obj) {
			return obj.toString();
		}
	}.omitLength();
	private static final SQLType<String> TYPE_STRING = new SQLType<String>(Types.VARCHAR, "VARCHAR", ResultSet::getString).requiresLength();
	private static final SQLType<Boolean> TYPE_BOOLEAN = new SQLType<>(Types.BOOLEAN, "BOOLEAN", ResultSet::getBoolean);
	private static final SQLType<Float> TYPE_FLOAT = new SQLType<>(Types.FLOAT, "FLOAT", ResultSet::getFloat);
	private static final SQLType<Double> TYPE_DOUBLE = new SQLType<>(Types.DOUBLE, "DOUBLE", ResultSet::getDouble);
	private static final SQLType<Long> TYPE_BIGINT = new SQLType<>(Types.BIGINT, "BIGINT", ResultSet::getLong);
	private static final SQLType<Integer> TYPE_INT = new SQLType<>(Types.INTEGER, "INTEGER", ResultSet::getInt);
	private static final SQLType<Short> TYPE_SMALLINT = new SQLType<>(Types.SMALLINT, "SMALLINT", ResultSet::getShort);
	private static final SQLType<Byte> TYPE_TINYINT = new SQLType<>(Types.TINYINT, "TINYINT", ResultSet::getByte);

	private static final Map<Class<?>, SQLType<?>> SQL_TYPES = new HashMap<>(ImmutableMap.<Class<?>, SQLType<?>>builder()
			.put(byte.class, TYPE_TINYINT)
			.put(Byte.class, TYPE_TINYINT)
			.put(short.class, TYPE_SMALLINT)
			.put(Short.class, TYPE_SMALLINT)
			.put(int.class, TYPE_INT)
			.put(Integer.class, TYPE_INT)
			.put(long.class, TYPE_BIGINT)
			.put(Long.class, TYPE_BIGINT)
			.put(double.class, TYPE_DOUBLE)
			.put(Double.class, TYPE_DOUBLE)
			.put(float.class, TYPE_FLOAT)
			.put(Float.class, TYPE_FLOAT)
			.put(boolean.class, TYPE_BOOLEAN)
			.put(Boolean.class, TYPE_BOOLEAN)
			.put(char.class, TYPE_CHAR)
			.put(Character.class, TYPE_CHAR)
			.put(String.class, TYPE_STRING)
			.put(Date.class, TYPE_DATE)
			.build());
	
	private final SavableData<T> wrappedData;
	private final SQLType<T> sqlType;
	private final String updateStatement;
	private final String columnDefinition;
	private final String defaultValueString;
	
	public SQLDataSaver(SavableData<T> wrappedData, String updateStatement) {
		this.wrappedData = wrappedData;
		this.updateStatement = updateStatement;
		
		sqlType = (SQLType<T>) SQL_TYPES.computeIfAbsent(wrappedData.getDataType(), JsonSQLType::new);
		
		String length = "";
		if (!sqlType.omitLength) {
			if (wrappedData.getMaxLength().isPresent()) {
				length = "(" + wrappedData.getMaxLength().getAsInt() + ")";
			}else {
				if (sqlType.requiresLength)
					throw new IllegalArgumentException("Column " + wrappedData.getColumnName() + " requires a max length.");
			}
		}
		
		defaultValueString = Objects.toString(wrappedData.getDefaultValue());
		columnDefinition = String.format("`%s` %s%s DEFAULT %s", wrappedData.getColumnName(), sqlType.sqlTypeName, length, defaultValueString);
	}
	
	public SavableData<T> getWrappedData() {
		return wrappedData;
	}
	
	public String getUpdateStatement() {
		return updateStatement;
	}
	
	public String getColumnDefinition() {
		return columnDefinition;
	}
	
	public String getDefaultValueString() {
		return defaultValueString;
	}
	
	public void setInStatement(PreparedStatement statement, int index, T value) throws SQLException {
		statement.setObject(index, sqlType.convert(value), sqlType.jdbcTypeCode);
	}
	
	public T getFromResultSet(ResultSet resultSet) throws SQLException {
		return sqlType.getter.get(resultSet, wrappedData.getColumnName());
	}
	
	private static class SQLType<T> {
		private final int jdbcTypeCode;
		private final String sqlTypeName;
		private final ResultSetProcessor<T> getter;
		
		private boolean requiresLength = false;
		private boolean omitLength = false;
		
		private SQLType(int jdbcTypeCode, String sqlTypeName, ResultSetProcessor<T> getter) {
			this.jdbcTypeCode = jdbcTypeCode;
			this.sqlTypeName = sqlTypeName;
			this.getter = getter;
		}
		
		public SQLType<T> requiresLength() {
			requiresLength = true;
			return this;
		}
		
		public SQLType<T> omitLength() {
			omitLength = true;
			return this;
		}
		
		public Object convert(T obj) {
			return obj;
		}
		
	}
	
	private static class JsonSQLType<T> extends SQLType<T> {
		private JsonSQLType(Class<T> type) {
			super(Types.VARCHAR, "JSON", (resultSet, column) -> {
				String json = resultSet.getString(column);
				return CustomizedObjectTypeAdapter.GSON.fromJson(json, type);
			});
		}
		
		@Override
		public Object convert(T obj) {
			return CustomizedObjectTypeAdapter.GSON.toJson(obj);
		}
	}
	
	@FunctionalInterface
	public static interface ResultSetProcessor<T> {
		T get(ResultSet resultSet, String column) throws SQLException;
	}
	
}
