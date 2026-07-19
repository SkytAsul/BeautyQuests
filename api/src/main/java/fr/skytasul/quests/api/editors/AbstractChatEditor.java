package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public abstract class AbstractChatEditor extends Editor implements Listener {

	protected AbstractChatEditor(@NotNull Player player, @NotNull Runnable cancel) {
		super(player, cancel);
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

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onChat(AsyncChatEvent event) {
		if (event.getPlayer() != player)
			return;

		event.setCancelled(true);

		String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
		String strippedMessage = LegacyComponentSerializer.legacySection().serialize(event.message());
		String coloredMessage = ChatColorUtils.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', rawMessage));

		QuestsPlugin.getPlugin().getLoggerExpanded().debug("{0} entered ''{1}'' ({2} characters) in an editor ({3})", player.getName(), rawMessage, rawMessage.length(), getClass().getName());
		if (strippedMessage.equalsIgnoreCase("cancel")) {
			cancel();
			return;
		}
		
		player.getScheduler().run(QuestsPlugin.getPlugin(), __ -> {
			boolean success = chat(coloredMessage, strippedMessage);
			if (!success)
				Lang.CHAT_EDITOR.send(player);
		}, null);
	}

}
