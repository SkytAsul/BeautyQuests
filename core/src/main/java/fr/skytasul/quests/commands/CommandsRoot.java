package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.MessageUtils;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.help.CommandHelp;

@Command ({ "quests", "quest", "bq", "beautyquests", "bquests" })
@Description ("Main command for quests")
@CommandPermission ("beautyquests.command")
public class CommandsRoot {
	
	@Subcommand ("help")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries) {
		Lang.COMMAND_HELP.sendWP(actor.getSender());
		helpEntries.forEach(help -> MessageUtils.sendUnprefixedMessage(actor.getSender(), help));
	}
	
	@Subcommand ("version")
	@CommandPermission ("beautyquests.command.version")
	public String version() {
		return "§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion();
	}
	
}
