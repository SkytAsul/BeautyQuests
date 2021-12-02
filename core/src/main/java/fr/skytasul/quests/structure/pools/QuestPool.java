package fr.skytasul.quests.structure.pools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.NPCStarter;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class QuestPool implements Comparable<QuestPool> {
	
	private final int id;
	
	private final int npcID;
	private final String hologram;
	private final int maxQuests;
	private final int questsPerLaunch;
	private final boolean redoAllowed;
	private final long timeDiff;
	private final boolean avoidDuplicates;
	private final List<AbstractRequirement> requirements;
	
	NPCStarter starter;
	List<Quest> quests = new ArrayList<>();
	
	QuestPool(int id, int npcID, String hologram, int maxQuests, int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates, List<AbstractRequirement> requirements) {
		this.id = id;
		this.npcID = npcID;
		this.hologram = hologram;
		this.maxQuests = maxQuests;
		this.questsPerLaunch = questsPerLaunch;
		this.redoAllowed = redoAllowed;
		this.timeDiff = timeDiff;
		this.avoidDuplicates = avoidDuplicates;
		this.requirements = requirements;
		
		if (npcID >= 0) {
			BQNPC npc = QuestsAPI.getNPCsManager().getById(npcID);
			if (npc == null) return;
			starter = BeautyQuests.getInstance().getNPCs().get(npc);
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
	
	public int getQuestsPerLaunch() {
		return questsPerLaunch;
	}
	
	public boolean isRedoAllowed() {
		return redoAllowed;
	}
	
	public long getTimeDiff() {
		return timeDiff;
	}
	
	public boolean doAvoidDuplicates() {
		return avoidDuplicates;
	}
	
	public List<AbstractRequirement> getRequirements() {
		return requirements;
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
	
	public ItemStack getItemStack(String action) {
		return ItemUtils.item(XMaterial.CHEST, Lang.poolItemName.format(id),
				Lang.poolItemNPC.format(npcID),
				Lang.poolItemMaxQuests.format(maxQuests),
				Lang.poolItemQuestsPerLaunch.format(questsPerLaunch),
				Lang.poolItemRedo.format(redoAllowed ? Lang.Enabled : Lang.Disabled),
				Lang.poolItemTime.format(Utils.millisToHumanString(timeDiff)),
				Lang.poolItemHologram.format(hologram == null ? "\n§7 > " + Lang.PoolHologramText.toString() + "\n§7 > " + Lang.defaultValue : hologram),
				Lang.poolItemAvoidDuplicates.format(avoidDuplicates ? Lang.Enabled : Lang.Disabled),
				"§7" + Lang.requirements.format(requirements.size()),
				Lang.poolItemQuestsList.format(quests.size(), quests.stream().map(x -> "#" + x.getID()).collect(Collectors.joining(", "))),
				"", action);
	}
	
	public void resetPlayer(PlayerAccount acc) {
		acc.removePoolDatas(this);
	}
	
	public void resetPlayerTimer(PlayerAccount acc) {
		if (!acc.hasPoolDatas(this)) return;
		acc.getPoolDatas(this).setLastGive(0);
	}
	
	public void questCompleted(PlayerAccount acc, Quest quest) {
		if (!avoidDuplicates) return;
		PlayerPoolDatas poolDatas = acc.getPoolDatas(this);
		poolDatas.getCompletedQuests().add(quest.getID());
		poolDatas.updatedCompletedQuests();
	}
	
	public boolean canGive(Player p, PlayerAccount acc) {
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		if (datas.getLastGive() + timeDiff > System.currentTimeMillis()) return false;
		
		if (!requirements.stream().allMatch(x -> x.test(p))) return false;
		
		List<Quest> notDoneQuests = avoidDuplicates ? quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList()) : quests;
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
		
		List<Quest> started = new ArrayList<>(questsPerLaunch);
		for (int i = 0; i < questsPerLaunch; i++) {
			for (AbstractRequirement requirement : requirements) {
				if (!requirement.test(p)) {
					requirement.sendReason(p);
					return null;
				}
			}
			
			List<Quest> notDoneQuests = avoidDuplicates ? quests.stream().filter(quest -> !datas.getCompletedQuests().contains(quest.getID())).collect(Collectors.toList()) : quests;
			if (notDoneQuests.isEmpty()) { // all quests completed
				if (!redoAllowed) {
					if (started.isEmpty()) return Lang.POOL_ALL_COMPLETED.toString();
					break;
				}
				notDoneQuests = quests.stream().filter(Quest::isRepeatable).collect(Collectors.toList());
				if (notDoneQuests.isEmpty()) {
					if (started.isEmpty()) return Lang.POOL_ALL_COMPLETED.toString();
					break;
				}
				datas.setCompletedQuests(quests.stream().filter(quest -> !quest.isRepeatable()).map(Quest::getID).collect(Collectors.toSet()));
			}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) {
				if (started.isEmpty()) return Lang.POOL_MAX_QUESTS.format(maxQuests);
				break;
			}
			
			List<Quest> available = notDoneQuests.stream().filter(quest -> quest.isLauncheable(p, acc, false)).collect(Collectors.toList());
			if (available.isEmpty()) {
				if (started.isEmpty()) return Lang.POOL_NO_AVAILABLE.toString();
				break;
			}else {
				Quest quest = datas.getTempStartQuest();
				if (quest == null || !quest.isLauncheable(p, acc, false)) {
					quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
					datas.setTempStartQuest(quest);
				}
				quest.attemptStart(p, () -> datas.setLastGive(System.currentTimeMillis()));
			}
		}
		return "started quest(s) #" + started.stream().map(x -> Integer.toString(x.getID())).collect(Collectors.joining(", "));
	}
	
	void unload() {
		if (starter != null) starter.removePool(this);
	}
	
	public void save(ConfigurationSection config) {
		config.set("hologram", hologram);
		config.set("maxQuests", maxQuests);
		if (questsPerLaunch != 1) config.set("questsPerLaunch", questsPerLaunch);
		config.set("redoAllowed", redoAllowed);
		config.set("timeDiff", timeDiff);
		config.set("npcID", npcID);
		config.set("avoidDuplicates", avoidDuplicates);
		if (!requirements.isEmpty()) config.set("requirements", Utils.serializeList(requirements, AbstractRequirement::serialize));
	}
	
	public static QuestPool deserialize(int id, ConfigurationSection config) {
		List<AbstractRequirement> requirements = QuestObject.deserializeList(config.getMapList("requirements"), AbstractRequirement::deserialize);
		return new QuestPool(id, config.getInt("npcID"), config.getString("hologram"), config.getInt("maxQuests"), config.getInt("questsPerLaunch", 1), config.getBoolean("redoAllowed"), config.getLong("timeDiff"), config.getBoolean("avoidDuplicates", true), requirements);
	}
	
}
