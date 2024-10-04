package fr.skytasul.quests.commands;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.commands.CommandsManager;
import fr.skytasul.quests.api.commands.OutsideEditor;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.commands.parameters.QuestParameter;
import fr.skytasul.quests.scoreboards.Scoreboard;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.Lamp;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandsManagerImplementation implements CommandsManager {

	private static String[] COMMAND_ALIASES = { "quests", "quest", "bq", "beautyquests", "bquests" };

	private Lamp<BukkitCommandActor> handler;
	private boolean locked = false;

	public CommandsManagerImplementation() {
		var builder = BukkitLamp.builder(BeautyQuests.getInstance());
		// handler.setMessagePrefix(BeautyQuests.getInstance().getPrefix());
		// handler.failOnTooManyArguments();

		builder.parameterTypes(parameters -> {
			parameters.addParameterType(Quest.class, new QuestParameter());
		});

		handler.registerValueResolver(QuestPool.class, context -> {
			int id = context.popInt();
			QuestPool pool = QuestsAPI.getAPI().getPoolsManager().getPool(id);
			if (pool == null)
				throw new CommandErrorException(Lang.POOL_INVALID.quickFormat("pool_id", id));
			return pool;
		});
		handler.getAutoCompleter().registerParameterSuggestions(QuestPool.class,
				SuggestionProvider.of(() -> QuestsAPI.getAPI().getPoolsManager().getPools()
						.stream()
						.map(pool -> Integer.toString(pool.getId()))
						.collect(Collectors.toList())));

		handler.registerValueResolver(BqNpc.class, context -> {
			String id = context.pop();
			BqNpc npc = QuestsPlugin.getPlugin().getNpcManager().getById(id);
			if (npc == null)
				throw new CommandErrorException(Lang.NPC_DOESNT_EXIST.quickFormat("npc_id", id));
			return npc;
		});
		handler.getAutoCompleter().registerParameterSuggestions(BqNpc.class,
				SuggestionProvider.of(() -> BeautyQuests.getInstance().getNpcManager().getAvailableIds()));

		handler.registerCondition((@NotNull CommandActor actor, @NotNull ExecutableCommand command, @NotNull @Unmodifiable List<String> arguments) -> {
			if (command.hasAnnotation(OutsideEditor.class)) {
				BukkitCommandActor bukkitActor = (BukkitCommandActor) actor;
				if (bukkitActor.isPlayer()
						&& (QuestsPlugin.getPlugin().getGuiManager().hasGuiOpened(bukkitActor.getAsPlayer())
								|| QuestsPlugin.getPlugin().getEditorManager().isInEditor(bukkitActor.getAsPlayer())))
					throw new CommandErrorException(Lang.ALREADY_EDITOR.toString());
			}
		});

		handler.setHelpWriter((command, actor) -> {
			if (!command.hasPermission(actor)) return null;
			for (Lang lang : Lang.values()) {
				if (lang.getPath().startsWith("msg.command.help.")) {
					String cmdKey = lang.getPath().substring(17);
					if (cmdKey.equalsIgnoreCase(command.getName()))
						return lang.quickFormat("label", command.getPath().get(0));
				}
			}
			return null;
		});

		handler.registerResponseHandler(String.class, (msg, actor, command) -> {
			MessageUtils.sendMessage(((BukkitCommandActor) actor).getSender(), msg, MessageType.DefaultMessageType.PREFIXED);
		});

		handler.registerContextResolver(Scoreboard.class, context -> {
			return BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(context.getResolvedArgument(Player.class));
		});

		handler.registerCondition((actor, command, arguments) -> {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(actor.getName() + " executed command: " + command.getPath().toRealString() + " " + String.join(" ", arguments));
		});

		handler = builder.build();
	}

	@Override
	public BukkitCommandHandler getHandler() {
		return handler;
	}

	public void initializeCommands() {
		handler.register(new CommandsRoot());

		registerCommands("", new CommandsAdmin(), new CommandsPlayer(), new CommandsPlayerManagement());
		registerCommands("scoreboard", new CommandsScoreboard());
		registerCommands("pools", new CommandsPools());
	}

	@Override
	public void registerCommands(String subpath, OrphanCommand... commands) {
		Orphans path;
		if (subpath == null || subpath.isEmpty()) {
			path = Orphans.path(COMMAND_ALIASES);
		}else {
			path = Orphans.path(Arrays.stream(COMMAND_ALIASES).map(x -> x + " " + subpath).toArray(String[]::new));
		}
		handler.register(Arrays.stream(commands).map(path::handler).toArray());
		// if (locked) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Registered commands after final locking.");
	}

	public void lockCommands() {
		if (locked) return;
		locked = true;
		handler.getBrigadier().ifPresent(brigadier -> {
			brigadier.register();
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Brigadier registered!");
		});
	}

	public void unload() {
		handler.unregisterAllCommands();
	}

}
