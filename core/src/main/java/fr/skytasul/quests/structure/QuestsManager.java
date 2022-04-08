package fr.skytasul.quests.structure;

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

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.options.OptionStartable;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.utils.Utils;

public class QuestsManager implements Iterable<Quest> {
	
	private final List<Quest> quests = new ArrayList<>();
	private final AtomicInteger lastID = new AtomicInteger();
	
	private final BeautyQuests plugin;
	
	public QuestsManager(BeautyQuests plugin, int lastID, File saveFolder) throws IOException {
		this.plugin = plugin;
		this.lastID.set(lastID);
		
		try (Stream<Path> files = Files.walk(saveFolder.toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
			files.filter(Files::isRegularFile).filter(path -> !path.getFileName().toString().contains("backup")).filter(path -> "yml".equalsIgnoreCase(Utils.getFilenameExtension(path.getFileName().toString()).orElse(null))).forEach(path -> {
				BeautyQuests.loadingFailure = false;
				try {
					File file = path.toFile();
					Quest quest = Quest.loadFromFile(file);
					if (quest != null) {
						addQuest(quest);
						if (BeautyQuests.loadingFailure) plugin.createQuestBackup(path, "Error when loading quest.");
					}else plugin.getLogger().severe("Quest from file " + file.getName() + " not activated");
				}catch (Exception ex) {
					BeautyQuests.logger.severe("An error occurred while loading quest file " + path.getFileName(), ex);
				}
			});
		}
	}
	
	public int getLastID() {
		return lastID.get();
	}
	
	public int incrementLastID() {
		return lastID.incrementAndGet();
	}
	
	public List<Quest> getQuests() {
		return quests;
	}
	
	public int getQuestsAmount() {
		return quests.size();
	}
	
	@Override
	public Iterator<Quest> iterator() {
		return quests.iterator();
	}
	
	public Quest getQuest(int id) {
		return quests.stream().filter(x -> x.getID() == id).findAny().orElse(null);
	}
	
	public void unloadQuests() {
		for (Quest quest : quests) {
			try {
				quest.unload();
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An error ocurred when unloading quest " + quest.getID(), ex);
			}
		}
	}
	
	public void removeQuest(Quest quest) {
		quests.remove(quest);
		if (quest.hasOption(OptionStarterNPC.class)) {
			quest.getOption(OptionStarterNPC.class).getValue().removeQuest(quest);
		}
	}
	
	public void addQuest(Quest quest) {
		lastID.set(Math.max(lastID.get(), quest.getID()));
		quests.add(quest);
		if (quest.hasOption(OptionStarterNPC.class)) {
			BQNPC npc = quest.getOptionValueOrDef(OptionStarterNPC.class);
			if (npc != null) npc.addQuest(quest);
		}
		quest.load();
	}
	
	public List<Quest> getQuestsStarted(PlayerAccount acc) {
		return getQuestsStarted(acc, false);
	}
	
	public List<Quest> getQuestsStarted(PlayerAccount acc, boolean withoutScoreboard) {
		return acc.getQuestsDatas()
				.stream()
				.filter(PlayerQuestDatas::hasStarted)
				.map(PlayerQuestDatas::getQuest)
				.filter(Objects::nonNull)
				.filter(quest -> !withoutScoreboard || quest.isScoreboardEnabled())
				.collect(Collectors.toList());
	}
	
	public void updateQuestsStarted(PlayerAccount acc, boolean withoutScoreboard, List<Quest> list) {
		for (Quest qu : quests) {
			if (withoutScoreboard && !qu.isScoreboardEnabled()) continue;
			boolean contains = list.contains(qu);
			if (qu.hasStarted(acc)) {
				if (!list.contains(qu)) list.add(qu);
			}else if (contains) list.remove(qu);
		}
	}
	
	public int getStartedSize(PlayerAccount acc) {
		return (int) quests
				.stream()
				.filter(quest -> !quest.canBypassLimit() && quest.hasStarted(acc))
				.count();
	}
	
	public List<Quest> getQuestsFinished(PlayerAccount acc, boolean hide) {
		return quests
				.stream()
				.filter(quest -> !(hide && quest.isHidden()) && quest.hasFinished(acc))
				.collect(Collectors.toList());
	}
	
	public List<Quest> getQuestsNotStarted(PlayerAccount acc, boolean hide, boolean clickableAndRedoable) {
		return quests
				.stream()
				.filter(quest -> {
					if (hide && quest.isHidden()) return false;
					if (quest.hasStarted(acc)) return false;
					if (!quest.hasFinished(acc)) return true;
					return clickableAndRedoable && quest.isRepeatable() && quest.getOptionValueOrDef(OptionStartable.class) && quest.testTimer(acc, false);
				})
				.collect(Collectors.toList());
	}
	
}
