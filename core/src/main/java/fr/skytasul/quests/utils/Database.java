package fr.skytasul.quests.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.bukkit.configuration.ConfigurationSection;

import fr.skytasul.quests.BeautyQuests;

public class Database {

	private ConfigurationSection config;
	private Properties properties;
	private String host, database;
	private int port;

	private Connection connection;

	public Database(ConfigurationSection config) {
		this.config = config;
		this.host = config.getString("host");
		this.database = config.getString("database");
		this.port = config.getInt("port");

		properties = new Properties();
		properties.setProperty("user", config.getString("username"));
		properties.setProperty("password", config.getString("password"));
		if (!config.getBoolean("ssl")) {
			properties.setProperty("verifyServerCertificate", "false");
			properties.setProperty("useSSL", "false");
		}
	}

	public String getDatabase() {
		return database;
	}
	
	public ConfigurationSection getConfig() {
		return config;
	}

	public boolean openConnection() {
		if (!isClosed()) return false;

		// it seems no longer useful to load the Driver manually
		/*try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch (ClassNotFoundException e) {
			BeautyQuests.logger.severe("Database driver not found.");
			return false;
		}*/

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.getDatabase(), properties);
			DebugUtils.logMessage("Opened database connection.");
		}catch (SQLException ex) {
			BeautyQuests.logger.severe("An exception occurred when connecting to the database.");
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean isClosed() {
		try {
			return connection == null || connection.isClosed() || !connection.isValid(0);
		}catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}

	public void closeConnection() {
		if (!isClosed()) {
			try {
				connection.close();
				DebugUtils.logMessage("Closed database connection.");
			}catch (SQLException ex) {
				BeautyQuests.logger.severe("An exception occurred when closing database connection.");
				ex.printStackTrace();
			}
			connection = null;
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public PreparedStatement prepareStatementGeneratedKeys(String sql) throws SQLException {
		return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}

	public Connection getConnection() {
		return connection;
	}

	public static Database getInstance(){
		return BeautyQuests.getInstance().getBQDatabase();
	}

	public class BQStatement {
		private final String statement;
		private boolean returnGeneratedKeys;

		private PreparedStatement prepared;

		public BQStatement(String statement) {
			this(statement, false);
		}

		public BQStatement(String statement, boolean returnGeneratedKeys) {
			this.statement = statement;
			this.returnGeneratedKeys = returnGeneratedKeys;
		}

		public PreparedStatement getStatement() throws SQLException {
			if (prepared == null || prepared.isClosed() || !prepared.getConnection().isValid(0)) {
				openConnection();
				prepared = returnGeneratedKeys ? connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(statement);
			}
			return prepared;
		}

		public String getStatementCommand() {
			return statement;
		}
	}

}
