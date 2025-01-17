package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.QuesterProvider;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;

public class QuesterManagerImplementation implements QuesterManager {

	private static final List<String> FORBIDDEN_DATA_ID = List.of("identifier", "quests", "pools");

	private final @NotNull Set<@NotNull SavableData<?>> savableData = new HashSet<>();
	private final @NotNull Map<@NotNull Key, @NotNull QuesterProvider> providers = new HashMap<>();

	private @NotNull QuesterDataManager dataManager;

	public QuesterManagerImplementation(@NotNull QuesterDataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void registerQuesterProvider(@NotNull QuesterProvider provider) {
		this.providers.put(provider.key(), provider);
	}

	@Override
	public @NotNull @UnmodifiableView Collection<QuesterProvider> getQuesterProviders() {
		return providers.values();
	}

	public @NotNull QuesterDataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(@NotNull QuesterDataManager dataManager) {
		this.dataManager = Objects.requireNonNull(dataManager);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Using {} as the new data manager",
				dataManager.getClass().getSimpleName());
	}

	@Override
	public void addSavableData(@NotNull SavableData<?> data) {
		if (loaded)
			throw new IllegalStateException("Cannot add account data after players manager has been loaded");
		if (FORBIDDEN_DATA_ID.contains(data.getId()))
			throw new IllegalArgumentException("Forbidden account data id " + data.getId());
		if (savableData.stream().anyMatch(x -> x.getId().equals(data.getId())))
			throw new IllegalArgumentException("Another account data already exists with the id " + data.getId());
		if (data.getDataType().isPrimitive())
			throw new IllegalArgumentException("Primitive account data types are not supported");
		savableData.add(data);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Registered quester data " + data.getId());
	}

	@Override
	public @NotNull Collection<@NotNull SavableData<?>> getSavableData() {
		return savableData;
	}

	@Override
	public @NotNull @UnmodifiableView Collection<? extends Quester> getPlayerQuesters(@NotNull Player player) {
		return providers.values().stream().flatMap(provider -> provider.getPlayerQuesters(player).stream()).toList();
	}

	@Override
	public @NotNull @UnmodifiableView Collection<? extends Quester> getLoadedQuesters() {
		return providers.values().stream().flatMap(provider -> provider.getLoadedQuesters().stream()).toList();
	}

	@Override
	public void saveAll() {
		for (var provider : providers.values()) {
			for (var quester : provider.getLoadedQuesters()) {

			}
		}
	}

}
