package fr.skytasul.quests.stages;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
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
		if (canUpdate(p)) Utils.runSync(() -> finishStage(p));
		return true;
	}

	
	public void serialize(Map<String, Object> map){
		Validate.notNull(text, "Text cannot be null");
		map.put("writeText", text);
		if (cancel) map.put("cancel", true);
		if (ignoreCase) map.put("ignoreCase", true);
	}
	
	public static StageChat deserialize(Map<String, Object> map, QuestBranch branch) {
		return new StageChat(branch, (String) map.get("writeText"), map.containsKey("cancel") ? (boolean) map.get("cancel") : false, map.containsKey("ignoreCase") ? (boolean) map.get("ignoreCase") : false);
	}

	public static class Creator extends StageCreation<StageChat> {
		
		private String text;
		private boolean cancel = true;
		private boolean ignoreCase = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.itemSwitch(Lang.ignoreCase.toString(), ignoreCase), (p, item) -> setIgnoreCase(ItemUtils.toggle(item)));
			line.setItem(7, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), cancel), (p, item) -> setCancel(ItemUtils.toggle(item)));
			line.setItem(8, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString()), (p, item) -> launchEditor(p));
		}
		
		public void setText(String text) {
			this.text = text;
			line.editItem(8, ItemUtils.lore(line.getItem(8), Lang.optionValue.format(text)));
		}
		
		public void setIgnoreCase(boolean ignoreCase) {
			if (this.ignoreCase != ignoreCase) {
				this.ignoreCase = ignoreCase;
				line.editItem(6, ItemUtils.set(line.getItem(6), ignoreCase));
			}
		}
		
		public void setCancel(boolean cancel) {
			if (this.cancel != cancel) {
				this.cancel = cancel;
				line.editItem(7, ItemUtils.set(line.getItem(7), cancel));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			launchEditor(p);
		}

		@Override
		public void edit(StageChat stage) {
			super.edit(stage);
			setText(stage.text);
			setIgnoreCase(stage.ignoreCase);
			setCancel(stage.cancel);
		}

		@Override
		public StageChat finishStage(QuestBranch branch) {
			return new StageChat(branch, text, cancel, ignoreCase);
		}
		
		private void launchEditor(Player p) {
			Lang.CHAT_MESSAGE.send(p);
			new TextEditor<String>(p, () -> {
				if (text == null) remove();
				reopenGUI(p, true);
			}, obj -> {
				obj = obj.replace("{SLASH}", "/");
				setText(obj);
				reopenGUI(p, false);
			}).enter();
		}
	}

}
