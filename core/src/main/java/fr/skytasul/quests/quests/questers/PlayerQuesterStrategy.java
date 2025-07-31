package fr.skytasul.quests.quests.questers;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy;
import fr.skytasul.quests.api.serializable.SerializableObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class PlayerQuesterStrategy extends QuestQuesterStrategy {

	private final PlayerManager playerManager;

	public PlayerQuesterStrategy(@NotNull PlayerManager playerManager) {
		super(QuestsAPI.getAPI().getQuestQuesterStrategyRegistry());
		this.playerManager = playerManager;
	}

	@Override
	public @NotNull Optional<? extends Quester> getPlayerQuester(@NotNull Player player) {
		return Optional.of(playerManager.getQuester(player));
	}

	@Override
	public boolean isQuesterApplicable(@NotNull Quester quester) {
		return quester.getProvider() == playerManager;
	}

	@Override
	public boolean shouldAllPlayersMatchRequirements() {
		return false;
	}

	@Override
	public @Nullable String getDescription() {
		return null; // default strategy: nothing should be shown, it's intuitive
	}

	@Override
	public @NotNull SerializableObject clone() {
		return this;
	}

	@Override
	public void save(@NotNull ConfigurationSection section) {}

	@Override
	public void load(@NotNull ConfigurationSection section) {}

}
