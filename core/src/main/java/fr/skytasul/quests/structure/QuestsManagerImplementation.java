package fr.skytasul.quests.structure;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.QuestsManager;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestsManagerImplementation implements QuestsManager {

	protected static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.Quests");

	private final List<QuestImplementation> quests = new ArrayList<>();
	private final AtomicInteger lastID = new AtomicInteger();

	private final BeautyQuests plugin;
	private final File saveFolder;

	public QuestsManagerImplementation(BeautyQuests plugin, int lastID, File saveFolder) throws IOException {
		this.plugin = plugin;
		this.lastID.set(lastID);
		this.saveFolder = saveFolder;

		try (Stream<Path> files = Files.walk(saveFolder.toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
			for (var path : (Iterable<Path>) files.filter(Files::isRegularFile)
					.filter(path -> !path.getFileName().toString().contains("backup"))
					.filter(path -> "yml".equalsIgnoreCase(
							Utils.getFilenameExtension(path.getFileName().toString()).orElse(null)))
					::iterator) {
						plugin.resetLoadingFailure();
				try {
					File file = path.toFile();
					QuestImplementation quest = QuestImplementation.loadFromFile(this, file);
					addQuest(quest);
					if (plugin.hasLoadingFailed())
						plugin.createQuestBackup(path, "Error when loading quest.");
				}catch (Exception ex) {
					LOGGER.severe("An error occurred while loading quest file {0}", ex,
							path.getFileName());
				}
			}
		}
		LOGGER.debug("Finished loading {} quests", quests.size());
	}

	public @NotNull BeautyQuests getPlugin() {
		return plugin;
	}

	public int getFreeQuestID() {
		int id = lastID.incrementAndGet();

		if (quests.stream().noneMatch(quest -> quest.getId() == id))
			return id;

		LOGGER.warning("Quest id {0} already taken, this should not happen.", id);
		return getFreeQuestID();
	}

	public int getLastID() {
		return lastID.get();
	}

	public int updateAll() {
		int updated = 0;
		for (QuestImplementation quest : quests) {
			try {
				if (quest.saveToFile()) {
					updated++;
					LOGGER.debug("Saving quest {0} to {1}", quest.getId(), quest.getFile());
				} else {
					LOGGER.debug("Quest {0} was already up-to-date", quest.getId());
				}
			} catch (Exception ex) {
				LOGGER.severe("Failed to save quest {0}", ex, quest.getId());
			}
		}
		return updated;
	}

	public @NotNull QuestImplementation createQuest(OptionalInt id) {
		if (id.isPresent()) {
			final int potentialId = id.getAsInt();
			if (QuestsAPI.getAPI().getQuestsManager().getQuests().stream().anyMatch(x -> x.getId() == potentialId)) {
				LOGGER.warning("Cannot create quest with custom ID {0} because another quest with this ID already exists.",
						potentialId);
				id = OptionalInt.empty();
			} else {
				LOGGER.warning("A quest will be created with custom ID {0}.", id.getAsInt());
			}
		}
		int finalId = id.orElseGet(this::getFreeQuestID);
		return new QuestImplementation(this, finalId, new File(saveFolder, finalId + ".yml"));
	}

	@Override
	public @NotNull File getSaveFolder() {
		return saveFolder;
	}

	@Override
	public @NotNull List<QuestImplementation> getQuests() {
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
				LOGGER.severe("An error ocurred when unloading quest {}", ex, quest.getId());
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
	public @NotNull @Unmodifiable List<QuestImplementation> getQuestsFinished(@NotNull Quester acc, boolean hide) {
		return quests
				.stream()
				.filter(quest -> !(hide && quest.isHidden(QuestVisibilityLocation.TAB_FINISHED)) && quest.hasFinished(acc))
				.collect(Collectors.toList());
	}

	@Override
	public @NotNull @Unmodifiable List<QuestImplementation> getQuestsNotStarted(@NotNull Quester acc, boolean hide,
			boolean clickableAndRedoable) {
		return quests
				.stream()
				.filter(quest -> {
					if (!quest.getQuesterStrategy().isQuesterApplicable(acc))
						return false;
					if (hide && quest.isHidden(QuestVisibilityLocation.TAB_NOT_STARTED)) return false;
					if (quest.hasStarted(acc)) return false;
					if (!quest.hasFinished(acc)) return true;
					return clickableAndRedoable && quest.isRepeatable() && quest.getOptionValueOrDef(OptionStartable.class) && quest.testTimer(acc, false);
				})
				.collect(Collectors.toList());
	}

}
