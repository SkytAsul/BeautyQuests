package fr.skytasul.quests.structure.pools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.NPCStarter;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class QuestPool implements Comparable<QuestPool> {
	
	private final int id;
	
	private final int npcID;
	private final String hologram;
	private final int maxQuests;
	private final boolean redoAllowed;
	private final long timeDiff;
	
	NPCStarter starter;
	List<Quest> quests = new ArrayList<>();
	
	QuestPool(int id, int npcID, String hologram, int maxQuests, boolean redoAllowed, long timeDiff) {
		this.id = id;
		this.npcID = npcID;
		this.hologram = hologram;
		this.maxQuests = maxQuests;
		this.redoAllowed = redoAllowed;
		this.timeDiff = timeDiff;
		if (npcID >= 0) {
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
			if (npc == null) return;
			NPCStarter starter = BeautyQuests.getInstance().getNPCs().get(npc);
			if (starter == null) {
				starter = new NPCStarter(npc);
				BeautyQuests.getInstance().getNPCs().put(npc, starter);
			}
			starter.addPool(this);
		}else BeautyQuests.logger.warning("Unknown NPC " + npcID + " for quest pool #" + id);
	}
	
	public int getID() {
		return id;
	}
	
	public int getNPCID() {
		return npcID;
	}
	
	public String getHologram() {
		return hologram;
	}
	
	public int getMaxQuests() {
		return maxQuests;
	}
	
	public boolean isRedoAllowed() {
		return redoAllowed;
	}
	
	public long getTimeDiff() {
		return timeDiff;
	}
	
	public void addQuest(Quest quest) {
		quests.add(quest);
	}
	
	public void removeQuest(Quest quest) {
		quests.remove(quest);
	}
	
	@Override
	public int compareTo(QuestPool o) {
		return Integer.compare(id, o.id);
	}
	
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.CHEST, Lang.poolItemName.format(id), Lang.poolItemDescription.format(npcID, maxQuests, redoAllowed ? Lang.Enabled : Lang.Disabled, timeDiff, hologram == null ? Lang.PoolHologramText.toString() + " " + Lang.defaultValue : hologram), "§7(" + quests.size()
				+ " quests)");
	}
	
	public void questCompleted(PlayerAccount acc, Quest quest) {
		PlayerPoolDatas poolDatas = acc.getPoolDatas(this);
		poolDatas.getCompletedQuests().add(quest.getID());
		poolDatas.updatedCompletedQuests();
	}
	
	public boolean canGive(Player p, PlayerAccount acc) {
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		if (datas.getLastGive() + timeDiff > System.currentTimeMillis()) return false;
		
		List<Quest> notDoneQuests = quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList());
		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!redoAllowed) return false;
			return quests.stream().anyMatch(quest -> quest.isRepeatable() && quest.isLauncheable(p, acc, false));
		}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) return false;
		
		return notDoneQuests.stream().anyMatch(quest -> quest.isLauncheable(p, acc, false));
	}
	
	public String give(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		long time = (datas.getLastGive() + timeDiff) - System.currentTimeMillis();
		if (time > 0) return Lang.POOL_NO_TIME.format(Utils.millisToHumanString(time));
		
		List<Quest> notDoneQuests = quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList());
		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!redoAllowed) return Lang.POOL_ALL_COMPLETED.toString();
			notDoneQuests = quests.stream().filter(Quest::isRepeatable).collect(Collectors.toList());
			if (notDoneQuests.isEmpty()) return Lang.POOL_ALL_COMPLETED.toString();
			datas.setCompletedQuests(quests.stream().filter(quest -> !quest.isRepeatable()).map(Quest::getID).collect(Collectors.toSet()));
		}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) return Lang.POOL_NO_AVAILABLE.toString();
		
		List<Quest> available = notDoneQuests.stream().filter(quest -> quest.isLauncheable(p, acc, false)).collect(Collectors.toList());
		if (available.isEmpty()) {
			return Lang.POOL_NO_AVAILABLE.toString();
		}else {
			Quest quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
			quest.start(p);
			datas.setLastGive(System.currentTimeMillis());
			return "started quest #" + quest.getID();
		}
	}
	
	void unload() {
		if (starter != null) starter.removePool(this);
	}
	
	public void save(ConfigurationSection config) {
		config.set("hologram", hologram);
		config.set("maxQuests", maxQuests);
		config.set("redoAllowed", redoAllowed);
		config.set("timeDiff", timeDiff);
		config.set("npcID", npcID);
	}
	
	public static QuestPool deserialize(int id, ConfigurationSection config) {
		return new QuestPool(id, config.getInt("npcID"), config.getString("hologram"), config.getInt("maxQuests"), config.getBoolean("redoAllowed"), config.getLong("timeDiff"));
	}
	
}
