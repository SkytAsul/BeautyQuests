package fr.skytasul.quests.api;

import fr.skytasul.quests.api.commands.CommandsManager;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.gui.GuiManager;
import fr.skytasul.quests.api.npcs.BqNpcManager;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.utils.IntegrationManager;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface QuestsPlugin extends Plugin {

	/**
	 * Utility method to get the API object.
	 *
	 * @return the api object
	 */
	public @NotNull QuestsAPI getAPI();

	public @NotNull CommandsManager getCommand();

	public @NotNull QuestsConfiguration getConfiguration();

	public @NotNull PlayersManager getPlayersManager();

	public @NotNull LoggerExpanded getLoggerExpanded();

	public @NotNull GuiManager getGuiManager();

	public @NotNull EditorManager getEditorManager();

	public @NotNull BqNpcManager getNpcManager();

	public @NotNull IntegrationManager getIntegrationManager();

	public void notifyLoadingFailure();

	public void notifySavingFailure();

	public @NotNull BukkitAudiences getAudiences();

	public boolean isRunningPaper();

	/**
	 * Utility method to get the plugin object.
	 *
	 * @return the plugin object
	 */
	public static @NotNull QuestsPlugin getPlugin() {
		return QuestsAPIProvider.getAPI().getPlugin();
	}

}
