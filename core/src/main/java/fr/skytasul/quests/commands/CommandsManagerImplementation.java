package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.commands.CommandsManager;
import fr.skytasul.quests.api.commands.OutsideEditor;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.commands.parameters.BqNpcParameter;
import fr.skytasul.quests.commands.parameters.QuestParameter;
import fr.skytasul.quests.commands.parameters.QuestPoolParameter;
import fr.skytasul.quests.scoreboards.Scoreboard;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;
import java.util.Arrays;

public class CommandsManagerImplementation implements CommandsManager {

	private static String[] COMMAND_ALIASES = {"quests", "quest", "bq", "beautyquests", "bquests"};

	private Lamp<BukkitCommandActor> lamp;

	public CommandsManagerImplementation() {
		var builder = BukkitLamp.builder(BeautyQuests.getInstance());
		// handler.failOnTooManyArguments();

		builder.parameterTypes(parameters -> {
			parameters.addParameterType(Quest.class, new QuestParameter());
			parameters.addParameterType(QuestPool.class, new QuestPoolParameter());
			parameters.addParameterType(BqNpc.class, new BqNpcParameter());

			parameters.addContextParameter(Scoreboard.class, (parameter, context) -> {
				return BeautyQuests.getInstance().getScoreboardManager()
						.getPlayerScoreboard(context.getResolvedArgument(Player.class));
			});
		});

		builder.commandCondition(context -> {
			if (!context.command().annotations().contains(OutsideEditor.class))
				return;
			if (!context.actor().isPlayer())
				return;
			boolean inGui = QuestsPlugin.getPlugin().getGuiManager().hasGuiOpened(context.actor().asPlayer());
			boolean inEditor = QuestsPlugin.getPlugin().getEditorManager().isInEditor(context.actor().asPlayer());
			if (inGui || inEditor)
				throw new CommandErrorException(Lang.ALREADY_EDITOR.toString());
		});

		builder.defaultMessageSender((actor, msg) -> {
			MessageUtils.sendMessage(actor.sender(), msg, MessageType.DefaultMessageType.PREFIXED);
		});

		builder.responseHandler(String.class, (msg, context) -> context.actor().reply(msg));

		builder.hooks().onCommandExecuted((command, context, cancel) -> {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.debug(context.actor().name() + " executed command: " + context.input().source()); // TODO test
		});

		lamp = builder.build();
	}

	public void initializeCommands() {
		registerCommands("", new CommandsMisc(), new CommandsAdmin(), new CommandsPlayer(), new CommandsPlayerManagement());
		registerCommands("scoreboard", new CommandsScoreboard());
		registerCommands("pools", new CommandsPools());
	}

	@Override
	public @NotNull Lamp<BukkitCommandActor> getHandler() {
		return lamp;
	}

	@Override
	public void registerCommands(String subpath, OrphanCommand... commands) {
		Orphans path;
		if (subpath == null || subpath.isEmpty()) {
			path = Orphans.path(COMMAND_ALIASES);
		}else {
			path = Orphans.path(Arrays.stream(COMMAND_ALIASES).map(x -> x + " " + subpath).toArray(String[]::new));
		}
		for (var cmd : commands) {
			lamp.register(path.handler(cmd));
		}
		lamp.register(Arrays.stream(commands).map(path::handler).toArray());
		// if (locked) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Registered commands after final locking.");
	}

	public void unload() {
		lamp.unregisterAllCommands();
	}

}
