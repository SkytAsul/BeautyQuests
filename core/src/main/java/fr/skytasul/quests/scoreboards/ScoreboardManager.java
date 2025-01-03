package fr.skytasul.quests.scoreboards;

import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ScoreboardManager implements Listener, QuestsHandler {

	private final File file;
	private Map<Player, Scoreboard> scoreboards;
	private Map<UUID, Boolean> forceHiddenState;

	// Parameters
	private final List<ScoreboardLine> lines = new ArrayList<>();

	private int changeTime;
	private boolean hide;
	private boolean refreshLines;
	private boolean hideUnknownQuestPlaceholders;

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

	public boolean hideUnknownQuestPlaceholders() {
		return hideUnknownQuestPlaceholders;
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
		if (!QuestsConfiguration.getConfig().getQuestsConfig().scoreboards())
			return;
		removePlayerScoreboard(p);

		Scoreboard scoreboard = new Scoreboard(p, this);
		scoreboards.put(p, scoreboard);

		Boolean forceHidden = forceHiddenState.remove(p.getUniqueId());
		if (forceHidden != null && forceHidden.booleanValue()) scoreboard.hide(true);
	}

	@Override
	public void load() {
		if (!QuestsConfiguration.getConfig().getQuestsConfig().scoreboards())
			return;

		try {
			new FastBoard(null); // trigger class initialization
		}catch (ExceptionInInitializerError ex) {
			throw new IllegalStateException("The Scoreboard util cannot load, probably due to an incompatible server version.", ex);
		}catch (NullPointerException ex) {} // as we pass a null player to initialize, it will throw NPE

		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		ConfigurationSection questsSection = config.getConfigurationSection("quests");
		changeTime = questsSection.getInt("changeTime", 11);
		hide = questsSection.getBoolean("hideIfEmpty", true);
		refreshLines = questsSection.getBoolean("refreshLines", true);
		hideUnknownQuestPlaceholders = questsSection.getBoolean("hide unknown quest placeholders");

		worldsFilter = config.getStringList("worlds.filterList");
		isWorldAllowList = config.getBoolean("worlds.isAllowList");

		lines.clear();
		for (Map<?, ?> map : config.getMapList("lines")) {
			if (lines.size() == 15) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Limit of 15 scoreboard lines reached - please delete some in scoreboard.yml");
				break;
			}
			try {
				lines.add(ScoreboardLine.deserialize((Map<String, Object>) map));
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Registered " + lines.size() + " lines in scoreboard");

		scoreboards = new HashMap<>();
		forceHiddenState = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}

	@Override
	public void unload(){
		if (scoreboards == null)
			return;
		HandlerList.unregisterAll(this);
		for (Scoreboard s : scoreboards.values()) s.cancel();
		if (!scoreboards.isEmpty()) QuestsPlugin.getPlugin().getLoggerExpanded().info(scoreboards.size() + " scoreboards deleted.");
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
	public void questFinish(Quester acc, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, x -> x.questRemove(quest));
	}

	@Override
	public void questReset(Quester acc, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, x -> x.questRemove(quest));
	}

	@Override
	public void questUpdated(Quester acc, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, x -> x.setShownQuest(quest, true));
	}

	@Override
	public void questStart(Quester acc, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(acc, x -> x.questAdd(quest));
	}

	private void questEvent(Quester quester, Consumer<Scoreboard> consumer) {
		for (Player player : quester.getOnlinePlayers()) {
			Scoreboard scoreboard = scoreboards.get(player);
			if (scoreboard != null) consumer.accept(scoreboard);
		}
	}

}
