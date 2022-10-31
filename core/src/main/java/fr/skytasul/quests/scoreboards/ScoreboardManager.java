package fr.skytasul.quests.scoreboards;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;

public class ScoreboardManager implements Listener, QuestsHandler {

	private final File file;
	private Map<Player, Scoreboard> scoreboards;
	private Map<UUID, Boolean> forceHiddenState;
	
	// Parameters
	private final List<ScoreboardLine> lines = new ArrayList<>();
	
	private int changeTime;
	private boolean hide;
	private boolean refreshLines;
	
	private List<String> worldsFilter;
	private boolean isWorldAllowList;
	
	public ScoreboardManager(File file) {
		this.file = file;
	}
	
	public List<ScoreboardLine> getScoreboardLines(){
		return lines;
	}
	
	public int getQuestChangeTime(){
		return changeTime;
	}
	
	public boolean hideEmtptyScoreboard(){
		return hide;
	}
	
	public boolean refreshLines(){
		return refreshLines;
	}
	
	public List<String> getWorldsFilter() {
		return worldsFilter;
	}
	
	public boolean isWorldAllowList() {
		return isWorldAllowList;
	}
	
	public boolean isWorldAllowed(String worldName) {
		return isWorldAllowList() ? getWorldsFilter().contains(worldName) : !getWorldsFilter().contains(worldName);
	}
	
	public Scoreboard getPlayerScoreboard(Player p){
		return scoreboards.get(p);
	}
	
	public void removePlayerScoreboard(Player p){
		Scoreboard scoreboard = scoreboards.remove(p);
		if (scoreboard != null) {
			scoreboard.cancel();
			forceHiddenState.put(p.getUniqueId(), scoreboard.isForceHidden());
		}
	}
	
	public void create(Player p){
		if (!QuestsConfiguration.showScoreboards()) return;
		removePlayerScoreboard(p);
		
		Scoreboard scoreboard = new Scoreboard(p, this);
		scoreboards.put(p, scoreboard);
		
		Boolean forceHidden = forceHiddenState.remove(p.getUniqueId());
		if (forceHidden != null && forceHidden.booleanValue()) scoreboard.hide(true);
	}
	
	@Override
	public void load() {
		if (!QuestsConfiguration.showScoreboards()) return;
		
		try {
			new FastBoard(null); // trigger class initialization
		}catch (ExceptionInInitializerError ex) {
			throw new IllegalStateException("The Scoreboard util cannot load, probably due to an incompatible server version.", ex);
		}catch (NullPointerException ex) {} // as we pass a null player to initialize, it will throw NPE
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		changeTime = config.getInt("quests.changeTime", 11);
		hide = config.getBoolean("quests.hideIfEmpty", true);
		refreshLines = config.getBoolean("quests.refreshLines", true);
		
		worldsFilter = config.getStringList("worlds.filterList");
		isWorldAllowList = config.getBoolean("worlds.isAllowList");
		
		lines.clear();
		for (Map<?, ?> map : config.getMapList("lines")) {
			if (lines.size() == 15) {
				BeautyQuests.logger.warning("Limit of 15 scoreboard lines reached - please delete some in scoreboard.yml");
				break;
			}
			try {
				lines.add(ScoreboardLine.deserialize((Map<String, Object>) map));
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		DebugUtils.logMessage("Registered " + lines.size() + " lines in scoreboard");
		
		scoreboards = new HashMap<>();
		forceHiddenState = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}
	
	@Override
	public void unload(){
		HandlerList.unregisterAll(this);
		for (Scoreboard s : scoreboards.values()) s.cancel();
		if (!scoreboards.isEmpty()) BeautyQuests.getInstance().getLogger().info(scoreboards.size() + " scoreboards deleted.");
		scoreboards.clear();
		scoreboards = null;
		forceHiddenState.clear();
		forceHiddenState = null;
	}
	
	@EventHandler
	public void onAccountJoin(PlayerAccountJoinEvent e) {
		create(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onAccountLeave(PlayerAccountLeaveEvent e) {
		removePlayerScoreboard(e.getPlayer());
	}
	
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent e) {
		Scoreboard scoreboard = getPlayerScoreboard(e.getPlayer());
		if (scoreboard == null) return;
		scoreboard.worldChange(isWorldAllowed(e.getPlayer().getWorld().getName()));
	}
	
	/* Quests events */
	
	@Override
	public void questEdit(Quest newQuest, Quest oldQuest, boolean keepDatas) {
		scoreboards.forEach((p, scoreboard) -> {
			if (keepDatas) {
				scoreboard.questEdited(newQuest, oldQuest);
			}else {
				scoreboard.questRemove(oldQuest);
			}
		});
	}
	
	@Override
	public void questRemove(Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		scoreboards.forEach((p, scoreboard) -> scoreboard.questRemove(quest));
	}
	
	@Override
	public void questFinish(PlayerAccount acc, Player p, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, p, x -> x.questRemove(quest));
	}
	
	@Override
	public void questReset(PlayerAccount acc, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, null, x -> x.questRemove(quest));
	}
	
	@Override
	public void questUpdated(PlayerAccount acc, Player p, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, p, x -> x.setShownQuest(quest, true));
	}
	
	@Override
	public void questStart(PlayerAccount acc, Player p, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, p, x -> x.questAdd(quest));
	}
	
	private void questEvent(PlayerAccount acc, Player p, Consumer<Scoreboard> consumer) {
		if (p == null) p = acc.getPlayer();
		if (p != null) {
			Scoreboard scoreboard = scoreboards.get(p);
			if (scoreboard != null) consumer.accept(scoreboard);
		}
	}
	
}
