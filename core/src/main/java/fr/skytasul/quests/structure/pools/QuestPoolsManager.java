package fr.skytasul.quests.structure.pools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.options.OptionQuestPool;

public class QuestPoolsManager {
	
	private File file;
	private YamlConfiguration config;
	
	private Map<Integer, QuestPool> pools = new HashMap<>();
	
	public QuestPoolsManager(File file) throws IOException {
		this.file = file;
		if (!file.exists()) {
			config = new YamlConfiguration();
			config.options().copyHeader(true);
			config.options().header("This file describes configuration of the different quest pools. See \"/quests pool\".");
			config.save(file);
		}else {
			config = YamlConfiguration.loadConfiguration(file);
			for (String key : config.getKeys(false)) {
				try {
					int id = Integer.parseInt(key);
					QuestPool pool = QuestPool.deserialize(id, config.getConfigurationSection(key));
					pools.put(id, pool);
				}catch (Exception ex) {
					BeautyQuests.logger.severe("An exception ocurred while loading quest pool " + key);
					ex.printStackTrace();
					continue;
				}
			}
		}
	}
	
	public void save(QuestPool pool) {
		ConfigurationSection section = config.createSection(Integer.toString(pool.getID()));
		pool.save(section);
		try {
			config.save(file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public QuestPool createPool(QuestPool editing, int npcID, String hologram, int maxQuests, int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates, List<AbstractRequirement> requirements) {
		if (editing != null) editing.unload();
		QuestPool pool = new QuestPool(editing == null ? pools.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1 : editing.getID(), npcID, hologram, maxQuests, questsPerLaunch, redoAllowed, timeDiff, avoidDuplicates, requirements);
		save(pool);
		pools.put(pool.getID(), pool);
		if (editing != null) {
			pool.quests = editing.quests;
			pool.quests.forEach(quest -> quest.getOption(OptionQuestPool.class).setValue(pool));
		}
		return pool;
	}
	
	public void removePool(int id) {
		QuestPool pool = pools.remove(id);
		if (pool == null) return;
		pool.unload();
		new ArrayList<>(pool.quests).forEach(quest -> quest.removeOption(OptionQuestPool.class)); // prevents concurrent
		config.set(Integer.toString(id), null);
		try {
			config.save(file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public QuestPool getPool(int id) {
		return pools.get(id);
	}
	
	public Collection<QuestPool> getPools() {
		return pools.values();
	}
	
}
