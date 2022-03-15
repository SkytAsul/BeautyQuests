package fr.skytasul.quests.utils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.bukkit.configuration.ConfigurationSection;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

import fr.skytasul.quests.BeautyQuests;

public class Database {

	private ConfigurationSection config;
	private String databaseName;

	private DataSource source;

	public Database(ConfigurationSection config) throws SQLException {
		this.config = config;
		this.databaseName = config.getString("database");
		
		try {
			Class.forName("org.mariadb.jdbc.MariaDbPoolDataSource");
			MariaDbPoolDataSource msource = new MariaDbPoolDataSource();
			msource.setPoolName("beautyquests");
			msource.setServerName(config.getString("host"));
			msource.setPortNumber(config.getInt("port"));
			msource.setDatabaseName(databaseName);
			msource.setUser(config.getString("username"));
			msource.setPassword(config.getString("password"));
			msource.setMaxIdleTime(60);
			source = msource;
		}catch (ClassNotFoundException e) {
			MysqlDataSource msource = new MysqlDataSource();
			msource.setServerName(config.getString("host"));
			msource.setPortNumber(config.getInt("port"));
			msource.setDatabaseName(databaseName);
			msource.setUser(config.getString("username"));
			msource.setPassword(config.getString("password"));
			boolean ssl = config.getBoolean("ssl");
			msource.setVerifyServerCertificate(ssl);
			msource.setUseSSL(ssl);
			source = msource;
		}
		DebugUtils.logMessage("Created SQL data source: " + source.getClass().getName());
		// Yes, I know this is literally the same code.
		// Unfortunately,  there is no common interface
		// with MariaDB and MySQL pool data source
		// which provides the configuration methods.
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

	public void closeConnection() {
		if (source instanceof Closeable) {
			try {
				((Closeable) source).close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConnection() throws SQLException {
		return source.getConnection();
	}
	
	public static Database getInstance(){
		return BeautyQuests.getInstance().getBQDatabase();
	}


}
