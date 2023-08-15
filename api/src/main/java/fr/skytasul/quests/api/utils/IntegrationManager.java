package fr.skytasul.quests.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import fr.skytasul.quests.api.QuestsPlugin;

public class IntegrationManager implements Listener {

	private List<BQDependency> dependencies;
	private boolean dependenciesTested = false;
	private boolean dependenciesInitialized = false;
	private boolean lockDependencies = false;

	public List<BQDependency> getDependencies() {
		return dependencies;
	}

	public void addDependency(BQDependency dependency) {
		if (lockDependencies) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("Trying to add a BQ dependency for plugin " + dependency.pluginNames + " after final locking.");
			return;
		}
		dependencies.add(dependency);
		if (dependenciesTested) {
			if (dependency.testCompatibility(true) && dependenciesInitialized)
				dependency.initialize();
		}
	}

	public void testCompatibilities() {
		if (dependenciesTested)
			return;
		dependencies.forEach(x -> x.testCompatibility(false));
		dependenciesTested = true;
	}

	public void initializeCompatibilities() {
		if (dependenciesInitialized)
			return;
		dependencies.stream().filter(BQDependency::isEnabled).forEach(BQDependency::initialize);
		dependenciesInitialized = true;
	}

	public void disableCompatibilities() {
		dependencies.forEach(BQDependency::disable);
	}

	public void lockDependencies() {
		lockDependencies = true;
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent e) {
		if (lockDependencies)
			return;
		// if (dependenciesTested) return;
		dependencies.stream().filter(x -> !x.enabled && x.isPlugin(e.getPlugin())).findAny().ifPresent(dependency -> {
			if (dependency.testCompatibility(true) && dependenciesInitialized)
				dependency.initialize();
		});
	}

	public static class BQDependency {
		private final List<String> pluginNames;
		private final Runnable initialize;
		private final Runnable disable;
		private final Predicate<Plugin> isValid;
		private boolean enabled = false;
		private boolean forceDisable = false;
		private boolean initialized = false;
		private Plugin foundPlugin;

		public BQDependency(String pluginName) {
			this(pluginName, null);
		}

		public BQDependency(String pluginName, Runnable initialize) {
			this(pluginName, initialize, null, null);
		}

		public BQDependency(String pluginName, Runnable initialize, Runnable disable) {
			this(pluginName, initialize, disable, null);
		}

		public BQDependency(String pluginName, Runnable initialize, Runnable disable, Predicate<Plugin> isValid) {
			Validate.notNull(pluginName);
			this.pluginNames = new ArrayList<>();
			this.pluginNames.add(pluginName);
			this.initialize = initialize;
			this.disable = disable;
			this.isValid = isValid;
		}

		public BQDependency addPluginName(String name) {
			pluginNames.add(name);
			return this;
		}

		boolean isPlugin(Plugin plugin) {
			return pluginNames.contains(plugin.getName());
		}

		boolean testCompatibility(boolean after) {
			if (forceDisable)
				return false;
			Plugin plugin = pluginNames.stream().map(Bukkit.getPluginManager()::getPlugin)
					.filter(x -> x != null && x.isEnabled()).findAny().orElse(null);
			if (plugin == null)
				return false;
			if (isValid != null && !isValid.test(plugin))
				return false;
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Hooked into " + pluginNames + " v"
					+ plugin.getDescription().getVersion() + (after ? " after primary initialization" : ""));
			enabled = true;
			foundPlugin = plugin;
			return true;
		}

		void initialize() {
			try {
				if (initialize != null)
					initialize.run();
				initialized = true;
			} catch (Throwable ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while initializing " + pluginNames.toString() + " integration", ex);
				enabled = false;
			}
		}

		public void disable() {
			forceDisable = true;
			if (enabled) {
				enabled = false;
				if (disable != null && initialized)
					disable.run();
				initialized = false;
			}
		}

		public boolean isEnabled() {
			return enabled;
		}

		public Plugin getFoundPlugin() {
			if (!enabled)
				throw new IllegalStateException(
						"The dependency " + pluginNames + " is not enabled");
			return foundPlugin;
		}

	}

}
