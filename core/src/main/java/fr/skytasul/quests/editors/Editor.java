package fr.skytasul.quests.editors;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
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
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.bossbar.BQBossBar;
import fr.skytasul.quests.utils.compatibility.bossbar.BQBossBarImplementation;
import fr.skytasul.quests.utils.nms.NMS;

public abstract class Editor implements Listener{

	private static Map<Player, Editor> players = new HashMap<>();
	private static BQBossBar bar = null;

	static {
		if (BQBossBar.BARS_ENABLED) {
			bar = new BQBossBarImplementation("§6Quests Editor", "YELLOW", "SOLID");
			bar.setProgress(0);
		}
	}
	
	protected final Player p;
	protected final Runnable cancel;
	
	public Editor(Player p, Runnable cancel) {
		this.p = p;
		this.cancel = cancel;
	}
	
	protected void begin() {
		Inventories.closeWithoutExit(p);
		if (NMS.getMCVersion() > 11){
			p.sendTitle(Lang.ENTER_EDITOR_TITLE.toString(), Lang.ENTER_EDITOR_SUB.toString(), 5, 50, 5);
		}else {
			Lang.ENTER_EDITOR_TITLE.send(p);
			Lang.ENTER_EDITOR_SUB.send(p);
		}
		if (bar != null) bar.addPlayer(p);
	}

	protected void end() {
		if (bar != null) bar.removePlayer(p);
	}
	
	protected final void cancel() {
		leave(p);
		cancel.run();
	}

	@Deprecated
	public <T extends Editor> T enterOrLeave(Player p) {
		Validate.isTrue(p == this.p);
		return enter();
	}
	
	public <T extends Editor> T enter() {
		Editor edit = players.get(p);
		if (edit == null) {
			begin();
			Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
			players.put(p, this);
			return (T) this;
		}
		Utils.sendMessage(p, Lang.ALREADY_EDITOR.toString());
		return null;
	}
	
	/**
	 * Happens when the player in the editor type somthing in the chat
	 * @param coloredMessage Message typed
	 * @param strippedMessage Message without default colors
	 * @return false if the plugin needs to send an help message to the player
	 */
	public boolean chat(String coloredMessage, String strippedMessage) {
		return false;
	}
	
	private final void callChat(String rawText){
		rawText = rawText.trim().replaceAll("\\uFEFF", ""); // remove blank characters, remove space at the beginning
		//rawText = ChatColor.stripColor(rawText); // remove default colors
		DebugUtils.logMessage(p.getName() + " entered \"" + rawText + "\" (" + rawText.length() + " characters) in an editor. (name: " + getClass().getName() + ")");
		String coloredMessage = ChatColor.translateAlternateColorCodes('&', rawText);
		String strippedMessage = ChatColor.stripColor(rawText);
		if (cancel != null && strippedMessage.equalsIgnoreCase("cancel")) {
			cancel();
		}else if (!chat(coloredMessage, strippedMessage)) {
			Lang.CHAT_EDITOR.send(p);
		}
	}
	
	protected String cancelWord(){
		return null;
	}
	
	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e){
		if (e.getPlayer() != p) return;
		e.setCancelled(true);
		if (e.isAsynchronous()){
			Utils.runSync(() -> callChat(e.getMessage()));
		}else callChat(e.getMessage());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer() == p) leave(p);
	}

	@Deprecated
	public static <T extends Editor> T enterOrLeave(Player p, T editor) {
		return editor.enterOrLeave(p);
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
		players.keySet().iterator().forEachRemaining(Editor::leave);
		players.clear();
	}

}
