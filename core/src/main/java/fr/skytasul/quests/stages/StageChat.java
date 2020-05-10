package fr.skytasul.quests.stages;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StageChat extends AbstractStage{
	
	private String text;
	private boolean cancel;
	private boolean ignoreCase;
	
	private boolean command;
	
	public StageChat(QuestBranch branch, String text, boolean cancel, boolean ignoreCase) {
		super(branch);
		
		Validate.notNull(text, "Text cannot be null");
		this.text = text;
		this.command = text.startsWith("/");

		this.cancel = cancel;
		this.ignoreCase = ignoreCase;
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
	
	public boolean isEventCancelled() {
		return cancel;
	}
	
	public boolean isCaseIgnored() {
		return ignoreCase;
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
		if (!(ignoreCase ? message.equalsIgnoreCase(text) : message.equals(text))) return false;
		if (!hasStarted(p)) return false;
		Utils.runSync(() -> finishStage(p));
		return true;
	}

	
	public void serialize(Map<String, Object> map){
		Validate.notNull(text, "Text cannot be null");
		map.put("writeText", text);
		if (cancel) map.put("cancel", true);
		if (ignoreCase) map.put("ignoreCase", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		AbstractStage st = new StageChat(branch, (String) map.get("writeText"), map.containsKey("cancel") ? (boolean) map.get("cancel") : false, map.containsKey("ignoreCase") ? (boolean) map.get("ignoreCase") : false);
		return st;
	}

	public static class Creator implements StageCreationRunnables {

		public void start(Player p, LineData datas) {
			datas.put("cancel", true);
			datas.put("ignoreCase", false);
			setItems(datas);
			launchEditor(p, datas);
		}

		public AbstractStage finish(LineData datas, QuestBranch branch) {
			StageChat stage = new StageChat(branch, (String) datas.get("text"), (boolean) datas.get("cancel"), (boolean) datas.get("ignoreCase"));
			return stage;
		}

		public void edit(LineData datas, AbstractStage stage) {
			StageChat st = (StageChat) stage;
			datas.put("text", st.text);
			datas.put("cancel", st.cancel);
			datas.put("ignoreCase", st.ignoreCase);
			setItems(datas);
		}

		public static void setItems(LineData datas) {
			datas.getLine().setItem(6, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString(), datas.containsKey("text") ? (String) datas.get("text") : "§lx"), (p, datasx, item) -> launchEditor(p, datas));
			datas.getLine().setItem(5, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), (boolean) datas.get("cancel")), (p, datasx, item) -> datas.put("cancel", ItemUtils.toggle(item)));
			datas.getLine().setItem(4, ItemUtils.itemSwitch(Lang.ignoreCase.toString(), (boolean) datas.get("ignoreCase")), (p, datasx, item) -> datas.put("ignoreCase", ItemUtils.toggle(item)));
		}

		public static void launchEditor(Player p, LineData datas) {
			Lang.CHAT_MESSAGE.send(p);
			new TextEditor(p, (obj) -> {
				String msg = ((String) obj).replace("{SLASH}", "/");
				datas.put("text", msg);
				datas.getGUI().reopen(p, false);
				ItemUtils.lore(datas.getLine().getItem(6), msg);
			}).enterOrLeave(p);
		}
	}

}
