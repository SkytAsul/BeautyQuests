package fr.skytasul.quests.commands;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.pools.PoolsManageGUI;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandsPools implements OrphanCommand {

	@Default
	@CommandPermission("beautyquests.command.pools")
	public void pools(Player player) {
		PoolsManageGUI.get().open(player);
	}

	@Subcommand("resetPlayer")
	@CommandPermission("beautyquests.command.resetPlayer")
	public void resetPlayerPool(BukkitCommandActor actor, Player player, QuestPool pool, @Switch boolean timer) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		if (timer) {
			pool.resetPlayerTimer(acc);
			Lang.POOL_RESET_TIMER.send(actor.getSender(), pool, acc);
		} else {
			pool.resetPlayer(acc).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(__ -> {
				Lang.POOL_RESET_FULL.send(actor.getSender(), pool, acc);
			}, "An error occurred while resetting pool " + pool.getId() + " to player " + player.getName(),
					actor.getSender()));
		}
	}

	@Subcommand("start")
	@CommandPermission("beautyquests.command.pools.start")
	public void start(BukkitCommandActor actor, EntitySelector<Player> players, QuestPool pool) {
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);
			if (!pool.canGive(player)) {
				Lang.POOL_START_ERROR.send(player, pool, acc);
				return;
			}

			pool.give(player).thenAccept(result -> Lang.POOL_START_SUCCESS.send(actor.getSender(), pool, acc,
							PlaceholderRegistry.of("result", result)));
		}
	}

}
