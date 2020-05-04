package fr.skytasul.quests.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.ConfigurationSection;

import fr.skytasul.quests.BeautyQuests;

public class Database {

	private String username, password, host, database;
	private int port;

	private Connection connection;
	private Statement statement;

	public Database(ConfigurationSection config) {
		this.username = config.getString("username");
		this.password = config.getString("password");
		this.host = config.getString("host");
		this.database = config.getString("database");
		this.port = config.getInt("port");
	}

	public String getDatabase() {
		return database;
	}

	public boolean openConnection() {
		if (!isClosed()) return false;

		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch (ClassNotFoundException e) {
			BeautyQuests.logger.severe("Database driver not found.");
			return false;
		}

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.getDatabase(), this.username, this.password);
			statement = connection.createStatement();
		}catch (SQLException ex) {
			BeautyQuests.logger.severe("An exception occured when connecting to the database.");
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
		if (isClosed()) {
			try {
				connection.close();
			}catch (SQLException e1) {}
			connection = null;
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public PreparedStatement prepareStatementGeneratedKeys(String sql) throws SQLException {
		return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}

	public Statement getStatement() {
		return statement;
	}

	public static Database getInstance(){
		return BeautyQuests.getInstance().getDatabase();
	}

	public class BQStatement {
		private final String statement;
		private boolean returnGeneratedKeys;

		public BQStatement(String statement) {
			this(statement, false);
		}

		public BQStatement(String statement, boolean returnGeneratedKeys) {
			this.statement = statement;
			this.returnGeneratedKeys = returnGeneratedKeys;
		}

		private PreparedStatement prepared;

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
