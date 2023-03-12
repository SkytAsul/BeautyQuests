package fr.skytasul.quests.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.scoreboards.Scoreboard;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

public class CommandsManager {
	
	private static String[] COMMAND_ALIASES = { "quests", "quest", "bq", "beautyquests", "bquests" };
	
	private BukkitCommandHandler handler;
	private boolean locked = false;
	
	public CommandsManager() {
		handler = BukkitCommandHandler.create(BeautyQuests.getInstance());
		handler.setMessagePrefix(QuestsConfiguration.getPrefix());
		handler.failOnTooManyArguments();
		
		handler.registerValueResolver(Quest.class, context -> {
			int id = context.popInt();
			Quest quest = QuestsAPI.getQuests().getQuest(id);
			if (quest == null)
				throw new CommandErrorException(Lang.QUEST_INVALID.format(id));
			return quest;
		});
		handler.getAutoCompleter().registerParameterSuggestions(Quest.class,
				SuggestionProvider.of(() -> QuestsAPI.getQuests().getQuests()
						.stream()
						.map(quest -> Integer.toString(quest.getID()))
						.collect(Collectors.toList())));
		
		handler.registerValueResolver(QuestPool.class, context -> {
			int id = context.popInt();
			QuestPool pool = QuestsAPI.getQuestPools().getPool(id);
			if (pool == null)
				throw new CommandErrorException(Lang.POOL_INVALID.format(id));
			return pool;
		});
		handler.getAutoCompleter().registerParameterSuggestions(QuestPool.class,
				SuggestionProvider.of(() -> QuestsAPI.getQuestPools().getPools()
						.stream()
						.map(pool -> Integer.toString(pool.getID()))
						.collect(Collectors.toList())));
		
		handler.registerValueResolver(BQNPC.class, context -> {
			int id = context.popInt();
			BQNPC npc = QuestsAPI.getNPCsManager().getById(id);
			if (npc == null)
				throw new CommandErrorException(Lang.NPC_DOESNT_EXIST.format(id));
			return npc;
		});
		handler.getAutoCompleter().registerParameterSuggestions(BQNPC.class,
				SuggestionProvider.of(() -> QuestsAPI.getNPCsManager().getIDs()
						.stream()
						.map(String::valueOf)
						.collect(Collectors.toList())));
		
		handler.registerCondition((@NotNull CommandActor actor, @NotNull ExecutableCommand command, @NotNull @Unmodifiable List<String> arguments) -> {
			if (command.hasAnnotation(OutsideEditor.class)) {
				BukkitCommandActor bukkitActor = (BukkitCommandActor) actor;
				if (bukkitActor.isPlayer() && (Inventories.isInSystem(bukkitActor.getAsPlayer()) || Editor.hasEditor(bukkitActor.getAsPlayer())))
					throw new CommandErrorException(Lang.ALREADY_EDITOR.toString());
			}
		});
		
		handler.setHelpWriter((command, actor) -> {
			if (!command.hasPermission(actor)) return null;
			for (Lang lang : Lang.values()) {
				if (lang.getPath().startsWith("msg.command.help.")) {
					String cmdKey = lang.getPath().substring(17);
					if (cmdKey.equalsIgnoreCase(command.getName())) return lang.format(command.getPath().get(0));
				}
			}
			return null;
		});
		
		handler.registerContextResolver(Scoreboard.class, context -> {
			return BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(context.getResolvedArgument(Player.class));
		});
		
		handler.registerCondition((actor, command, arguments) -> {
			DebugUtils.logMessage(actor.getName() + " executed command: " + command.getPath().toRealString() + " " + String.join(" ", arguments));
		});
	}
	
	public BukkitCommandHandler getHandler() {
		return handler;
	}
	
	public void initializeCommands() {
		handler.register(new CommandsRoot());
		
		registerCommands("", new CommandsAdmin(), new CommandsPlayer(), new CommandsPlayerManagement());
		registerCommands("scoreboard", new CommandsScoreboard());
		registerCommands("pools", new CommandsPools());
	}
	
	public void registerCommands(String subpath, OrphanCommand... commands) {
		Orphans path;
		if (subpath == null || subpath.isEmpty()) {
			path = Orphans.path(COMMAND_ALIASES);
		}else {
			path = Orphans.path(Arrays.stream(COMMAND_ALIASES).map(x -> x + " " + subpath).toArray(String[]::new));
		}
		handler.register(Arrays.stream(commands).map(path::handler).toArray());
		// if (locked) BeautyQuests.logger.warning("Registered commands after final locking.");
	}
	
	public void lockCommands() {
		if (locked) return;
		locked = true;
		handler.getBrigadier().ifPresent(brigadier -> {
			brigadier.register();
			DebugUtils.logMessage("Brigadier registered!");
		});
	}
	
	public void unload() {
		handler.unregisterAllCommands();
	}
	
}
