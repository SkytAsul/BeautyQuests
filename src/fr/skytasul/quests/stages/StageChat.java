package fr.skytasul.quests.stages;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class StageChat extends AbstractStage{
	
	private String text;
	private boolean cancel;
	
	private boolean command;
	
	public StageChat(StageManager manager, String text, boolean cancel){
		super(manager);
		
		Validate.notNull(text, "Text cannot be null");
		this.text = text;
		this.command = text.startsWith("/");

		this.cancel = cancel;
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_CHAT.format(text);
	}
	
	protected Object[] descriptionFormat(PlayerAccount acc, Source source){
		return new String[]{text};
	}
	
	public String getText() {
		return text;
	}
	
	public boolean cancelEvent() {
		return cancel;
	}
	
	@EventHandler
	public void onTchat(AsyncPlayerChatEvent e) {
		if (command) return;
		if (check(e.getMessage(), e.getPlayer()) && cancel) e.setCancelled(true);
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (!command) return;
		if (check(e.getMessage(), e.getPlayer()) && cancel) e.setCancelled(true);
	}
	
	private boolean check(String message, Player p) {
		if (!message.equals(text)) return false;
		if (!hasStarted(p)) return false;
		Utils.runSync(() -> finishStage(p));
		return true;
	}

	
	public void serialize(Map<String, Object> map){
		Validate.notNull(text, "Text cannot be null");
		map.put("writeText", text);
		if (cancel) map.put("cancel", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		AbstractStage st = new StageChat(manager, (String) map.get("writeText"), map.containsKey("cancel") ? (boolean) map.get("cancel") : false);
		return st;
	}

}
