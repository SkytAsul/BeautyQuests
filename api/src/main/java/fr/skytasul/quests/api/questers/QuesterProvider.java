package fr.skytasul.quests.api.questers;

import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.Optional;

public interface QuesterProvider extends Keyed {

	// Really useful?
	// @NotNull Optional<Quester> getQuester(@NotNull String identifier);

	@NotNull
	default Optional<String> getQuesterName(@NotNull String identifier) {
		return Optional.empty();
	}

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getPlayerQuesters(@NotNull Player player);

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getLoadedQuesters();

}
