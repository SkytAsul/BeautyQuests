	package fr.skytasul.quests.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class CommandsManager implements CommandExecutor, TabCompleter{

	public final Map<String, InternalCommand> commands = new HashMap<>();
	private Consumer<CommandSender> noArgs;
	
	/**
	 * @param noArgs RunnableObj(player) who'll be ran if the command is executed without any arguments <i>(can be null)</i>
	 */
	public CommandsManager(Consumer<CommandSender> noArgs){
		this.noArgs = noArgs;
	}
	
	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	public void registerCommandsClass(Object commandsClassInstance){
		for(Method method : commandsClassInstance.getClass().getDeclaredMethods()){
			if (method.isAnnotationPresent(Cmd.class)){
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1){
					if (method.getParameterTypes()[0] == CommandContext.class){
						this.commands.put(method.getName().toLowerCase(), new InternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				}
				DebugUtils.logMessage("Error when loading command annotated method " + method.getName() + " in class " + commandsClassInstance.getClass().getName() + ". Needed argument: fr.skytasul.quests.commands.CommandContext");
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		String commandString = "/" + label + " " + Utils.buildFromArray(args, 0, " ");
		DebugUtils.logMessage(sender.getName() + " issued server command: " + commandString);
		
		if (args.length == 0){
			if (noArgs != null){
				noArgs.accept(sender);
			}else Lang.INCORRECT_SYNTAX.sendWP(sender);
			return false;
		}
		
		InternalCommand internal = commands.get(args[0].toLowerCase());
		if (internal == null){
			Lang.COMMAND_DOESNT_EXIST.sendWP(sender);
			return false;
		}
		
		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof Player)){
			Lang.MUST_PLAYER.sendWP(sender);
			return false;
		}
		
		if (!cmd.permission().isEmpty() && !hasPermission(sender, cmd.permission(), true)) return false;
		
		if (args.length - 1 < cmd.min()){
			Lang.INCORRECT_SYNTAX.sendWP(sender);
			return false;
		}
		
		if (cmd.player() && cmd.noEditorInventory() && (Inventories.isInSystem((Player) sender) || Editor.hasEditor((Player) sender))){
			Lang.ALREADY_EDITOR.send(sender);
			return true;
		}
		
		Object[] argsCmd = new Object[args.length - 1];
		for (int i = 1; i < args.length; i++){
			/*if (i > cmd.args().length){
				Lang.INCORRECT_SYNTAX.sendWP(sender);
				return false;
			}*/
			String arg = args[i];
			String type = i > cmd.args().length ? "" : cmd.args()[i-1];
			if (type.equals("PLAYERS")){
				Player target = Bukkit.getPlayerExact(arg);
				if (target == null){
					Lang.PLAYER_NOT_ONLINE.send(sender, arg);
					return false;
				}
				argsCmd[i-1] = target;
			}else if (type.equals("QUESTSID")){
				Integer id = Utils.parseInt(sender, arg);
				if (id == null) return false;
				Quest qu = QuestsAPI.getQuests().getQuest(id);
				if (qu == null){
					Lang.QUEST_INVALID.send(sender, id);
					return false;
				}
				argsCmd[i-1] = qu;
			}else if (type.equals("POOLSID")) {
				Integer id = Utils.parseInt(sender, arg);
				if (id == null) return false;
				QuestPool pool = QuestsAPI.getQuestPools().getPool(id);
				if (pool == null) {
					Lang.POOL_INVALID.send(sender, id);
					return false;
				}
				argsCmd[i - 1] = pool;
			}else if (type.equals("NPCSID")){
				Integer id = Utils.parseInt(sender, arg);
				if (id == null) return false;
				BQNPC npc = QuestsAPI.getNPCsManager().getById(id);
				if (npc == null){
					Lang.NPC_DOESNT_EXIST.send(sender, id);
					return false;
				}
				argsCmd[i-1] = npc;
			}else {
				argsCmd[i-1] = arg;
			}
		}
		
		try {
			DebugUtils.logMessage(sender.getName() + " invoked method \"" + internal.method.getName() + "\" from command: " + commandString);
			internal.method.invoke(internal.commands, new CommandContext(this, sender, argsCmd, label));
		}catch (Exception e) {
			Lang.ERROR_OCCURED.send(sender, e.getCause() == null ? e.getClass().getSimpleName() : e.getCause().getClass().getSimpleName());
			BeautyQuests.logger.severe("An exception occured during command execution:", e);
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];
		
		if (args.length == 1){
			for (Entry<String, InternalCommand> en : commands.entrySet()){ // PERMISSIONS
				if (!en.getValue().cmd.hide() && hasPermission(sender, en.getValue().cmd.permission(), false)) find.add(en.getKey());
			}
		}else if (args.length >= 2){
			int index = args.length-2;
			if (!commands.containsKey(sel)) return tmp;
			InternalCommand internal = commands.get(sel);
			String[] needed = internal.cmd.args();
			if (needed.length <= index) return tmp;
			if (!hasPermission(sender, internal.cmd.permission(), false)) return tmp;
			sel = args[index + 1];
			String key = needed[index];
			if (key.equals("QUESTSID")){
				for (Quest quest : QuestsAPI.getQuests()) find.add(Integer.toString(quest.getID()));
			}else if (key.equals("POOLSID")) {
				for (QuestPool pool : QuestsAPI.getQuestPools().getPools()) find.add(Integer.toString(pool.getID()));
			}else if (key.equals("PLAYERS")){
				return null;
			}else if (key.equals("NPCSID")){
				find.addAll(QuestsAPI.getNPCsManager().getIDs().stream().map(String::valueOf).collect(Collectors.toList()));
			}else if (key.equals("BOOLEAN")) {
				find.add("false");
				find.add("true");
			}else {
				find.addAll(Arrays.asList(key.split("\\|")));
			}
		}else return tmp;
		
		for (String arg : find){
			if (arg.startsWith(sel)) tmp.add(arg);
		}
		return tmp;
	}
	
	public static boolean hasPermission(CommandSender sender, String cmd, boolean message) {
		if (cmd == null || cmd.isEmpty()) return true;
		if (!sender.hasPermission(("beautyquests.command." + cmd))) {
			Lang.PERMISSION_REQUIRED.sendWP(sender, "beautyquests.command." + cmd);
			return false;
		}
		return true;
	}
	
	class InternalCommand{
		Cmd cmd;
		Method method;
		Object commands;
		
		InternalCommand(Cmd cmd, Method method, Object commandsClass){
			this.cmd = cmd;
			this.method = method;
			this.commands = commandsClass;
		}
	}
	
}