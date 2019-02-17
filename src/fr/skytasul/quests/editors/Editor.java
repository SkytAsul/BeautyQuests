package fr.skytasul.quests.editors;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public abstract class Editor implements Listener{

	private static Map<Player, Editor> players = new HashMap<>();
	protected final Player p;
	
	public Editor(Player p){
		this.p = p;
	}
	
	public void begin(){
		Inventories.closeWithoutExit(p);
	}

	public void end(){}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		if (e.getPlayer() == p){
			leave(p);
		}
	}

	public <T extends Editor> T enterOrLeave(Player p){
		return (T) enterOrLeave(p, this);
	}
	
	public void chat(String message){}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e){
		if (e.getPlayer() != p) return;
		e.setCancelled(true);
		if (e.isAsynchronous()){
			Utils.runSync(() -> chat(ChatColor.translateAlternateColorCodes('&', e.getMessage())));
		}else chat(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
	}
	
	private static void enter(Player p, Editor editor){
		editor.begin();
		Bukkit.getPluginManager().registerEvents(editor, BeautyQuests.getInstance());

		players.put(p, editor);
	}

	public static <T extends Editor> T enterOrLeave(Player p, T editor){
		if (editor == null) return null;
		Editor edit = (Editor) players.get(p);
		if (edit == null){
			enter(p, editor);
		}else if (edit.getClass() == editor.getClass()){
			leave(p);
		}else{
			Utils.sendMessage(p, Lang.ALREADY_EDITOR.toString());
		}
		return editor;
	}

	public static boolean hasEditor(Player player){
		return players.containsKey(player);
	}

	public static void leave(Player player){
		if (!hasEditor(player))
			return;
		Editor editor = (Editor) players.remove(player);
		HandlerList.unregisterAll(editor);
		editor.end();
	}

	public static void leaveAll(){
		for (Player p : players.keySet()){
			leave(p);
		}
		players.clear();
	}

}
