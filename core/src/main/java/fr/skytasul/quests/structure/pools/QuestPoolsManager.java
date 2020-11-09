package fr.skytasul.quests.structure.pools;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.skytasul.quests.BeautyQuests;

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
					QuestPool pool = new QuestPool(id);
					pool.load(config.getConfigurationSection(key));
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
	
	public QuestPool getPool(int id) {
		return pools.get(id);
	}
	
	public Collection<QuestPool> getPools() {
		return pools.values();
	}
	
}
