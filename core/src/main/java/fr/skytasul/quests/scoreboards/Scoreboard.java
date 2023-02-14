package fr.skytasul.quests.scoreboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.ChatUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.nms.NMS;

public class Scoreboard extends BukkitRunnable implements Listener {

	private static final Pattern QUEST_PLACEHOLDER = Pattern.compile("\\{quest_(.+)\\}");
	private static final int maxLength = NMS.getMCVersion() >= 13 ? 1024 : 30;
	
	private PlayerAccount acc;
	private Player p;
	private FastBoard board;
	private ScoreboardManager manager;
	
	private LinkedList<Line> lines = new LinkedList<>();
	
	private Quest shown = null;
	private List<Quest> launched;
	private boolean hid = false;
	private boolean hidForce = false;
	private int changeTime = 1;

	Scoreboard(Player player, ScoreboardManager manager) {
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		this.p = player;
		this.acc = PlayersManager.getPlayerAccount(p);
		this.manager = manager;

		for (ScoreboardLine line : manager.getScoreboardLines()) {
			lines.add(new Line(line));
		}

		launched = QuestsAPI.getQuests().getQuestsStarted(acc, false, true);

		hid = !manager.isWorldAllowed(p.getWorld().getName());
		
		super.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 2L, 20L);
	}

	@Override
	public void run() {
		if (!p.isOnline()) return;
		if (hid) return;
		changeTime--;
		if (changeTime == 0) {
			changeTime = manager.getQuestChangeTime();

			if (launched.isEmpty()) {
				shown = null;
				if (manager.hideEmtptyScoreboard()) {
					if (board != null) deleteBoard();
					return;
				}
			}
			if (board == null) initScoreboard();

			if (!launched.isEmpty()) {
				int lastID = launched.indexOf(shown);
				int id = lastID + 1;
				if (id >= launched.size() || lastID == -1) id = 0;
				if (lastID != id) {
					shown = launched.get(id);
					refreshQuestsLines(false);
				}
			}
		}
		if (board == null) return;

		updateBoard(false, true);
	}
	
	protected void questAdd(Quest quest) {
		launched.add(launched.indexOf(shown) + 1, quest);
		shown = quest;
		refreshQuestsLines(true);
	}
	
	protected void questRemove(Quest quest) {
		int id = launched.indexOf(quest);
		if (id == -1) return;
		launched.remove(quest);
		if (quest == shown) {
			if (!launched.isEmpty()) {
				shown = launched.get(id >= launched.size() ? 0 : id);
			}else shown = null;
			refreshQuestsLines(true);
		}
	}
	
	protected void questEdited(Quest newQuest, Quest oldQuest) {
		int index = launched.indexOf(oldQuest);
		if (index == -1) {
			// if scoreboard has been enabled during quest edition,
			// we add the quest to the player list
			if (newQuest.isScoreboardEnabled() && newQuest.hasStarted(acc)) launched.add(newQuest);
			return;
		}
		
		// if scoreboard has been disabled during quest edition,
		// we remove the quest from the player list as it should no longer be displayed
		if (!newQuest.isScoreboardEnabled()) {
			questRemove(oldQuest);
			return;
		}
		
		// we replace the old quest instance by the new one
		launched.set(index, newQuest);
		if (shown == oldQuest) {
			shown = newQuest;
			refreshQuestsLines(true);
		}
	}
	
	protected void worldChange(boolean toAllowed) {
		if (hid) {
			if (toAllowed) show(false);
		}else {
			if (!toAllowed) hide(false);
		}
	}
	
	public Quest getShownQuest() {
		return shown;
	}
	
	public boolean isHidden() {
		return hid;
	}
	
	public boolean isForceHidden() {
		return hidForce;
	}
	
	public void hide(boolean force) {
		hid = true;
		if (force) hidForce = true;
		if (board != null) {
			deleteBoard();
		}
	}
	
	public void show(boolean force) {
		if (hidForce && !force) return;
		hid = false;
		hidForce = false;
		if (board == null && !(launched.isEmpty() && manager.hideEmtptyScoreboard())) {
			initScoreboard();
			updateBoard(true, false);
		}
	}
	
	private void deleteBoard() {
		board.delete();
		board = null;
		for (Line line : lines) line.reset();
	}
	
	public void setShownQuest(Quest quest, boolean errorWhenUnknown) {
		if (!quest.isScoreboardEnabled()) return;
		if (!launched.contains(quest)) {
			if (errorWhenUnknown) {
				launched = QuestsAPI.getQuests().getQuestsStarted(acc, false, true);
				if (!launched.contains(quest)) throw new IllegalArgumentException("Quest is not running for player.");
			}else return;
		}
		shown = quest;
		refreshQuestsLines(true);
	}

	public void refreshQuestsLines(boolean updateBoard) {
		if (!manager.refreshLines()) return;
		lines.stream().filter(line -> line.hasQuestPlaceholders).forEach(line -> line.willRefresh = true);
		if (board == null || launched.isEmpty()) {
			changeTime = 1;
			run();
		}else {
			if (updateBoard) updateBoard(false, false);
		}
	}
	
	private void updateBoard(boolean update, boolean time) {
		if (board == null && !time) return;
		List<String> linesStrings = new ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			try {
				if (line.tryRefresh(time) && !update) update = true;
				linesStrings.addAll(line.lines);
				if (linesStrings.size() >= ChatColor.values().length - 1) {
					while (linesStrings.size() >= ChatColor.values().length - 1) {
						linesStrings.remove(linesStrings.size() - 1);
					}
					break;
				}
			}catch (Exception ex) {
				BeautyQuests.logger.warning("An error occured while refreshing scoreboard line " + i + " for " + p.getName(), ex);
				linesStrings.add("§c§lline error");
			}
		}
		if (update && board != null) board.updateLines(linesStrings);
	}
	
	public void setCustomLine(int id, String value){
		if (lines.size() <= id){
			Line line = new Line(new ScoreboardLine(value));
			line.createdLine = true;
			lines.add(line);
		}else {
			Line line = lines.get(id);
			line.setCustomValue(value);
			line.willRefresh = true;
		}
		updateBoard(true, false);
	}
	
	public boolean resetLine(int id){
		if (lines.size() <= id) return false;
		Line line = lines.get(id);
		if (line.createdLine){
			lines.remove(id);
		}else {
			line.setCustomValue(null);
			line.willRefresh = true;
		}
		updateBoard(true, false);
		return true;
	}
	
	public boolean removeLine(int id){
		if (lines.size() <= id) return false;
		lines.remove(id);
		updateBoard(true, false);
		return true;
	}
	
	@Override
	public synchronized void cancel() throws IllegalStateException {
		super.cancel();
		HandlerList.unregisterAll(this);
		if (board != null) deleteBoard();
	}
	
	public void initScoreboard(){
		board = new FastBoard(p);
		board.updateTitle(Lang.SCOREBOARD_NAME.toString());
	}

	class Line{

		ScoreboardLine param;
		int timeLeft = 0;
		
		private String customValue = null;
		boolean createdLine = false;
		
		boolean willRefresh = false;
		String lastValue = null;
		List<String> lines;
		
		boolean hasQuestPlaceholders;

		private Line(ScoreboardLine param) {
			this.param = param;
			computeHasQuestPlaceholders();
		}
		
		private boolean tryRefresh(boolean time) {
			if (!willRefresh && lines != null && param.getRefreshTime() == 0) return false;
			if (timeLeft == 0 || willRefresh) {
				willRefresh = false;
				timeLeft = param.getRefreshTime();
				String text = getValue();
				if (hasQuestPlaceholders)
					text = formatQuestPlaceholders(text);

				if (text == null) {
					// in this case, the line must not be displayed

					lines = Collections.emptyList();
					lastValue = null;
				} else {
					text = Utils.finalFormat(p, text, true);
					if (text.equals(lastValue))
						return false;

					lines = ChatUtils.wordWrap(text, param.getMaxLength() == 0 ? 30 : param.getMaxLength(), maxLength);

					lastValue = text;
				}
				return true;
			}
			if (time) timeLeft--;
			return false;
		}
		
		private void reset() {
			timeLeft = 0;
			lines = null;
			lastValue = null;
		}
		
		public String getValue(){
			return customValue == null ? param.getValue() : customValue;
		}
		
		public void setCustomValue(String value) {
			customValue = value;
			computeHasQuestPlaceholders();
		}

		private void computeHasQuestPlaceholders() {
			hasQuestPlaceholders = QUEST_PLACEHOLDER.matcher(getValue()).find();
		}
		
		private String formatQuestPlaceholders(String text) {
			StringBuffer textBuffer = new StringBuffer();
			Matcher matcher = QUEST_PLACEHOLDER.matcher(text);
			QuestDescriptionContext lazyContext = null;
			while (matcher.find()) {
				String descriptionId = matcher.group(1);
				String replacement;
				if (descriptionId.equals("name")) {
					replacement = shown == null ? Lang.SCOREBOARD_NONE_NAME.toString() : shown.getName();
				} else {
					if (shown == null) {
						replacement = descriptionId.equals("advancement")
								? Lang.SCOREBOARD_NONE_DESC.toString() // kept for consistency with pre-0.20
								: Lang.SCOREBOARD_NONE.toString();
					} else {
						Optional<QuestDescriptionProvider> optionalDescription = shown.getDescriptions().stream()
								.filter(description -> description.getDescriptionId().equals(descriptionId))
								.findFirst();
						if (optionalDescription.isPresent()) {
							if (lazyContext == null)
								lazyContext = new QuestDescriptionContext(QuestsConfiguration.getQuestDescription(),
										shown, acc, PlayerListGUI.Category.IN_PROGRESS, Source.SCOREBOARD);
							replacement = String.join("\n", optionalDescription.get().provideDescription(lazyContext));
						} else {
							if (manager.hideUnknownQuestPlaceholders()) {
								// early return as there is no point continuing processing placeholders
								// as this line won't be displayed
								return null;
							}

							replacement = descriptionId;
						}
					}
				}
				matcher.appendReplacement(textBuffer, replacement);
			}
			matcher.appendTail(textBuffer);
			return textBuffer.toString();
		}

	}
	
}