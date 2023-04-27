package fr.skytasul.quests.api;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.commands.CommandsManager;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.gui.GuiManager;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;

public interface QuestsPlugin extends Plugin {

	public @NotNull QuestsAPI getAPI();

	public @NotNull CommandsManager getCommand();
	
	public @NotNull QuestsConfiguration getConfiguration();
	
	public @NotNull PlayersManager getPlayersManager();

	public @NotNull LoggerExpanded getLoggerExpanded();
	
	public @NotNull GuiManager getGuiManager();

	public @NotNull EditorManager getEditorManager();

	public @NotNull String getPrefix(); // TODO maybe not necessary

	public void notifyLoadingFailure();

	public void noticeSavingFailure();

	public static @NotNull QuestsPlugin getPlugin() {
		return QuestsAPIProvider.getAPI().getPlugin();
	}

}
