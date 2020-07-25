package fr.skytasul.quests.scoreboards;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.QuestCreateEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class Scoreboard extends BukkitRunnable implements Listener {

	private PlayerAccount acc;
	private Player p;
	private FastBoard board;
	private ScoreboardManager manager;
	
	private LinkedList<Line> lines = new LinkedList<>();
	
	private Quest shown = null;
	private List<Quest> launched;
	private boolean hid = false;
	private int changeTime = 1;

	Scoreboard(Player player, ScoreboardManager manager) {
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		this.p = player;
		this.acc = PlayersManager.getPlayerAccount(p);
		this.manager = manager;

		for (ScoreboardLine line : manager.getScoreboardLines()) {
			lines.add(new Line(line));
		}

		launched = QuestsAPI.getQuestsStarteds(acc, true);

		super.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 5L, 20L);
	}

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
	
	@EventHandler
	public void onQuestFinished(QuestFinishEvent e){
		if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
	}
	
	@EventHandler
	public void onStageReset(PlayerQuestResetEvent e){
		if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
	}
	
	@EventHandler
	public void onQuestRemove(QuestRemoveEvent e){
		questRemove(e.getQuest());
	}
	
	@EventHandler
	public void onQuestCreate(QuestCreateEvent e) {
		if (e.isEdited()) {
			if (e.getQuest().hasStarted(acc)) launched.add(e.getQuest());
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onQuestLaunch(QuestLaunchEvent e){
		if (e.getPlayerAccount() == acc && e.getQuest().isCancellable()) {
			launched.add(launched.indexOf(shown) + 1, e.getQuest());
			shown = e.getQuest();
			refreshQuestsLines(true);
		}
	}
	
	private void questRemove(Quest quest){
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
	
	public Quest getShownQuest() {
		return shown;
	}
	
	public void hide(){
		hid = true;
		if (board != null) {
			deleteBoard();
		}
	}
	
	public void show(){
		hid = false;
		if (board == null) {
			initScoreboard();
			updateBoard(true, false);
		}
	}
	
	private void deleteBoard() {
		board.delete();
		board = null;
		for (Line line : lines) line.reset();
	}
	
	public void setShownQuest(Quest quest) {
		if (!quest.isCancellable()) return;
		if (!launched.contains(quest)) throw new IllegalArgumentException("Quest is not running for player.");
		shown = quest;
		refreshQuestsLines(true);
	}

	public void refreshQuestsLines(boolean updateBoard) {
		if (!manager.refreshLines()) return;
		for (Line line : lines) {
			if (line.getValue().contains("{questName}") || line.getValue().contains("{questDescription}")) line.willRefresh = true;
		}
		if (board == null || launched.isEmpty()) {
			changeTime = 1;
			run();
		}else {
			if (updateBoard) updateBoard(false, false);
		}
	}
	
	private void updateBoard(boolean update, boolean time) {
		List<String> linesStrings = new ArrayList<>(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			try {
				if (line.tryRefresh(time) && !update) update = true;
				linesStrings.addAll(line.lines);
			}catch (Exception ex) {
				BeautyQuests.logger.warning("An error occured while refreshing scoreboard line " + i);
				ex.printStackTrace();
				linesStrings.add("§c§lline error");
			}
		}
		if (update) board.updateLines(linesStrings);
	}
	
	public void setCustomLine(int id, String value){
		if (lines.size() <= id){
			Line line = new Line(new ScoreboardLine(value));
			line.createdLine = true;
			lines.add(line);
		}else {
			Line line = lines.get(id);
			line.customValue = value;
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
			line.customValue = null;
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
		
		String customValue = null;
		boolean createdLine = false;
		
		boolean willRefresh = false;
		String lastValue = null;
		List<String> lines;
		
		private Line(ScoreboardLine param) {
			this.param = param;
		}
		
		private boolean tryRefresh(boolean time) {
			if (!willRefresh && lines != null && param.getRefreshTime() == 0) return false;
			if (timeLeft == 0 || willRefresh) {
				willRefresh = false;
				timeLeft = param.getRefreshTime();
				String text = getValue();
				if (text.contains("{questName}")) {
					text = shown == null ? Lang.SCOREBOARD_NONE_NAME.toString() : text.replace("{questName}", shown.getName());
				}
				if (text.contains("{questDescription}")) {
					text = shown == null ? Lang.SCOREBOARD_NONE_DESC.toString() : text.replace("{questDescription}", shown.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.SCOREBOARD));
				}
				text = Utils.finalFormat(p, text, true);
				if (text.equals(lastValue)) return false;
				
				lines = Utils.wordWrap(text, param.getMaxLength() == 0 ? 30 : param.getMaxLength());
				
				lastValue = text;
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
		
	}
	
}