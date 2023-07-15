package fr.skytasul.quests.api.utils.messaging;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.localization.Lang;

public class DefaultErrors {

	private DefaultErrors() {}

	public static void sendInvalidNumber(@NotNull CommandSender sender, @Nullable String input) {
		Lang.NUMBER_INVALID.send(sender, PlaceholderRegistry.of("input", input));
	}

	public static void sendOutOfBounds(@NotNull CommandSender sender, Number index, Number min, Number max) {
		Lang.OUT_OF_BOUNDS.send(sender, PlaceholderRegistry.of("index", index, "min", min, "max", max));
	}

	public static void sendOutOfBounds(@NotNull CommandSender sender, Number min, Number max) {
		Lang.NUMBER_NOT_IN_BOUNDS.send(sender, PlaceholderRegistry.of("min", min, "max", max));
	}

	public static void sendGeneric(@NotNull CommandSender sender, @Nullable String error) {
		Lang.ERROR_OCCURED.send(sender, PlaceholderRegistry.of("error", error));
	}

}
