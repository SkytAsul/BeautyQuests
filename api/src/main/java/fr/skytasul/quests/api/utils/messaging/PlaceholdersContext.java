package fr.skytasul.quests.api.utils.messaging;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlaceholdersContext {

	static final @NotNull PlaceholdersContext DEFAULT_CONTEXT = of((Audience) null, true, null);

	@Nullable
	Audience getAudience();

	boolean replacePluginPlaceholders();

	@Nullable MessageType getMessageType();

	static PlaceholdersContext of(@Nullable Audience audience, boolean replacePluginPlaceholders,
			@Nullable MessageType messageType) {
		return new PlaceholdersContext() {
			@Override
			public @NotNull Audience getAudience() {
				return audience;
			}

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
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
			public @Nullable Audience getAudience() {
				return actor == null ? null : QuestsPlugin.getPlugin().getAudiences().player(actor);
			}

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

	public interface QuesterPlaceholdersContext extends PlaceholdersContext {

		@NotNull
		Quester getQuester();

		@Override
		default @NotNull Audience getAudience() {
			return getQuester();
		}

	}

	public interface PlayerPlaceholdersContext extends QuesterPlaceholdersContext {

		@NotNull
		Player getActor();

		@Override
		@NotNull
		default Quester getQuester() {
			return PlayersManager.getPlayerAccount(getActor());
		}

		@Override
		default @NotNull Audience getAudience() {
			return QuestsPlugin.getPlugin().getAudiences().player(getActor());
		}

	}

}
