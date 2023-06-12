package fr.skytasul.quests.structure.pools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.pools.QuestPoolsManager;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.options.OptionQuestPool;

public class QuestPoolsManagerImplementation implements QuestPoolsManager {

	private File file;
	private YamlConfiguration config;

	private Map<Integer, QuestPoolImplementation> pools = new HashMap<>();

	public QuestPoolsManagerImplementation(File file) throws IOException {
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
					QuestPoolImplementation pool = QuestPoolImplementation.deserialize(id, config.getConfigurationSection(key));
					pools.put(id, pool);
				} catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("An exception ocurred while loading quest pool " + key, ex);
					continue;
				}
			}
		}
	}

	public void save(@NotNull QuestPoolImplementation pool) {
		ConfigurationSection section = config.createSection(Integer.toString(pool.getId()));
		pool.save(section);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public @NotNull QuestPoolImplementation createPool(@Nullable QuestPool editing, int npcID, @Nullable String hologram,
			int maxQuests,
			int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates,
			@NotNull RequirementList requirements) {

		if (editing != null)
			((QuestPoolImplementation) editing).unload();

		QuestPoolImplementation pool = new QuestPoolImplementation(
				editing == null ? pools.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1 : editing.getId(),
				npcID, hologram, maxQuests, questsPerLaunch, redoAllowed, timeDiff, avoidDuplicates, requirements);
		save(pool);
		pools.put(pool.getId(), pool);
		if (editing != null) {
			pool.quests = editing.getQuests();
			pool.quests.forEach(quest -> quest.getOption(OptionQuestPool.class).setValue(pool));
		}
		return pool;
	}

	@Override
	public void removePool(int id) {
		QuestPoolImplementation pool = pools.remove(id);
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
	public @Nullable QuestPoolImplementation getPool(int id) {
		return pools.get(id);
	}

	@Override
	public @NotNull @UnmodifiableView Collection<QuestPool> getPools() {
		return (Collection) pools.values();
	}

}
