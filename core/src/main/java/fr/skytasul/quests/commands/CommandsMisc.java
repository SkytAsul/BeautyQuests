package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help;
import revxrsal.commands.orphan.OrphanCommand;
import java.util.ArrayList;

@Description ("Main command for quests")
@CommandPermission ("beautyquests.command")
public class CommandsMisc implements OrphanCommand {

	@Subcommand ("help")
	public void help(BukkitCommandActor actor, ExecutableCommand<BukkitCommandActor> executedCommand,
			Help.RelatedCommands<BukkitCommandActor> commands) {
		Lang.COMMAND_HELP.send(actor.sender());

		var list = new ArrayList<Lang>();

		for (var command : commands) {
			if (!command.isVisibleTo(actor))
				continue;

			String subcommandLabel = "ERROR";
			for (var node : command.nodes()) {
				if (node.isLiteral()) {
					subcommandLabel = node.name();
				} else
					break;
			}

			for (Lang lang : Lang.values()) {
				if (lang.getPath().startsWith("msg.command.help.")) {
					list.add(lang);
					String cmdKey = lang.getPath().substring(17);
					if (cmdKey.equalsIgnoreCase(subcommandLabel)) {
						list.remove(lang);
						String helpString = lang.quickFormat("label", command.firstNode().name());
						MessageUtils.sendMessage(actor.sender(), helpString, MessageType.DefaultMessageType.UNPREFIXED);
						break;
					}
				}
			}
		}

		for (Lang lang : list) {
			// temporary, until refactoring of the help system TODO make use of automatic usage generation
			String helpString = lang.quickFormat("label", executedCommand.firstNode().name());
			MessageUtils.sendMessage(actor.sender(), helpString, MessageType.DefaultMessageType.UNPREFIXED);
		}
	}

	@Subcommand ("version")
	@CommandPermission ("beautyquests.command.version")
	public String version() {
		return "§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion();
	}

}
