package fr.skytasul.quests.structure.pools;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.pools.QuestPoolData;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.options.OptionQuestPool;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestPoolsManagerImplementation implements QuestPoolsManager {

	private final @NotNull BeautyQuests plugin;
	private final @NotNull File file;
	private final @NotNull YamlConfiguration config;

	private Map<Integer, QuestPoolControllerImplementation> pools = new HashMap<>();

	public QuestPoolsManagerImplementation(@NotNull BeautyQuests plugin, @NotNull File file) throws IOException {
		this.plugin = plugin;
		this.file = file;
		if (!file.exists()) {
			config = new YamlConfiguration();
			config.options().copyHeader(true);
			config.options().header("This file describes configuration of the different quest pools. See \"/quests pool\".");
			config.save(file);
		} else {
			config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false)) {
				try {
					int id = Integer.parseInt(key);
					var poolData = QuestPoolData.deserialize(config.getConfigurationSection(key));
					pools.put(id, new QuestPoolControllerImplementation(id, poolData));
				} catch (Exception ex) {
					plugin.getLoggerExpanded().severe("An exception ocurred while loading quest pool {0}", ex, key);
					continue;
				}
			}
			plugin.getLoggerExpanded().debug("Loaded {0} pools.", pools.size());
		}
	}

	private void save(@NotNull QuestPoolControllerImplementation pool) {
		ConfigurationSection section = config.createSection(Integer.toString(pool.getId()));
		pool.getPoolData().save(section);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateAll() throws IOException {
		for (var pool : pools.values()) {
			pool.getPoolData().save(config.createSection(Integer.toString(pool.getId())));
		}
		config.save(file);
	}

	@Override
	public @NotNull QuestPoolController registerPool(@NotNull QuestPoolData pool) {
		int id = pools.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;

		var controller = new QuestPoolControllerImplementation(id, pool);
		save(controller);
		pools.put(id, controller);

		return controller;
	}

	@Override
	public @NotNull QuestPoolController editPool(int id, @NotNull QuestPoolData newPool) {
		var existing = Objects.requireNonNull(pools.get(id));
		existing.unload();

		var controller = new QuestPoolControllerImplementation(id, newPool);
		save(controller);
		pools.put(id, controller);

		controller.quests = existing.quests;
		controller.quests.forEach(quest -> quest.getOption(OptionQuestPool.class).setValue(controller));

		return controller;
	}

	@Override
	public void removePool(int id) {
		var pool = pools.remove(id);
		if (pool == null)
			return;
		pool.unload();
		new ArrayList<>(pool.quests).forEach(quest -> quest.removeOption(OptionQuestPool.class)); // prevents concurrent
		config.set(Integer.toString(id), null);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public @Nullable QuestPoolControllerImplementation getPool(int id) {
		return pools.get(id);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public @NotNull @UnmodifiableView Collection<QuestPoolControllerImplementation> getPools() {
		return pools.values();
	}

}
