package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.pools.PoolsManageGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.parameters.EntitySelector;
import revxrsal.commands.orphan.OrphanCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CommandsPools implements OrphanCommand {

	@CommandPlaceholder
	@CommandPermission("beautyquests.command.pools")
	public void pools(Player player) {
		PoolsManageGUI.get().open(player);
	}

	@Subcommand("resetPlayer")
	@CommandPermission("beautyquests.command.resetPlayer")
	public void resetPlayerPool(BukkitCommandActor actor, Player player, QuestPool pool, @Switch boolean timer) {
		Quester acc = PlayersManager.getPlayerAccount(player);
		if (timer) {
			pool.resetPlayerTimer(acc);
			Lang.POOL_RESET_TIMER.send(actor.sender(), pool, acc);
		} else {
			pool.resetPlayer(acc).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(__ -> {
				Lang.POOL_RESET_FULL.send(actor.sender(), pool, acc);
			}, "An error occurred while resetting pool " + pool.getId() + " to player " + player.getName(),
					actor.audience().get()));
		}
	}

	@Subcommand("reset")
	@CommandPermission("beautyquests.command.resetQuest")
	public void resetPool(BukkitCommandActor actor, QuestPool pool) {
		List<CompletableFuture<Boolean>> futures = new ArrayList<>(Bukkit.getOnlinePlayers().size());

		for (Player p : Bukkit.getOnlinePlayers()) {
			futures.add(pool.resetPlayer(PlayersManager.getPlayerAccount(p))
					.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(
							"An error occurred while resetting pool " + pool.getId() + " to player " + p.getName(),
							actor.audience().get())));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((__, ___) -> {
			// we do not care about failure or success of this "global" future

			int resetAmount =
					(int) futures.stream().filter(future -> {
						try {
							return !future.isCompletedExceptionally() && future.get();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} catch (ExecutionException ignored) {
							// we already check if the future is completed exceptionnally before using get()
						}
						return false;
					}).count();

			BeautyQuests.getInstance().getPlayersManager().removePoolDatas(pool)
					.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError((Integer removedAmount) -> {
						Lang.POOL_COMPLETELY_RESET.quickSend(actor.sender(), "player_amount",
								removedAmount + resetAmount);
					}, "An error occurred while removing pool datas", actor.audience().get()));
		}).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError());

	}

	@Subcommand("start")
	@CommandPermission("beautyquests.command.pools.start")
	public void start(BukkitCommandActor actor, EntitySelector<Player> players, QuestPool pool) {
		for (Player player : players) {
			Quester acc = PlayersManager.getPlayerAccount(player);
			if (!pool.canGive(player)) {
				Lang.POOL_START_ERROR.send(player, pool, acc);
				return;
			}

			pool.give(player).thenAccept(result -> Lang.POOL_START_SUCCESS.send(actor.sender(), pool, acc,
							PlaceholderRegistry.of("result", result)));
		}
	}

}
