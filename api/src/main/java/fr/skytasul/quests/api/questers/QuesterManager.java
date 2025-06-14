package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.data.SavableData;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;

public interface QuesterManager {

	void registerQuesterProvider(@NotNull QuesterProvider provider);

	@NotNull
	@UnmodifiableView
	Collection<QuesterProvider> getQuesterProviders();

	@NotNull
	QuesterProvider getQuesterProvide(@NotNull Key key) throws IllegalArgumentException;

	void addSavableData(@NotNull SavableData<?> data);

	@NotNull
	Collection<@NotNull SavableData<?>> getSavableData();

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getPlayerQuesters(@NotNull Player player);

	@NotNull
	@UnmodifiableView
	Collection<? extends Quester> getLoadedQuesters();

	void saveAll();

}
