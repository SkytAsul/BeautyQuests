package fr.skytasul.quests.npcs.dialogs;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.npcs.dialogs.MessageSender;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class ActionBarMessageSender implements MessageSender {

	private final Map<Player, BukkitTask> players = new HashMap<>();

	@Override
	public void displayMessage(@NotNull Player player, @NotNull DialogRunner dialog, @NotNull Message message) {
		if (players.containsKey(player))
			players.remove(player).cancel();

		String sent = message.formatMessage(player, dialog.getNpc(), dialog.getDialog().getNPCName(dialog.getNpc()),
				dialog.getDialog().getMessages().indexOf(message), dialog.getDialog().getMessages().size());

		BaseComponent[] components = TextComponent.fromLegacyText(sent.replace("{nl}", " "));
		if (message.getWaitTime() > 60) {
			players.put(player, new BukkitRunnable() {
				int time = 0;

				@Override
				public void run() {
					if (!player.isOnline() || time >= message.getWaitTime()) {
						cancel();
						players.remove(player);
						return;
					}

					player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
					time += 40;
				}
			}.runTaskTimerAsynchronously(QuestsPlugin.getPlugin(), 0, 40));
		}
	}

	@Override
	public void stopDisplayingMessages(@NotNull Player player, @NotNull DialogRunner dialog) {
		BukkitTask task = players.remove(player);
		if (task != null) {
			task.cancel();
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
		}
	}

}
