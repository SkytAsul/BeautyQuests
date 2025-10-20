package fr.skytasul.quests;

import fr.skytasul.quests.api.QuestsAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public final class QuestsAPIProvider {

	private static @Nullable QuestsAPI instance;

	private QuestsAPIProvider() {}

	public static @NotNull QuestsAPI getAPI() {
		if (instance == null)
			throw new IllegalStateException("BeautyQuests API is not yet initialized");
		return instance;
	}

	static void initializeAPI(@NotNull QuestsAPI api) {
		if (instance != null)
			throw new IllegalStateException("BeautyQuests API has already been set");
		instance = Objects.requireNonNull(api);
	}

	static void removeAPI() {
		instance = null;
	}

}
