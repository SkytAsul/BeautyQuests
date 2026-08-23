package fr.skytasul.quests.questers.data.sql;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.skytasul.quests.api.QuestsConfiguration;

class FakeDatabaseConfig implements QuestsConfiguration.Database {

    private final String connectionString;

    FakeDatabaseConfig(@NotNull String connectionString) {
        this.connectionString = connectionString;
    }

	@Override
	public boolean enabled() {
		return true;
	}

	@Override
	public @Nullable String host() {
		return null;
	}

	@Override
	public int port() {
		return 0;
	}

	@Override
	public @NotNull String databaseName() {
		return "unused"; // we use the JDBC URL
	}

	@Override
	public @Nullable String username() {
		return null;
	}

	@Override
	public @Nullable String password() {
		return null;
	}

	@Override
	public boolean sslEnabled() {
		return false;
	}

	@Override
	public @Nullable String connectionString() {
		return connectionString;
	}

	@Override
	public @NotNull Map<String, String> tables() {
		return Map.of("questers", "QUESTERS", "questers quests", "QUESTERS_QUESTS", "questers pools", "QUESTERS_POOLS");
	}

}
