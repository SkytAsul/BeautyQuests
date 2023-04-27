package fr.skytasul.quests.commands;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.gui.pools.PoolsManageGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.BukkitCommandActor;
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
	public void resetPlayerPool(BukkitCommandActor actor, Player player, QuestPoolImplementation pool, @Switch boolean timer) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		if (timer) {
			pool.resetPlayerTimer(acc);
			Lang.POOL_RESET_TIMER.send(actor.getSender(), pool.getId(), player.getName());
		} else {
			pool.resetPlayer(acc).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(__ -> {
				Lang.POOL_RESET_FULL.send(actor.getSender(), pool.getId(), player.getName());
			}, "An error occurred while resetting pool " + pool.getId() + " to player " + player.getName(),
					actor.getSender()));
		}
	}

	@Subcommand("start")
	@CommandPermission("beautyquests.command.pools.start")
	public void start(BukkitCommandActor actor, Player player, QuestPoolImplementation pool) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);

		if (!pool.canGive(player, acc)) {
			Lang.POOL_START_ERROR.send(player, pool.getId(), player.getName());
			return;
		}

		pool.give(player).thenAccept(result -> Lang.POOL_START_SUCCESS.send(player, pool.getId(), player.getName(), result));
	}

}
