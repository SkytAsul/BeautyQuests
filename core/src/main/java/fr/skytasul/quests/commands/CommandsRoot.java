package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Command;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Description;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.help.CommandHelp;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;

@Command ({ "quests", "quest", "bq", "beautyquests", "bquests" })
@Description ("Main command for quests")
@CommandPermission ("beautyquests.command")
public class CommandsRoot {
	
	@Subcommand ("help")
	public void help(BukkitCommandActor actor, CommandHelp<String> helpEntries) {
		Lang.COMMAND_HELP.send(actor.getSender());
		helpEntries.forEach(help -> MessageUtils.sendMessage(actor.getSender(), help, MessageType.DefaultMessageType.UNPREFIXED));
	}
	
	@Subcommand ("version")
	@CommandPermission ("beautyquests.command.version")
	public String version() {
		return "§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion();
	}
	
}
