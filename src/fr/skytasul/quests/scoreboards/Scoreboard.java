package fr.skytasul.quests.scoreboards;

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
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerStageResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class Scoreboard implements Listener{

	private PlayerAccount acc;
	private Player p;
	private ScoreboardSigns sb;
	
	private final BukkitRunnable runnable;
	private boolean refreshed = false;
	
	private List<Quest> launched;
	
	Scoreboard(Player player){
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		this.p = player;
		this.acc = PlayersManager.getPlayerAccount(p);

		launched = QuestsAPI.getQuestsStarteds(acc, true);
		if (!launched.isEmpty()) initScoreboard();
		
		runnable = new BukkitRunnable() {
			Quest quest = null;
			int id = 0;
			int secondsLeft = 0;
			int lineAmount = 0;
			
			public void run() {
				if (launched.isEmpty()){
					if (sb != null){
						sb.destroy();
						sb = null;
					}
					if (QuestsConfiguration.showEmptyScoreboards()) initScoreboard();
					return;
				}else if (sb == null) initScoreboard();
				
				if (secondsLeft == 0 || refreshed){
					id++;
					if (id >= launched.size()) id = 0;
					quest = launched.get(id);
					secondsLeft = 10;
					refreshed = false;
				}
				
				try{
					sb.setLine(1, "§6" + quest.getName());
					int i = 3;
					String desc = quest.getStageManager().getDescriptionLine(acc, false);
					if (desc != null){
						for (String s : Utils.splitOnSpace(desc, 30)){
							sb.setLine(i, s);
							i++;
						}
					}
					for (int ii = i; ii < lineAmount; ii++){
						sb.removeLine(ii);
					}
					lineAmount = i;
					
					secondsLeft--;
				}catch (Throwable ex){
					Utils.sendMessage(p, "An error occured when updating scoreboard. Please report it to the administrators. Quest : " + quest.getName() + ", stage " + quest.getStageManager().getPlayerStageID(acc));
					cancel();
					sb.destroy();
					ex.printStackTrace();
				}
			}
		};
		runnable.runTaskTimer(BeautyQuests.getInstance(), 5L, 20L); // 500L
	}
	
	@EventHandler
	public void onQuestFinished(QuestFinishEvent e){
		if (e.getPlayerAccount() == acc)  refreshLaunched();
	}
	
	@EventHandler
	public void onStageReset(PlayerStageResetEvent e){
		if (e.getPlayerAccount() == acc) refreshLaunched();
	}
	
	@EventHandler
	public void onQuestRemove(QuestRemoveEvent e){
		if (e.getLaunchedAccounts().contains(acc)) refreshLaunched();
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onQuestLaunch(QuestLaunchEvent e){
		if (!e.isCancelled() && e.getPlayerAccount() == acc){
			launched.add(e.getQuest());
			refreshed = true;
		}
	}
	
	public void unload(){
		if (sb != null) sb.destroy();
	}
	
	private void refreshLaunched(){
		launched = QuestsAPI.getQuestsStarteds(acc, true);
		refreshed = true;
	}
	
	public ScoreboardSigns getScoreboard(){
		return sb;
	}
	
	public void initScoreboard(){
		sb = new ScoreboardSigns(p, Lang.SCOREBOARD_NAME.toString());
		sb.create();
		sb.setLine(0, "");
		sb.setLine(1, Lang.SCOREBOARD_NONE.toString());
		sb.setLine(2, " ");
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		if (e.getPlayer() != p) return;
		if (sb != null) sb.destroy();
		HandlerList.unregisterAll(this);
		runnable.cancel();
	}
	
}
