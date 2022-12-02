package fr.skytasul.quests.utils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.bukkit.configuration.ConfigurationSection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fr.skytasul.quests.BeautyQuests;

public class Database implements Closeable {

	private final ConfigurationSection config;
	private final String databaseName;
	
	private final DataSource source;

	public Database(ConfigurationSection config) {
		this.config = config;
		this.databaseName = config.getString("database");

		HikariConfig hikariConfig = new HikariConfig("/hikari.properties");
		hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("host") + ":" + config.getInt("port") + "/" + databaseName);
		hikariConfig.setUsername(config.getString("username"));
		hikariConfig.setPassword(config.getString("password"));
		hikariConfig.setPoolName("BeautyQuests-SQL-pool");
		hikariConfig.setConnectionTimeout(20_000);
		
		boolean ssl = config.getBoolean("ssl");
		hikariConfig.addDataSourceProperty("verifyServerCertificate", ssl);
		hikariConfig.addDataSourceProperty("useSSL", ssl);
		
		source = new HikariDataSource(hikariConfig);
	}
	
	public void testConnection() throws SQLException {
		try (Connection connection = source.getConnection()) {
			if (!connection.isValid(0))
				throw new SQLException("Could not establish database connection.");
		}
	}

	public String getDatabase() {
		return databaseName;
	}
	
	public ConfigurationSection getConfig() {
		return config;
	}

	@Override
	public void close() {
		BeautyQuests.logger.info("Closing database pool...");
		try {
			((Closeable) source).close();
		}catch (IOException ex) {
			BeautyQuests.logger.severe("An error occurred while closing database pool.", ex);
		}
	}

	public Connection getConnection() throws SQLException {
		return source.getConnection();
	}
	
	public static Database getInstance(){
		return BeautyQuests.getInstance().getBQDatabase();
	}


}
