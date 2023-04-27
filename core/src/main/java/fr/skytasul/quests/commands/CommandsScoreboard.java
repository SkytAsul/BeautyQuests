package fr.skytasul.quests.commands;

import org.bukkit.entity.Player;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.scoreboards.Scoreboard;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandsScoreboard implements OrphanCommand {
	
	@Default
	@CommandPermission ("beautyquests.command.scoreboard.toggle")
	public void scoreboardToggle(Player player, ExecutableCommand command, Scoreboard scoreboard, @Optional String subcommand) {
		if (subcommand != null) throw new revxrsal.commands.exception.InvalidSubcommandException(command.getPath(), subcommand);
		if (scoreboard.isForceHidden()) {
			scoreboard.show(true);
			Lang.COMMAND_SCOREBOARD_OWN_SHOWN.send(player);
		}else {
			scoreboard.hide(true);
			Lang.COMMAND_SCOREBOARD_OWN_HIDDEN.send(player);
		}
	}
	
	@Subcommand ("setline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void setline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line, String text) {
		scoreboard.setCustomLine(line, text);
		Lang.COMMAND_SCOREBOARD_LINESET.send(actor.getSender(), line);
	}
	
	@Subcommand ("removeline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void removeline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line) {
		if (scoreboard.removeLine(line)) {
			Lang.COMMAND_SCOREBOARD_LINEREMOVE.send(actor.getSender(), line);
		}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(actor.getSender(), line);
	}
	
	@Subcommand ("resetline")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void resetline(BukkitCommandActor actor, Player player, Scoreboard scoreboard, @Range (min = 0) int line) {
		if (scoreboard.resetLine(line)) {
			Lang.COMMAND_SCOREBOARD_LINERESET.send(actor.getSender(), line);
		}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(actor.getSender(), line);
	}
	
	@Subcommand ("resetall")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void resetall(BukkitCommandActor actor, Player player) {
		BeautyQuests.getInstance().getScoreboardManager().removePlayerScoreboard(player);
		BeautyQuests.getInstance().getScoreboardManager().create(player);
		Lang.COMMAND_SCOREBOARD_RESETALL.send(actor.getSender(), player.getName());
	}
	
	@Subcommand ("show")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void show(BukkitCommandActor actor, Player player, Scoreboard scoreboard) {
		scoreboard.show(true);
		Lang.COMMAND_SCOREBOARD_SHOWN.send(actor.getSender(), player.getName());
	}
	
	@Subcommand ("hide")
	@CommandPermission ("beautyquests.command.scoreboard")
	public void hide(BukkitCommandActor actor, Player player, Scoreboard scoreboard) {
		scoreboard.hide(true);
		Lang.COMMAND_SCOREBOARD_HIDDEN.send(actor.getSender(), player.getName());
	}
	
}