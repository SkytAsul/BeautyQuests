package fr.skytasul.quests.api.questers;

import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;

public interface QuesterProvider extends Keyed {

	// Really useful?
	// @NotNull Optional<Quester> getQuester(@NotNull String identifier);

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getPlayerQuesters(@NotNull Player player);

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getLoadedQuesters();

}
