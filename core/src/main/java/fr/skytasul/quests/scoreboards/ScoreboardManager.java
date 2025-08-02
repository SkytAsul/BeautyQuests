package fr.skytasul.quests.scoreboards;

import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.questers.events.QuesterLeaveEvent;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class ScoreboardManager implements Listener, QuestsHandler {

	static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.ScoreboardManager");

	private final @NotNull BeautyQuests plugin;
	private final @NotNull File configFile;
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

	public ScoreboardManager(@NotNull BeautyQuests plugin, @NotNull File configFile) {
		this.plugin = plugin;
		this.configFile = configFile;
	}

	public List<ScoreboardLine> getScoreboardLines(){
		return lines;
	}

	public int getQuestChangeTime(){
		return changeTime;
	}

	public boolean hideEmptyScoreboard(){
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

		Scoreboard scoreboard = new Scoreboard(p, this, plugin.getQuesterManager(), plugin.getQuestsManager());
		scoreboards.put(p, scoreboard);

		Boolean forceHidden = forceHiddenState.remove(p.getUniqueId());
		if (forceHidden != null && forceHidden.booleanValue()) scoreboard.hide(true);
	}

	@Override
	public void load() {
		if (!QuestsConfiguration.getConfig().getQuestsConfig().scoreboards())
			return;
		if (plugin.isUnitTesting())
			return;

		try {
			new FastBoard(null); // trigger class initialization
		}catch (ExceptionInInitializerError ex) {
			throw new IllegalStateException("The Scoreboard util cannot load, probably due to an incompatible server version.", ex);
		}catch (NullPointerException ex) {} // as we pass a null player to initialize, it will throw NPE

		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

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
				LOGGER.warning("Limit of 15 scoreboard lines reached - please delete some in scoreboard.yml");
				break;
			}
			try {
				lines.add(ScoreboardLine.deserialize((Map<String, Object>) map));
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		LOGGER.debug("Registered {0} lines in scoreboard", lines.size());

		scoreboards = new HashMap<>();
		forceHiddenState = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void unload(){
		if (scoreboards == null)
			return;
		HandlerList.unregisterAll(this);
		for (Scoreboard s : scoreboards.values()) s.cancel();
		if (!scoreboards.isEmpty())
			LOGGER.debug("{0} scoreboards deleted.", scoreboards.size());
		scoreboards.clear();
		scoreboards = null;
		forceHiddenState.clear();
		forceHiddenState = null;
	}

	@EventHandler
	public void onAccountJoin(QuesterJoinEvent e) {
		create(e.getPlayer());
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onAccountLeave(QuesterLeaveEvent e) {
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
	public void questFinish(Quester quester, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(quester, x -> x.questRemove(quest, quester));
	}

	@Override
	public void questReset(Quester quester, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(quester, x -> x.questRemove(quest, quester));
	}

	@Override
	public void questUpdated(Quester quester, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(quester, x -> x.setShownQuest(quest, quester, true));
	}

	@Override
	public void questStart(Quester quester, Quest quest) {
		if (!quest.isScoreboardEnabled()) return;
		questEvent(quester, x -> x.questAdd(quest, quester));
	}

	private void questEvent(Quester quester, Consumer<Scoreboard> consumer) {
		for (Player player : quester.getOnlinePlayers()) {
			Scoreboard scoreboard = scoreboards.get(player);
			if (scoreboard != null) consumer.accept(scoreboard);
		}
	}

}
