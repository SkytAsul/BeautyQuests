	package fr.skytasul.quests.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class CommandsManager implements CommandExecutor, TabCompleter{

	public final Map<String, InternalCommand> commands = new HashMap<>();
	private RunnableObj noArgs;
	
	/**
	 * @param noArgs RunnableObj(player) who'll be ran if the command is executed without any arguments <i>(can be null)</i>
	 */
	public CommandsManager(RunnableObj noArgs){
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
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (args.length == 0){
			if (noArgs != null){
				noArgs.run(sender);
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
		
		if (!cmd.permission().isEmpty() && !hasPermission(sender, cmd.permission())){
			Lang.PERMISSION_REQUIRED.sendWP(sender, cmd.permission());
			return false;
		}
		
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
				Quest qu = QuestsAPI.getQuestFromID(id);
				if (qu == null){
					Lang.QUEST_INVALID.send(sender, id);
					return false;
				}
				argsCmd[i-1] = qu;
			}else if (type.equals("NPCSID")){
				Integer id = Utils.parseInt(sender, arg);
				if (id == null) return false;
				NPC npc = CitizensAPI.getNPCRegistry().getById(id);
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
			internal.method.invoke(internal.commands, new CommandContext(this, sender, argsCmd, label));
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Lang.ERROR_OCCURED.send(sender, " command executing");
			e.printStackTrace();
		}
		
		return false;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];
		
		if (args.length == 1){
			for (Entry<String, InternalCommand> en : commands.entrySet()){ // PERMISSIONS
				String perm = en.getValue().cmd.permission();
				if (perm != null && sender.hasPermission(perm)) find.add(en.getKey());
			}
		}else if (args.length >= 2){
			int index = args.length-2;
			if (!commands.containsKey(sel)) return tmp;
			InternalCommand internal = commands.get(sel);
			String[] needed = internal.cmd.args();
			if (needed.length <= index) return tmp;
			if (!internal.cmd.permission().isEmpty() && !hasPermission(sender, internal.cmd.permission())) return tmp;
			sel = args[index + 1];
			String key = needed[index];
			if (key.equals("QUESTSID")){
				for (Quest quest : QuestsAPI.getQuests()) find.add(quest.getID() + "");
			}else if (key.equals("PLAYERS")){
				return null;
			}else if (key.equals("NPCSID")){
				for (NPC npc : CitizensAPI.getNPCRegistry().sorted()) find.add(npc.getId() + "");
			}else{
				find = new ArrayList<>(Arrays.asList(key.split("\\|")));
			}
		}else return tmp;
		
		for (String arg : find){
			if (arg.startsWith(sel)) tmp.add(arg);
		}
		return tmp;
	}
	
	public static boolean hasPermission(CommandSender sender, String cmd){
		return sender.hasPermission(("beautyquests.command." + cmd));
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