package fr.skytasul.quests.api.utils.messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;

public interface PlaceholdersContext {

	static final @NotNull PlaceholdersContext DEFAULT_CONTEXT = of(null, true);

	@Nullable
	CommandSender getActor();

	boolean replacePluginPlaceholders();

	static PlaceholdersContext of(@Nullable CommandSender actor, boolean replacePluginPlaceholders) {
		return new PlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @Nullable CommandSender getActor() {
				return actor;
			}
		};
	}

	static PlayerPlaceholdersContext of(@Nullable Player actor, boolean replacePluginPlaceholders) {
		return new PlayerPlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @NotNull Player getActor() {
				return actor;
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
