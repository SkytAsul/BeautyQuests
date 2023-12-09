package fr.skytasul.quests.api;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestsAPIProvider {

	private static @Nullable QuestsAPI instance;

	private QuestsAPIProvider() {}

	public static @NotNull QuestsAPI getAPI() {
		if (instance == null)
			throw new IllegalStateException("BeautyQuests API is not yet initialized");
		return instance;
	}

	static void setAPI(@NotNull QuestsAPI api) {
		if (instance != null)
			throw new IllegalStateException("BeautyQuests API has already been set");
		instance = Objects.requireNonNull(api);
	}

}
