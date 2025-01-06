package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.data.SavableData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;

public interface QuesterManager {

	void registerQuesterProvider(@NotNull QuesterProvider provider);

	@NotNull
	@UnmodifiableView
	Collection<QuesterProvider> getQuesterProviders();

	void addSavableData(@NotNull SavableData<?> data);

	@NotNull
	Collection<@NotNull SavableData<?>> getSavableData();

	@NotNull
	@UnmodifiableView
	Collection<Quester> getPlayerQuesters(@NotNull Player player);

}
