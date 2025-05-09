package fr.skytasul.quests.structure;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.options.OptionStarterNPC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestsManagerImplementation implements QuestsManager {

	private final List<QuestImplementation> quests = new ArrayList<>();
	private final AtomicInteger lastID = new AtomicInteger();

	private final BeautyQuests plugin;
	private final File saveFolder;

	public QuestsManagerImplementation(BeautyQuests plugin, int lastID, File saveFolder) throws IOException {
		this.plugin = plugin;
		this.lastID.set(lastID);
		this.saveFolder = saveFolder;

		try (Stream<Path> files = Files.walk(saveFolder.toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
			files.filter(Files::isRegularFile).filter(path -> !path.getFileName().toString().contains("backup")).filter(path -> "yml".equalsIgnoreCase(Utils.getFilenameExtension(path.getFileName().toString()).orElse(null))).forEach(path -> {
				BeautyQuests.getInstance().resetLoadingFailure();
				try {
					File file = path.toFile();
					QuestImplementation quest = QuestImplementation.loadFromFile(file);
					if (quest != null) {
						addQuest(quest);
						if (BeautyQuests.getInstance().hasLoadingFailed())
							plugin.createQuestBackup(path, "Error when loading quest.");
					}else plugin.getLogger().severe("Quest from file " + file.getName() + " not activated");
				}catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while loading quest file " + path.getFileName(), ex);
				}
			});
		}
	}

	public int getFreeQuestID() {
		int id = getLastID();

		if (quests.stream().noneMatch(quest -> quest.getId() == id)) return id;

		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Quest id " + id + " already taken, this should not happen.");
		incrementLastID();
		return getFreeQuestID();
	}

	public int getLastID() {
		return lastID.get();
	}

	public int incrementLastID() {
		return lastID.incrementAndGet();
	}

	public int updateAll() throws IOException {
		int updated = 0;
		for (QuestImplementation quest : quests) {
			if (quest.saveToFile()) updated++;
		}
		return updated;
	}

	public @NotNull BeautyQuests getPlugin() {
		return plugin;
	}

	@Override
	public @NotNull File getSaveFolder() {
		return saveFolder;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public @NotNull List<Quest> getQuests() {
		return (List) quests;
	}

	public @NotNull List<QuestImplementation> getQuestsRaw() {
		return quests;
	}

	public int getQuestsAmount() {
		return quests.size();
	}

	@Override
	public @Nullable QuestImplementation getQuest(int id) {
		return quests.stream().filter(x -> x.getId() == id).findAny().orElse(null);
	}

	public void unloadQuests() {
		for (QuestImplementation quest : quests) {
			try {
				quest.unload();
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error ocurred when unloading quest " + quest.getId(), ex);
			}
		}
	}

	public void removeQuest(@NotNull Quest quest) {
		quests.remove(quest);
	}

	@Override
	public void addQuest(@NotNull Quest quest) {
		QuestImplementation qu = (QuestImplementation) quest;
		lastID.set(Math.max(lastID.get(), quest.getId()));
		quests.add(qu);
		if (quest.hasOption(OptionStarterNPC.class)) {
			BqNpcImplementation npc = (BqNpcImplementation) quest.getOptionValueOrDef(OptionStarterNPC.class);
			if (npc != null) npc.addQuest(quest);
		}
		qu.load();
	}

	@Override
	public @NotNull @Unmodifiable List<Quest> getQuestsStarted(Quester acc) {
		return getQuestsStarted(acc, false, false);
	}

	@Override
	public @NotNull @Unmodifiable List<Quest> getQuestsStarted(@NotNull Quester acc, boolean hide,
			boolean withoutScoreboard) {
		return acc.getDataHolder().getAllQuestsData()
				.stream()
				.filter(QuesterQuestData::hasStarted)
				.map(QuesterQuestData::getQuest)
				.filter(Objects::nonNull)
				.filter(Quest::isValid)
				.filter(quest -> !hide || !quest.isHidden(QuestVisibilityLocation.TAB_IN_PROGRESS))
				.filter(quest -> !withoutScoreboard || quest.isScoreboardEnabled())
				.collect(Collectors.toList());
	}

	@Override
	public void updateQuestsStarted(@NotNull Quester acc, boolean withoutScoreboard, @NotNull List<Quest> list) {
		for (Iterator<Quest> iterator = list.iterator(); iterator.hasNext();) {
			QuestImplementation existing = (QuestImplementation) iterator.next();
			if (!existing.hasStarted(acc) || (withoutScoreboard && !existing.isScoreboardEnabled())) iterator.remove();
		}

		for (QuestImplementation qu : quests) {
			if (withoutScoreboard && !qu.isScoreboardEnabled()) continue;
			if (!list.contains(qu) && qu.hasStarted(acc)) list.add(qu);
		}
	}

	@Override
	public int getStartedSize(@NotNull Quester acc) {
		return (int) quests
				.stream()
				.filter(quest -> !quest.canBypassLimit() && quest.hasStarted(acc))
				.count();
	}

	@Override
	public @NotNull @Unmodifiable List<Quest> getQuestsFinished(@NotNull Quester acc, boolean hide) {
		return quests
				.stream()
				.filter(quest -> !(hide && quest.isHidden(QuestVisibilityLocation.TAB_FINISHED)) && quest.hasFinished(acc))
				.collect(Collectors.toList());
	}

	@Override
	public @NotNull @Unmodifiable List<Quest> getQuestsNotStarted(@NotNull Quester acc, boolean hide,
			boolean clickableAndRedoable) {
		return quests
				.stream()
				.filter(quest -> {
					if (hide && quest.isHidden(QuestVisibilityLocation.TAB_NOT_STARTED)) return false;
					if (quest.hasStarted(acc)) return false;
					if (!quest.hasFinished(acc)) return true;
					return clickableAndRedoable && quest.isRepeatable() && quest.getOptionValueOrDef(OptionStartable.class) && quest.testTimer(acc, false);
				})
				.collect(Collectors.toList());
	}

}
