package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.PlayerPlaceholdersContext;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StageDescriptionPlaceholdersContext extends PlaceholdersContext, PlayerPlaceholdersContext {

	@Override
	@NotNull
	PlayerAccount getPlayerAccount();

	@NotNull
	DescriptionSource getDescriptionSource();

	static @NotNull StageDescriptionPlaceholdersContext of(boolean replacePluginPlaceholders, @NotNull PlayerAccount account,
			@NotNull DescriptionSource source, @Nullable MessageType messageType) {
		return new StageDescriptionPlaceholdersContext() {

			@Override
			public @Nullable Audience getAudience() {
				return account;
			}

			@Override
			public @Nullable Player getActor() {
				return account.getPlayer();
			}

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @NotNull PlayerAccount getPlayerAccount() {
				return account;
			}

			@Override
			public @NotNull DescriptionSource getDescriptionSource() {
				return source;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

}
