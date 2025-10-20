package fr.skytasul.quests.api.utils.messaging;

import fr.skytasul.quests.api.localization.Lang;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultErrors {

	private DefaultErrors() {}

	public static void sendInvalidNumber(@NotNull Audience audience, @Nullable String input) {
		Lang.NUMBER_INVALID.send(audience, PlaceholderRegistry.of("input", input));
	}

	public static void sendOutOfBounds(@NotNull Audience audience, Number index, Number min, Number max) {
		Lang.OUT_OF_BOUNDS.send(audience, PlaceholderRegistry.of("index", index, "min", min, "max", max));
	}

	public static void sendOutOfBounds(@NotNull Audience audience, Number min, Number max) {
		Lang.NUMBER_NOT_IN_BOUNDS.send(audience, PlaceholderRegistry.of("min", min, "max", max));
	}

	public static void sendGeneric(@NotNull Audience audience, @Nullable String error) {
		Lang.ERROR_OCCURED.send(audience, PlaceholderRegistry.of("error", error));
	}

}
