package fr.skytasul.quests.integrations.placeholders;

import fr.skytasul.quests.api.utils.messaging.MessageProcessor;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiMessageProcessor implements MessageProcessor {

	@Override
	public @NotNull String processString(@NotNull String string, @NotNull PlaceholdersContext context) {
		if (context.replacePluginPlaceholders() && context.getActor() instanceof Player) {
			Player player = (Player) context.getActor();
			return PlaceholderAPI.setPlaceholders(player, string);
		}

		return string;
	}

}
