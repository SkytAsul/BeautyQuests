package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.NumberNotInRangeException;
import revxrsal.commands.help.Help;
import revxrsal.commands.orphan.OrphanCommand;
import java.util.List;
import java.util.StringJoiner;

@Description ("Main command for quests")
@CommandPermission ("beautyquests.command")
public class CommandsMisc implements OrphanCommand {

	private static final int COMMANDS_PER_HELP_PAGE = 15;

	@Subcommand ("help")
	public void help(BukkitCommandActor actor,
			@Range(min = 1) @Default("1") int page,
			ExecutableCommand<BukkitCommandActor> thisCommand) {
		List<ExecutableCommand<BukkitCommandActor>> commands = thisCommand.siblingCommands(actor).all().stream()
				.sorted((c1, c2) -> {
					if (!c1.nodes().get(1).isLiteral())
						return -1; // c1 is the top-level command
					if (!c2.nodes().get(1).isLiteral())
						return 1; // c2 is the top-level command
					return c1.path().compareTo(c2.path());
				})
				.toList();

		int numberOfPages = Help.numberOfPages(commands.size(), COMMANDS_PER_HELP_PAGE);
		if (page > numberOfPages)
			throw new NumberNotInRangeException(page, 1, numberOfPages);

		Lang.COMMAND_HELP_HEADER.send(actor.sender(), PlaceholderRegistry.of("page_id", page, "page_count", numberOfPages));

		for (var command : Help.paginate(commands, page, COMMANDS_PER_HELP_PAGE)) {
			String usage = command.usage();
			String path;
			if (command.nodes().get(1).isLiteral()) {
				var literalsJoiner = new StringJoiner(".");
				for (int i = 1; i < command.nodes().size(); i++) { // we skip the first value since it's the command label
					var node = command.nodes().get(i);
					if (!node.isLiteral())
						break;
					literalsJoiner.add(node.name());
				}
				path = literalsJoiner.toString();
			} else {
				// hacky workaround for the [subcommand] parameter
				usage = command.nodes().get(0).name();
				path = "base";
			}
			Lang lang = Lang.getFromPath("command." + path);
			String message = "/" + usage;
			if (lang != null)
				message += ": §e" + lang.getValue();
			MessageUtils.sendMessage(actor.sender(), message, MessageType.DefaultMessageType.UNPREFIXED);
		}
	}

	@Subcommand ("version")
	@CommandPermission ("beautyquests.command.version")
	public String version() {
		return "§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion();
	}

}
