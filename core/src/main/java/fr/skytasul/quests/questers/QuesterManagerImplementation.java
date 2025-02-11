package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.QuesterProvider;
import fr.skytasul.quests.api.utils.DataSavingException;
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

	private boolean lockData = false;

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

	public void lockData() {
		this.lockData = true;
	}

	@Override
	public void addSavableData(@NotNull SavableData<?> data) {
		if (lockData)
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
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Saving questers...");

		for (var provider : providers.values()) {
			for (var quester : provider.getLoadedQuesters()) {
				try {
					quester.getDataHolder().save();
				} catch (DataSavingException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to save data of {}", ex,
							quester.getDetailedName());
				}
			}
		}

		try {
			dataManager.save();
		} catch (DataSavingException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to save data manager.", ex);
		}
	}

}
