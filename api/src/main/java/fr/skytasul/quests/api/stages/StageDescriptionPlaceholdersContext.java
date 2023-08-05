package fr.skytasul.quests.api.stages;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.PlayerPlaceholdersContext;

public interface StageDescriptionPlaceholdersContext extends PlaceholdersContext, PlayerPlaceholdersContext {

	@Override
	@NotNull
	PlayerAccount getPlayerAccount();

	@NotNull
	DescriptionSource getDescriptionSource();

	static @NotNull StageDescriptionPlaceholdersContext of(boolean replacePluginPlaceholders, @NotNull PlayerAccount account,
			@NotNull DescriptionSource source) {
		return new StageDescriptionPlaceholdersContext() {

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
		};
	}

}
