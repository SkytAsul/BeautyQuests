package fr.skytasul.quests.structure.pools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerPoolDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.players.PlayerPoolDatasImplementation;
import fr.skytasul.quests.utils.QuestUtils;

public class QuestPoolImplementation implements Comparable<QuestPoolImplementation>, QuestPool {
	
	private final int id;
	
	private final int npcID;
	private final String hologram;
	private final int maxQuests;
	private final int questsPerLaunch;
	private final boolean redoAllowed;
	private final long timeDiff;
	private final boolean avoidDuplicates;
	private final RequirementList requirements;
	
	BqNpcImplementation npc;
	List<Quest> quests = new ArrayList<>();
	
	QuestPoolImplementation(int id, int npcID, String hologram, int maxQuests, int questsPerLaunch, boolean redoAllowed, long timeDiff, boolean avoidDuplicates, RequirementList requirements) {
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
			npc = BeautyQuests.getInstance().getNpcManager().getById(npcID);
			if (npc != null) {
				npc.addPool(this);
				return;
			}
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Unknown NPC " + npcID + " for quest pool #" + id);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public int getNpcId() {
		return npcID;
	}
	
	@Override
	public String getHologram() {
		return hologram;
	}
	
	@Override
	public int getMaxQuests() {
		return maxQuests;
	}
	
	@Override
	public int getQuestsPerLaunch() {
		return questsPerLaunch;
	}
	
	@Override
	public boolean isRedoAllowed() {
		return redoAllowed;
	}
	
	@Override
	public long getTimeDiff() {
		return timeDiff;
	}
	
	@Override
	public boolean doAvoidDuplicates() {
		return avoidDuplicates;
	}
	
	@Override
	public RequirementList getRequirements() {
		return requirements;
	}
	
	@Override
	public List<Quest> getQuests() {
		return quests;
	}
	
	@Override
	public void addQuest(Quest quest) {
		quests.add(quest);
	}
	
	@Override
	public void removeQuest(Quest quest) {
		quests.remove(quest);
	}
	
	@Override
	public int compareTo(QuestPoolImplementation o) {
		return Integer.compare(id, o.id);
	}
	
	@Override
	public ItemStack getItemStack(String action) {
		return ItemUtils.item(XMaterial.CHEST, Lang.poolItemName.format(id),
				Lang.poolItemNPC.format(npcID + " (" + (npc == null ? "unknown" : npc.getNpc().getName())
						+ ")"),
				Lang.poolItemMaxQuests.format(maxQuests),
				Lang.poolItemQuestsPerLaunch.format(questsPerLaunch),
				Lang.poolItemRedo.format(redoAllowed ? Lang.Enabled : Lang.Disabled),
				Lang.poolItemTime.format(Utils.millisToHumanString(timeDiff)),
				Lang.poolItemHologram.format(hologram == null ? "\n§7 > " + Lang.PoolHologramText.toString() + "\n§7 > " + Lang.defaultValue : hologram),
				Lang.poolItemAvoidDuplicates.format(avoidDuplicates ? Lang.Enabled : Lang.Disabled),
				"§7" + Lang.requirements.format(requirements.size()),
				Lang.poolItemQuestsList.format(quests.size(),
						quests.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", "))),
				"", action);
	}
	
	@Override
	public CompletableFuture<PlayerPoolDatas> resetPlayer(PlayerAccount acc) {
		return acc.removePoolDatas(this);
	}
	
	@Override
	public void resetPlayerTimer(PlayerAccount acc) {
		if (!acc.hasPoolDatas(this)) return;
		acc.getPoolDatas(this).setLastGive(0);
	}
	
	public void questCompleted(PlayerAccount acc, Quest quest) {
		if (!avoidDuplicates) return;
		PlayerPoolDatasImplementation poolDatas = (PlayerPoolDatasImplementation) acc.getPoolDatas(this);
		poolDatas.getCompletedQuests().add(quest.getId());
		poolDatas.updatedCompletedQuests();
	}
	
	@Override
	public boolean canGive(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		if (datas.getLastGive() + timeDiff > System.currentTimeMillis()) return false;
		
		for (AbstractRequirement requirement : requirements) {
			try {
				if (!requirement.test(p)) return false;
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot test requirement " + requirement.getClass().getSimpleName() + " in pool " + id + " for player " + p.getName(), ex);
				return false;
			}
		}
		
		List<Quest> notDoneQuests = avoidDuplicates ? quests.stream()
				.filter(quest -> !datas.getCompletedQuests().contains(quest.getId())).collect(Collectors.toList()) : quests;
		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!redoAllowed) return false;
			return quests.stream().anyMatch(quest -> quest.isRepeatable() && quest.canStart(p, false));
		}else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest())).count() >= maxQuests) return false;
		
		return notDoneQuests.stream().anyMatch(quest -> quest.canStart(p, false));
	}
	
	@Override
	public CompletableFuture<String> give(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerPoolDatas datas = acc.getPoolDatas(this);
		
		long time = (datas.getLastGive() + timeDiff) - System.currentTimeMillis();
		if (time > 0)
			return CompletableFuture.completedFuture(Lang.POOL_NO_TIME.format(Utils.millisToHumanString(time)));

		return CompletableFuture.supplyAsync(() -> {
			List<Quest> started = new ArrayList<>(questsPerLaunch);
			try {
				for (int i = 0; i < questsPerLaunch; i++) {
					PoolGiveResult result = giveOne(p, acc, datas, !started.isEmpty()).get();
					if (result.quest != null) {
						started.add(result.quest);
						datas.setLastGive(System.currentTimeMillis());
					} else if (!result.forceContinue) {
						if (started.isEmpty())
							return result.reason;
						else
							break;
					}
				}
			} catch (InterruptedException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Interrupted!", ex);
				Thread.currentThread().interrupt();
			} catch (ExecutionException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to give quests to player " + p.getName() + " from pool " + id, ex);
			}

			return "started quest(s) " + started.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", "));
		});
	}

	private CompletableFuture<PoolGiveResult> giveOne(Player p, PlayerAccount acc, PlayerPoolDatas datas,
			boolean hadOne) {
		if (!requirements.testPlayer(p, !hadOne))
			return CompletableFuture.completedFuture(new PoolGiveResult(""));

		List<Quest> notCompleted = avoidDuplicates ? quests.stream()
				.filter(quest -> !datas.getCompletedQuests().contains(quest.getId())).collect(Collectors.toList()) : quests;
		if (notCompleted.isEmpty()) {
			// all quests completed: we check if the player can redo some of them
			notCompleted = replenishQuests(datas);
			if (notCompleted.isEmpty())
				return CompletableFuture.completedFuture(new PoolGiveResult(Lang.POOL_ALL_COMPLETED.toString()));
		} else if (acc.getQuestsDatas().stream().filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest()))
				.count() >= maxQuests) {
			// player has too much quests in this pool to be able to start one more
			return CompletableFuture.completedFuture(new PoolGiveResult(Lang.POOL_MAX_QUESTS.format(maxQuests)));
		}

		List<Quest> notStarted = notCompleted.stream().filter(quest -> !quest.hasStarted(acc)).collect(Collectors.toList());
		if (notStarted.isEmpty()) {
			// means all quests that are not yet completed are already started.
			// we should then check if the player can redo some of the quests it has completed
			notStarted = replenishQuests(datas);
		}

		List<Quest> available = notStarted.stream().filter(quest -> quest.canStart(p, false)).collect(Collectors.toList());
		// at this point, "available" contains all quests that the player has not yet completed, that it is
		// not currently doing and that meet the requirements to launch

		if (available.isEmpty()) {
			return CompletableFuture.completedFuture(new PoolGiveResult(Lang.POOL_NO_AVAILABLE.toString()));
		} else {
			CompletableFuture<PoolGiveResult> future = new CompletableFuture<>();
			QuestUtils.runOrSync(() -> {
				Quest quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
				quest.attemptStart(p).whenComplete((result, exception) -> {
					if (exception != null) {
						future.completeExceptionally(exception);
					} else {
						future.complete(result ? new PoolGiveResult(quest) : new PoolGiveResult("").forceContinue());
					}
				});
			});
			return future;
		}
	}

	private List<Quest> replenishQuests(PlayerPoolDatas datas) {
		if (!redoAllowed) return Collections.emptyList();
		List<Quest> notDoneQuests = quests.stream()
				.filter(Quest::isRepeatable)
				.filter(quest -> !quest.hasStarted(datas.getAccount()))
				.collect(Collectors.toList());
		if (!notDoneQuests.isEmpty()) {
			datas.setCompletedQuests(quests
					.stream()
					.filter(quest -> !notDoneQuests.contains(quest))
					.map(Quest::getId)
					.collect(Collectors.toSet()));
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Replenished available quests of " + datas.getAccount().getNameAndID() + " for pool " + id);
		return notDoneQuests;
	}
	
	void unload() {
		if (npc != null) npc.removePool(this);
	}
	
	public void unloadStarter() {
		npc = null;
	}
	
	public void save(ConfigurationSection config) {
		config.set("hologram", hologram);
		config.set("maxQuests", maxQuests);
		if (questsPerLaunch != 1) config.set("questsPerLaunch", questsPerLaunch);
		config.set("redoAllowed", redoAllowed);
		config.set("timeDiff", timeDiff);
		config.set("npcID", npcID);
		config.set("avoidDuplicates", avoidDuplicates);
		if (!requirements.isEmpty())
			config.set("requirements", requirements.serialize());
	}
	
	public static QuestPoolImplementation deserialize(int id, ConfigurationSection config) {
		return new QuestPoolImplementation(id, config.getInt("npcID"), config.getString("hologram"),
				config.getInt("maxQuests"), config.getInt("questsPerLaunch", 1), config.getBoolean("redoAllowed"),
				config.getLong("timeDiff"), config.getBoolean("avoidDuplicates", true),
				RequirementList.deserialize(config.getMapList("requirements")));
	}
	
	private static class PoolGiveResult {
		private final Quest quest;
		private final String reason;
		private boolean forceContinue = false;

		public PoolGiveResult(Quest quest) {
			this.quest = quest;
			this.reason = null;
		}

		public PoolGiveResult(String reason) {
			this.quest = null;
			this.reason = reason;
		}

		public PoolGiveResult forceContinue() {
			forceContinue = true;
			return this;
		}
	}

}
