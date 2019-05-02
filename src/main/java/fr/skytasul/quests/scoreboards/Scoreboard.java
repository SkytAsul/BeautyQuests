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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerStageResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.scoreboards.ScoreboardSigns.VirtualTeam;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class Scoreboard implements Listener{

	private PlayerAccount acc;
	private Player p;
	private ScoreboardSigns sb;
	
	private LinkedList<Line> lines = new LinkedList<>();
	private BukkitRunnable runnable;
	
	private Quest showed = null;
	private int nextId = -1;
	private List<Quest> launched;
	
	Scoreboard(Player player, ScoreboardManager manager){
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		this.p = player;
		this.acc = PlayersManager.getPlayerAccount(p);
		
		for (ScoreboardLine line : manager.getScoreboardLines()){
			lines.add(new Line(line));
		}
		
		launched = QuestsAPI.getQuestsStarteds(acc, true);

		if (manager.hideEmtptyScoreboard()) {
			if (!launched.isEmpty()) initScoreboard();
		}else initScoreboard();
		
		runnable = new BukkitRunnable() {
			int changeTime = 1;
			public void run(){
				changeTime--;
				boolean change = nextId != -1;
				if (changeTime == 0 || change){
					changeTime = manager.getQuestChangeTime();
					nextId = -1;
					
					if (launched.isEmpty()){
						showed = null;
						if (manager.hideEmtptyScoreboard() && sb != null){
							sb.destroy();
							sb = null;
							for (Line line : lines) line.reset();
						}
						return; // no refresh
					}else {
						if (sb == null) initScoreboard(); 
						
						int id = nextId == -1 ? launched.indexOf(showed)+1 : nextId;
						if (id >= launched.size() || id == -1) id = 0;
						showed = launched.get(id);
						if (change && manager.refreshLines()) refreshQuestsLines();
					}
				}
				if (sb == null) return;
				
				for (Line line : lines){
					try{
						if (line.tryRefresh()) line.refreshLines();
					}catch (Exception ex){
						BeautyQuests.logger.warning("An error occured when refreshing scoreboard line " + lines.indexOf(line));
						ex.printStackTrace();
					}
				}
			}
		};
		runnable.runTaskTimerAsynchronously(BeautyQuests.getInstance(), 5L, 20L);
	}
	
	public ScoreboardSigns getScoreboard(){
		return sb;
	}
	
	@EventHandler
	public void onQuestFinished(QuestFinishEvent e){
		if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
	}
	
	@EventHandler
	public void onStageReset(PlayerStageResetEvent e){
		if (e.getPlayerAccount() == acc) questRemove(e.getQuest());
	}
	
	@EventHandler
	public void onQuestRemove(QuestRemoveEvent e){
		if (launched.contains(e.getQuest())) questRemove(e.getQuest());
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onQuestLaunch(QuestLaunchEvent e){
		if (!e.isCancelled() && e.getPlayerAccount() == acc && e.getQuest().isScoreboardEnabled()){
			nextId = launched.indexOf(showed)+1;
			launched.add(nextId, e.getQuest());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		if (e.getPlayer() != p) return;
		unload();
	}
	
	private void questRemove(Quest quest){
		if (quest == showed){
			nextId = launched.indexOf(showed);
		}
		launched.remove(quest);
	}
	
	public void refreshQuestsLines(){
		for (Line line : lines){
			if (line.getValue().contains("{questName}") || line.getValue().contains("{questDescription}")) line.refreshLines();
		}
	}
	
	public void setCustomLine(int id, String value){
		if (lines.size() <= id){
			Line line = new Line(new ScoreboardLine(value));
			line.createdLine = true;
			line.setLines(lines.isEmpty() ? 0 : lines.getLast().lastLineIndex() + 1);
			lines.add(line);
		}else {
			Line line = lines.get(id);
			line.customValue = value;
			line.refreshLines();
		}
	}
	
	public boolean resetLine(int id){
		if (lines.size() <= id) return false;
		Line line = lines.get(id);
		if (line.createdLine){
			line.removeLines();
			lines.remove(id);
		}else {
			line.customValue = null;
			line.refreshLines();
		}
		return true;
	}
	
	public boolean removeLine(int id){
		if (lines.size() <= id) return false;
		Line line = lines.get(id);
		line.removeLines();
		lines.remove(id);
		return true;
	}
	
	public void unload(){
		HandlerList.unregisterAll(this);
		if (sb != null) sb.destroy();
		if (runnable != null) runnable.cancel();
	}
	
	public void initScoreboard(){
		sb = new ScoreboardSigns(p, Lang.SCOREBOARD_NAME.toString());
		sb.create();
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			line.setLines(i == 0 ? 0 : lines.get(i - 1).lastLineIndex() + 1);
		}
	}

	class Line{
		ScoreboardLine param;
		int timeLeft = 1;
		int lastAmount = 0;
		List<VirtualTeam> teams = new ArrayList<>();
		
		String customValue = null;
		boolean createdLine = false;
		
		Line(ScoreboardLine param){
			this.param = param;
			timeLeft = param.refresh;
		}
		
		/**
		 * Refresh all lines, based on the first index of the previous lines
		 */
		public void refreshLines(){
			setLines(firstLineIndex());
		}
		
		/**
		 * How it works:
		 * <ol>
		 * <li> If there is no custom value, the default text will be used
		 * <li> If there is quests placeholders (<code>{questName}</code> or <code>{questDescription}</code>) they will be replaced by the appropriated value
		 * <li> All other placeholders are replaced
		 * <li> The final value is split into lines, depending of its length
		 * <li> If there is less lines than the previous time, theses lines are removed
		 * <li> If there is more lines than the previous time, all lines up are moved forward
		 * <li> Finally, the lines are set in the scoreboard
		 * </ol>
		 * @param firstLine Scoreboard line where the first line will be placed
		 */
		public void setLines(int firstLine){
			String text = getValue();
			if (text.contains("{questName}")){
				text = showed == null ? Lang.SCOREBOARD_NONE_NAME.toString() : text.replace("{questName}", showed.getName());
			}
			if (text.contains("{questDescription}")){
				text = showed == null ? Lang.SCOREBOARD_NONE_DESC.toString() : text.replace("{questDescription}", showed.getStageManager().getDescriptionLine(acc, Source.SCOREBOARD));
			}
			text = Utils.finalFormat(p, text, true);
			List<String> ls = Utils.splitOnSpace(text, param.length == 0 ? 48 : param.length);
			if (lastAmount > ls.size()){
				int toRemove = lastAmount - ls.size();
				for (int i = 0; i < toRemove; i++){
					sb.removeLine(sb.getTeamLine(teams.get(0)));
					teams.remove(0);
				}
			}else if (lastAmount < ls.size()){
				sb.moveLines(firstLine + lastAmount, ls.size() - lastAmount);
			}
			lastAmount = ls.size();
			for (int i = 0; i < ls.size(); i++){
				setTeam(i, sb.setLine(firstLine + i, ls.get(i)));
			}
		}
		
		public void removeLines(){
			int index = firstLineIndex();
			for (int i = 0; i < teams.size(); i++){
				sb.removeLine(index);
				teams.remove(0);
			}
		}
		
		private boolean tryRefresh(){
			if (param.refresh == 0) return false;
			if (timeLeft == 0){
				timeLeft = param.refresh;
				return true;
			}
			timeLeft--;
			return false;
		}
		
		private void setTeam(int index, VirtualTeam team){
			if (teams.size() <= index){
				teams.add(team); // theorically useless (space should be made before with sb.moveLines)
			}else {
				teams.set(index, team);
			}
		}
		
		void reset(){
			teams.clear();
			lastAmount = 0;
			timeLeft = param.refresh;
		}
		
		public int firstLineIndex(){
			return sb.getTeamLine(teams.get(0));
		}
		
		public int lastLineIndex(){
			return sb.getTeamLine(teams.get(teams.size() - 1));
		}
		
		public String getValue(){
			return customValue == null ? param.value : customValue;
		}
		
	}
	
}