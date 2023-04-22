package fr.skytasul.quests.stages;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.cryptomorin.xseries.XMaterial;
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

public class StageChat extends AbstractStage{
	
	private String text;
	private boolean cancel;
	private boolean ignoreCase;
	private boolean placeholders;
	
	private boolean command;
	
	public StageChat(QuestBranch branch, String text, boolean cancel, boolean ignoreCase, boolean placeholders) {
		super(branch);
		
		Validate.notNull(text, "Text cannot be null");
		this.text = text;
		this.command = text.startsWith("/");

		this.cancel = cancel;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_CHAT.format(text);
	}
	
	@Override
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
		if (placeholders) message = Utils.finalFormat(p, message, true);
		if (!(ignoreCase ? message.equalsIgnoreCase(text) : message.equals(text))) return false;
		if (!hasStarted(p)) return false;
		if (canUpdate(p)) finishStage(p);
		return true;
	}

	
	@Override
	public void serialize(ConfigurationSection section) {
		Validate.notNull(text, "Text cannot be null");
		section.set("writeText", text);
		if (!cancel) section.set("cancel", false);
		if (ignoreCase) section.set("ignoreCase", true);
		if (!placeholders) section.set("placeholders", false);
	}
	
	public static StageChat deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageChat(branch, section.getString("writeText"), section.getBoolean("cancel", true), section.getBoolean("ignoreCase", false), section.getBoolean("placeholders", true));
	}

	public static class Creator extends StageCreation<StageChat> {
		
		private static final int PLACEHOLDERS_SLOT = 5;
		private static final int IGNORE_CASE_SLOT = 6;
		private static final int CANCEL_EVENT_SLOT = 7;
		private static final int MESSAGE_SLOT = 8;
		
		private String text;
		private boolean placeholders = true;
		private boolean cancel = true;
		private boolean ignoreCase = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(PLACEHOLDERS_SLOT, ItemUtils.itemSwitch(Lang.placeholders.toString(), placeholders), (p, item) -> setPlaceholders(ItemUtils.toggle(item)));
			line.setItem(IGNORE_CASE_SLOT, ItemUtils.itemSwitch(Lang.ignoreCase.toString(), ignoreCase), (p, item) -> setIgnoreCase(ItemUtils.toggle(item)));
			line.setItem(CANCEL_EVENT_SLOT, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), cancel), (p, item) -> setCancel(ItemUtils.toggle(item)));
			line.setItem(MESSAGE_SLOT, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString()), (p, item) -> launchEditor(p));
		}
		
		public void setText(String text) {
			this.text = text;
			line.editItem(MESSAGE_SLOT, ItemUtils.lore(line.getItem(MESSAGE_SLOT), Lang.optionValue.format(text)));
		}
		
		public void setPlaceholders(boolean placeholders) {
			if (this.placeholders != placeholders) {
				this.placeholders = placeholders;
				line.editItem(PLACEHOLDERS_SLOT, ItemUtils.set(line.getItem(PLACEHOLDERS_SLOT), placeholders));
			}
		}
		
		public void setIgnoreCase(boolean ignoreCase) {
			if (this.ignoreCase != ignoreCase) {
				this.ignoreCase = ignoreCase;
				line.editItem(IGNORE_CASE_SLOT, ItemUtils.set(line.getItem(IGNORE_CASE_SLOT), ignoreCase));
			}
		}
		
		public void setCancel(boolean cancel) {
			if (this.cancel != cancel) {
				this.cancel = cancel;
				line.editItem(CANCEL_EVENT_SLOT, ItemUtils.set(line.getItem(CANCEL_EVENT_SLOT), cancel));
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
			setPlaceholders(stage.placeholders);
		}

		@Override
		public StageChat finishStage(QuestBranch branch) {
			return new StageChat(branch, text, cancel, ignoreCase, placeholders);
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
