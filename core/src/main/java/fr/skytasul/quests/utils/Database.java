package fr.skytasul.quests.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation.DatabaseConfig;
import fr.skytasul.quests.api.QuestsPlugin;
import org.jetbrains.annotations.NotNull;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public class Database implements Closeable {

	private final DatabaseConfig config;

	private final DbType type;

	private final DataSource source;

	public Database(@NotNull DatabaseConfig config) throws IOException {
		this.config = config;

		var properties = new Properties();
		properties.load(getClass().getResourceAsStream("/hikari.properties"));
		HikariConfig hikariConfig = new HikariConfig(properties);

		String connectionString = config.getConnectionString();
		if (connectionString == null || connectionString.isEmpty())
			connectionString = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabaseName();

		Matcher matcher = Pattern.compile("^jdbc:(\\w+):\\/\\/").matcher(connectionString);
		if (matcher.find()) {
			switch (matcher.group(1).toLowerCase()) {
				case "mysql":
					type = DbType.MySQL;
					break;
				case "postgresql":
					type = DbType.PostgreSQL;
					break;
				default:
					QuestsPlugin.getPlugin().getLogger().warning("Unsupported database provider: " + matcher.group(1));
					type = DbType.MySQL;
					break;
			}
		} else {
			QuestsPlugin.getPlugin().getLogger().warning("Malformed database connection string!");
			type = DbType.MySQL;
		}

		hikariConfig.setJdbcUrl(connectionString);
		hikariConfig.setUsername(config.getUsername());
		hikariConfig.setPassword(config.getPassword());
		hikariConfig.setPoolName("BeautyQuests-SQL-pool");
		hikariConfig.setConnectionTimeout(20_000);

		boolean ssl = config.isSslEnabled();
		hikariConfig.addDataSourceProperty("verifyServerCertificate", ssl);
		hikariConfig.addDataSourceProperty("useSSL", ssl);

		source = new HikariDataSource(hikariConfig);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initialized database source. Type: " + type.name());
	}

	public void testConnection() throws SQLException {
		try (Connection connection = source.getConnection()) {
			if (!connection.isValid(0))
				throw new SQLException("Could not establish database connection.");
		}
	}

	public @NotNull DatabaseConfig getConfig() {
		return config;
	}

	public DbType getType() {
		return type;
	}

	@Override
	public void close() {
		QuestsPlugin.getPlugin().getLoggerExpanded().info("Closing database pool...");
		try {
			((Closeable) source).close();
		}catch (IOException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while closing database pool.", ex);
		}
	}

	public Connection getConnection() throws SQLException {
		return source.getConnection();
	}

	public static Database getInstance(){
		return BeautyQuests.getInstance().getBQDatabase();
	}

	public enum DbType {
		MySQL("INT NOT NULL AUTO_INCREMENT", "TINYINT", "LONGTEXT"), PostgreSQL("SERIAL", "BOOLEAN", "TEXT");

		private final String serialType;
		private final String booleanType;
		private final String longTextType;

		private DbType(String serialType, String booleanType, String longTextType) {
			this.serialType = serialType;
			this.booleanType = booleanType;
			this.longTextType = longTextType;
		}

		public String getSerialType() {
			return serialType;
		}

		public String getBooleanType() {
			return booleanType;
		}

		public String getLongTextType() {
			return longTextType;
		}
	}

}
