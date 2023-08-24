package fr.skytasul.quests.api.utils.messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;

public interface PlaceholdersContext {

	static final @NotNull PlaceholdersContext DEFAULT_CONTEXT = of(null, true, null);

	@Nullable
	CommandSender getActor();

	boolean replacePluginPlaceholders();

	@Nullable MessageType getMessageType();

	static PlaceholdersContext of(@Nullable CommandSender actor, boolean replacePluginPlaceholders,
			@Nullable MessageType messageType) {
		return new PlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @Nullable CommandSender getActor() {
				return actor;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

	static PlayerPlaceholdersContext of(@Nullable Player actor, boolean replacePluginPlaceholders,
			@Nullable MessageType messageType) {
		return new PlayerPlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @NotNull Player getActor() {
				return actor;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

	public interface PlayerPlaceholdersContext extends PlaceholdersContext {

		@Override
		@Nullable
		Player getActor();

		@NotNull
		default PlayerAccount getPlayerAccount() {
			return PlayersManager.getPlayerAccount(getActor());
		}

	}

}
