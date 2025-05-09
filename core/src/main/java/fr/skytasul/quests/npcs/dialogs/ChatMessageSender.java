package fr.skytasul.quests.npcs.dialogs;

import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.npcs.dialogs.MessageSender;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A simple message sender that displays the message in chat.
 */
public class ChatMessageSender implements MessageSender {

	@Override
	public void displayMessage(@NotNull Player player, @NotNull DialogRunner dialog, @NotNull Message message) {
		String sent = message.formatMessage(player, dialog.getNpc(), dialog.getDialog().getNPCName(dialog.getNpc()),
				dialog.getDialog().getMessages().indexOf(message), dialog.getDialog().getMessages().size());

		MessageUtils.sendRawMessage(player, sent, null,
				PlaceholdersContext.of(player, false, MessageType.DefaultMessageType.UNPREFIXED));
	}

	@Override
	public void stopDisplayingMessages(@NotNull Player player, @NotNull DialogRunner dialog) {
		// intentionnally do nothing
	}

}
