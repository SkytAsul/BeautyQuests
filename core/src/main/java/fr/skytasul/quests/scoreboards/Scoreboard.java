package fr.skytasul.quests.scoreboards;

import fr.mrmicky.fastboard.FastBoard;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scoreboard extends BukkitRunnable implements Listener {

	private static final Pattern QUEST_PLACEHOLDER = Pattern.compile("\\{quest_(.+)\\}");
	private static final int maxLength = 1024;

	private final Player p;
	private final ScoreboardManager manager;
	private final QuesterManager questerManager;

	private FastBoard board;
	private LinkedList<Line> lines = new LinkedList<>();

	private List<QuestEntry> launched;
	private List<QuestEntry> shown;
	private OptionalInt shownIndex = OptionalInt.empty();

	private boolean hid = false;
	private boolean hidForce = false;
	private int changeTime = 1;

	Scoreboard(Player player, ScoreboardManager manager, QuesterManager questerManager, QuestsManager questsManager) {
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		this.p = player;
		this.manager = manager;
		this.questerManager = questerManager;

		for (ScoreboardLine line : manager.getScoreboardLines()) {
			lines.add(new Line(line));
		}

		launched = questerManager.getPlayerQuesters(player).stream().flatMap(quester -> {
			return questsManager.getQuestsStarted(quester, false, true).stream()
					.map(quest -> new QuestEntry(quest, quester));
		}).collect(Collectors.toList());
		shown = new ArrayList<>(launched); // at the beginning, all quests are shown

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

			if (shown.isEmpty()) {
				shownIndex = OptionalInt.empty();
				if (manager.hideEmptyScoreboard()) {
					if (board != null) deleteBoard();
					return;
				}
			}
			if (board == null) initScoreboard();

			if (!shown.isEmpty()) {
				int newId;
				if (shownIndex.isEmpty()) {
					newId = 0;
				} else {
					newId = (shownIndex.getAsInt() + 1) % shown.size();
				}
				if (shownIndex.isEmpty() || newId != shownIndex.getAsInt()) {
					shownIndex = OptionalInt.of(newId);
					refreshQuestsLines(false);
				}
			}
		}
		if (board == null) return;

		updateBoard(false, true);
	}

	protected Optional<QuestEntry> getShown() {
		if (shownIndex.isEmpty())
			return Optional.empty();
		return Optional.of(shown.get(shownIndex.getAsInt()));
	}

	public Optional<QuestEntry> getEntry(@NotNull Quest quest, @NotNull Quester quester) {
		return launched.stream().filter(entry -> entry.applies(quest, quester)).findAny();
	}

	private boolean hasPinnedEntry() {
		return launched.stream().anyMatch(entry -> entry.pinned);
	}

	private void handleEmptyShown() {
		if (!launched.isEmpty()) {
			shown.addAll(launched);
			shownIndex = OptionalInt.of(0);
		} else {
			shownIndex = OptionalInt.empty();
			refreshQuestsLines(true); // to hide the scoreboard
		}
	}

	private void removeShown(QuestEntry entry) {
		if (!shown.contains(entry))
			return;

		int index = shown.indexOf(entry);
		shown.remove(index);

		if (shownIndex.isEmpty())
			return;

		if (shown.isEmpty()) {
			handleEmptyShown();
			return;
		}

		if (index == shownIndex.getAsInt()) {
			// removing the shown quest
			shownIndex = OptionalInt.of(index % shown.size());
			refreshQuestsLines(true);
		} else if (index < shownIndex.getAsInt()) {
			// removing a quest before the shown quest
			shownIndex = OptionalInt.of(shownIndex.getAsInt() - 1);
		}
	}

	protected void questAdd(@NotNull Quest quest, @NotNull Quester quester) {
		var entry = new QuestEntry(quest, quester);
		launched.add(entry);

		if (hasPinnedEntry())
			return;
		int index = shownIndex.orElse(-1) + 1;
		shown.add(index, entry);
		shownIndex = OptionalInt.of(index);
		resetChangeTime();
		refreshQuestsLines(true);
	}

	protected void questRemove(@NotNull Quest quest) {
		for (var iterator = launched.iterator(); iterator.hasNext();) {
			QuestEntry entry = iterator.next();
			if (entry.quest.equals(quest)) {
				iterator.remove();
				shown.remove(entry);
			}
		}
		if (shown.isEmpty())
			handleEmptyShown();
	}

	protected void questRemove(@NotNull Quest quest, @NotNull Quester quester) {
		var entryOpt = getEntry(quest, quester);
		if (entryOpt.isEmpty())
			return;

		launched.remove(entryOpt.get());
		removeShown(entryOpt.get());
	}

	protected void questEdited(Quest newQuest, Quest oldQuest) {
		for (var quester : questerManager.getPlayerQuesters(p)) {
			if (!newQuest.getQuesterStrategy().isQuesterApplicable(quester))
				continue;

			var entryOpt = getEntry(oldQuest, quester);
			if (entryOpt.isEmpty()) {
				// if scoreboard has been enabled during quest edition,
				// we add the quest to the player list
				if (newQuest.isScoreboardEnabled() && newQuest.hasStarted(quester)) {
					var entry = new QuestEntry(newQuest, quester);
					launched.add(entry);
					if (!hasPinnedEntry())
						shown.add(entry);
				}
				continue;
			}

			// if scoreboard has been disabled during quest edition,
			// we remove the quest from the player list as it should no longer be displayed
			if (!newQuest.isScoreboardEnabled()) {
				questRemove(oldQuest, quester);
				continue;
			}

			entryOpt.get().quest = newQuest;
			if (shownIndex.orElse(-2) == shown.indexOf(entryOpt.get()))
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
		if (board == null && !(shown.isEmpty() && manager.hideEmptyScoreboard())) {
			initScoreboard();
			updateBoard(true, false);
		}
	}

	private void deleteBoard() {
		board.delete();
		board = null;
		for (Line line : lines) line.reset();
	}

	public void setShownQuest(@NotNull Quest quest, @NotNull Quester quester, boolean errorWhenUnknown) {
		if (!quest.isScoreboardEnabled()) return;

		var entryOpt = getEntry(quest, quester);
		if (entryOpt.isEmpty()) {
			if (errorWhenUnknown)
				throw new IllegalArgumentException("Quest is not running for player.");
		} else if (!hasPinnedEntry() || entryOpt.get().isPinned()) {
			if (!getShown().equals(entryOpt)) {
				shownIndex = OptionalInt.of(shown.indexOf(entryOpt.get()));
				resetChangeTime();
			}
			refreshQuestsLines(true);
		}
	}

	protected void resetChangeTime() {
		changeTime = manager.getQuestChangeTime();
	}

	public void refreshQuestsLines(boolean updateBoard) {
		if (!manager.refreshLines()) return;
		lines.stream().filter(line -> line.hasQuestPlaceholders).forEach(line -> line.willRefresh = true);
		if (board == null || shown.isEmpty()) {
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
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error occured while refreshing scoreboard line " + i + " for " + p.getName(), ex);
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

	public class QuestEntry {
		private final @NotNull Quester quester;
		private @NotNull Quest quest; // not final because the quest can be edited

		private boolean pinned = false;

		private QuestEntry(@NotNull Quest quest, @NotNull Quester quester) {
			this.quest = quest;
			this.quester = quester;
		}

		public void setPinned(boolean pinned) {
			if (pinned) {
				if (!hasPinnedEntry())
					shown.clear();
				shown.add(this);
				shownIndex = OptionalInt.of(shown.indexOf(this));
				resetChangeTime();
				refreshQuestsLines(true);
				this.pinned = true;
			} else {
				this.pinned = false;
				removeShown(this);
			}
		}

		public boolean isPinned() {
			return pinned;
		}

		public boolean applies(Quest quest, Quester quester) {
			return this.quest.equals(quest) && this.quester.equals(quester);
		}
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
				try {
					if (hasQuestPlaceholders)
						text = formatQuestPlaceholders(text);

					if (text == null) {
						// in this case, the line must not be displayed
						lines = Collections.emptyList();

						if (lastValue == null)
							return false;
					} else {
						text = MessageUtils.finalFormat(text, null, PlaceholdersContext.of(p, true, null));
						if (text.equals(lastValue))
							return false;

						lines = ChatColorUtils.wordWrap(text, param.getMaxLength() == 0 ? 30 : param.getMaxLength(),
								maxLength);
					}
				} catch (Exception ex) {
					ScoreboardManager.LOGGER.warning("Failed to refresh line {0} for player {1}", ex, text, p.getName());
					lines = List.of("§cerror");
				}
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

		public void setCustomValue(String value) {
			customValue = value;
			computeHasQuestPlaceholders();
		}

		private void computeHasQuestPlaceholders() {
			hasQuestPlaceholders = QUEST_PLACEHOLDER.matcher(getValue()).find();
		}

		private String formatQuestPlaceholders(String text) {
			var shownOpt = getShown();

			StringBuffer textBuffer = new StringBuffer();
			Matcher matcher = QUEST_PLACEHOLDER.matcher(text);
			QuestDescriptionContext lazyContext = null;
			while (matcher.find()) {
				String descriptionId = matcher.group(1);
				String replacement;
				if (descriptionId.equals("name")) {
					replacement = shownOpt.map(entry -> entry.quest.getName()).orElse(Lang.SCOREBOARD_NONE_NAME.toString());
				} else {
					if (shownOpt.isEmpty()) {
						replacement = descriptionId.equals("advancement")
								? Lang.SCOREBOARD_NONE_DESC.toString() // kept for consistency with pre-0.20
								: Lang.SCOREBOARD_NONE.toString();
					} else {
						Optional<QuestDescriptionProvider> optionalDescription = shownOpt.get().quest.getDescriptions()
								.stream()
								.filter(description -> description.getDescriptionId().equals(descriptionId))
								.findFirst();
						if (optionalDescription.isPresent()) {
							if (lazyContext == null)
								lazyContext = new QuestDescriptionContext(
										QuestsConfiguration.getConfig().getQuestDescriptionConfig(),
										shownOpt.get().quest, p, shownOpt.get().quester, PlayerListCategory.IN_PROGRESS,
										DescriptionSource.SCOREBOARD);
							List<String> descriptionStrings = optionalDescription.get().provideDescription(lazyContext);
							replacement = descriptionStrings == null ? "" : String.join("\n", descriptionStrings);
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
				matcher.appendReplacement(textBuffer, Matcher.quoteReplacement(replacement));
			}
			matcher.appendTail(textBuffer);
			return textBuffer.toString();
		}

	}

}