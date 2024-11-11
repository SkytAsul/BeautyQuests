package fr.skytasul.quests.integrations.placeholders;

import fr.skytasul.quests.api.utils.messaging.MessageProcessor;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class PapiMessageProcessor implements MessageProcessor {

	@Override
	public @NotNull String processString(@NotNull String string, @NotNull PlaceholdersContext context) {
		if (context.replacePluginPlaceholders()) {
			var playerOpt = context.getAudience().get(Identity.UUID).map(Bukkit::getOfflinePlayer);
			if (playerOpt.isPresent() && playerOpt.get().hasPlayedBefore())
				return PlaceholderAPI.setPlaceholders(playerOpt.get(), string);
		}

		return string;
	}

}
