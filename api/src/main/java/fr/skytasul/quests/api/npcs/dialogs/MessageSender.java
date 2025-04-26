package fr.skytasul.quests.api.npcs.dialogs;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MessageSender {

	/**
	 * Displays a message to a player. If there was already a message being displayed for this dialog,
	 * then this should stop displaying it.
	 *
	 * @param player Player to display the message to
	 * @param dialog Dialog from which the message is
	 * @param message Message to display
	 */
	void displayMessage(@NotNull Player player, @NotNull DialogRunner dialog, @NotNull Message message);

	/**
	 * Stop displaying the messages from a dialog.
	 *
	 * @param player Player who received the messages
	 * @param dialog Dialog from which the messages were
	 */
	void stopDisplayingMessages(@NotNull Player player, @NotNull DialogRunner dialog);

}
