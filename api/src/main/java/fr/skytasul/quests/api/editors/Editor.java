package fr.skytasul.quests.api.editors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.ChatColorUtils;

@AutoRegistered
public abstract class Editor {
	
	protected final @NotNull Player player;
	protected final @NotNull Runnable cancel;
	private boolean started = false;
	
	protected Editor(@NotNull Player player, @NotNull Runnable cancel) {
		this.player = player;
		this.cancel = cancel;
	}
	
	public @NotNull Player getPlayer() {
		return player;
	}

	public void begin() {
		if (started)
			throw new IllegalStateException("Editor already started");

		started = true;
	}

	public void end() {
		if (!started)
			throw new IllegalStateException("Editor did not started");

		started = false;
	}
	
	public final void start() {
		QuestsPlugin.getPlugin().getEditorManager().start(this);
	}

	public final void stop() {
		QuestsPlugin.getPlugin().getEditorManager().stop(this);
	}

	public final void cancel() {
		cancel.run();
		QuestsPlugin.getPlugin().getEditorManager().stop(this);
	}
	
	/**
	 * Happens when the player in the editor type somthing in the chat
	 * @param coloredMessage Message typed
	 * @param strippedMessage Message without default colors
	 * @return false if the plugin needs to send an help message to the player
	 */
	public boolean chat(String coloredMessage, String strippedMessage) {
		return false;
	}
	
	public final void callChat(String rawText) {
		rawText = rawText.trim().replaceAll("\\uFEFF", ""); // remove blank characters, remove space at the beginning
		QuestsPlugin.getPlugin().getLoggerExpanded().debug(player.getName() + " entered \"" + rawText + "\" ("
				+ rawText.length() + " characters) in an editor. (name: " + getClass().getName() + ")");
		String coloredMessage = ChatColorUtils.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', rawText));
		String strippedMessage = ChatColor.stripColor(rawText);
		if (strippedMessage.equalsIgnoreCase(cancelWord())) {
			cancel();
		}else if (!chat(coloredMessage, strippedMessage)) {
			Lang.CHAT_EDITOR.send(player);
		}
	}
	
	protected String cancelWord(){
		return null;
	}

}
