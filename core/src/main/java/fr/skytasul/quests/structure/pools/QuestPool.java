package fr.skytasul.quests.structure.pools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.XMaterial;

public class QuestPool {
	
	private final int id;
	
	private String name;
	private int maxQuests = 1;
	private boolean redoAllowed = true;
	private long timeDiff = TimeUnit.DAYS.toMillis(1);
	
	private List<Quest> quests = new ArrayList<>();
	
	QuestPool(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getMaxQuests() {
		return maxQuests;
	}
	
	public void setMaxQuests(int maxQuests) {
		this.maxQuests = maxQuests;
	}
	
	public boolean isRedoAllowed() {
		return redoAllowed;
	}
	
	public void setRedoAllowed(boolean redoAllowed) {
		this.redoAllowed = redoAllowed;
	}
	
	public long getTimeDiff() {
		return timeDiff;
	}
	
	public void setTimeDiff(long timeDiff) {
		this.timeDiff = timeDiff;
	}
	
	public void addQuest(Quest quest) {
		quests.add(quest);
	}
	
	public void removeQuest(Quest quest) {
		quests.remove(quest);
	}
	
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.CHEST, name + " #" + id, "Max quests: " + maxQuests, "Can redo: " + redoAllowed, "Time between quests (ms): " + timeDiff);
	}
	
	public String give(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		if (datas.getLastGive() + timeDiff > System.currentTimeMillis()) return "no time";
		
		List<Quest> notDoneQuests = quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList());
		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!redoAllowed) return "all quests completed";
			notDoneQuests = quests.stream().filter(Quest::isRepeatable).collect(Collectors.toList());
			if (notDoneQuests.isEmpty()) return "all quests completed";
			datas.setCompletedQuests(quests.stream().filter(quest -> !quest.isRepeatable()).map(Quest::getID).collect(Collectors.toList()));
		}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) return "max quest limit";
		
		List<Quest> available = quests.stream().filter(quest -> quest.isLauncheable(p, false)).collect(Collectors.toList());
		if (available.isEmpty()) {
			return "no quest available";
		}else {
			Quest quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
			quest.start(p);
			return "started quest #" + quest.getID();
		}
	}
	
	public void load(ConfigurationSection config) {
		name = config.getString("name");
		maxQuests = config.getInt("maxQuests");
		redoAllowed = config.getBoolean("redoAllowed");
		timeDiff = config.getLong("timeDiff");
	}
	
	public void save(ConfigurationSection config) {
		config.set("name", name);
		config.set("maxQuests", maxQuests);
		config.set("redoAllowed", redoAllowed);
		config.set("timeDiff", timeDiff);
	}
	
}
