package fr.skytasul.quests.api.questers;

import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.Optional;

public interface QuesterProvider extends Keyed {

	@NotNull
	Optional<Quester> getQuester(@NotNull String identifier);

	@NotNull
	@UnmodifiableView
	Collection<Quester> getPlayerQuesters(@NotNull Player player);

}
