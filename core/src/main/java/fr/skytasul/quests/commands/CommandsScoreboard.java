package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Default;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Optional;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Range;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.command.ExecutableCommand;
import fr.skytasul.quests.api.commands.revxrsal.exception.InvalidSubcommandException;
import fr.skytasul.quests.api.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.scoreboards.Scoreboard;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.EntitySelector;

public class CommandsScoreboard implements OrphanCommand {

	@Default
	@CommandPermission ("beautyquests.command.scoreboard.toggle")
	public void scoreboardToggle(BukkitCommandActor actor, ExecutableCommand command, @Optional String subcommand) {
		if (subcommand != null)
			throw new InvalidSubcommandException(command.getPath(), subcommand);

		Scoreboard scoreboard = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(actor.requirePlayer());
		if (scoreboard.isForceHidden()) {
			scoreboard.show(true);
			Lang.COMMAND_SCOREBOARD_OWN_SHOWN.send(actor.getSender());
		}else {
			scoreboard.hide(true);
			Lang.COMMAND_SCOREBOARD_OWN_HIDDEN.send(actor.getSender());
		}
	}

	private PlaceholderRegistry getLineRegistry(int line) {
		return PlaceholderRegistry.of("line_id", line);
	}

	private PlaceholderRegistry getPlayerRegistry(Player player) {
		return PlaceholderRegistry.of("player_name", player.getName());
	}

	@Subcommand ("setline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void setline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line, String text) {
		scoreboard.setCustomLine(line, text);
		Lang.COMMAND_SCOREBOARD_LINESET.send(actor.getSender(), getLineRegistry(line));
	}

	@Subcommand ("removeline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void removeline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line) {
		if (scoreboard.removeLine(line)) {
			Lang.COMMAND_SCOREBOARD_LINEREMOVE.send(actor.getSender(), getLineRegistry(line));
		} else
			Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(actor.getSender(), getLineRegistry(line));
	}

	@Subcommand ("resetline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void resetline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line) {
		if (scoreboard.resetLine(line)) {
			Lang.COMMAND_SCOREBOARD_LINERESET.send(actor.getSender(), getLineRegistry(line));
		} else
			Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(actor.getSender(), getLineRegistry(line));
	}

	@Subcommand ("resetall")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void resetall(BukkitCommandActor actor, Player player) {
		BeautyQuests.getInstance().getScoreboardManager().removePlayerScoreboard(player);
		BeautyQuests.getInstance().getScoreboardManager().create(player);
		Lang.COMMAND_SCOREBOARD_RESETALL.send(actor.getSender(), getPlayerRegistry(player));
	}

	@Subcommand ("show")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void show(BukkitCommandActor actor, EntitySelector<Player> players) {
		for (Player player : players) {
			Scoreboard scoreboard = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(player);
			scoreboard.show(true);
			Lang.COMMAND_SCOREBOARD_SHOWN.send(actor.getSender(), getPlayerRegistry(player));
		}
	}

	@Subcommand ("hide")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void hide(BukkitCommandActor actor, EntitySelector<Player> players) {
		for (Player player : players) {
			Scoreboard scoreboard = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(player);
			scoreboard.hide(true);
			Lang.COMMAND_SCOREBOARD_HIDDEN.send(actor.getSender(), getPlayerRegistry(player));
		}
	}

}