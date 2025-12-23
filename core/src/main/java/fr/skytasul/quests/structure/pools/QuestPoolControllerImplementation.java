package fr.skytasul.quests.structure.pools;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.pools.QuestPoolController;
import fr.skytasul.quests.api.pools.QuestPoolData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.data.QuesterPoolData;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.npcs.BqNpcImplementation;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class QuestPoolControllerImplementation implements QuestPoolController, Comparable<QuestPoolController> {

	private final int id;
	private QuestPoolData data;

	BqNpcImplementation npc;
	List<Quest> quests = new ArrayList<>();

	private @Nullable PlaceholderRegistry placeholders;

	protected QuestPoolControllerImplementation(int id, QuestPoolData data) {
		this.id = id;
		this.data = data;

		if (data.npcId() != null) {
			npc = BeautyQuests.getInstance().getNpcManager().getById(data.npcId());
			if (npc != null)
				npc.addPool(this);
			else
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Unknown NPC {} for quest pool #{}", data.npcId(), id);
		}
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public QuestPoolData getPoolData() {
		return data;
	}

	@Override
	public List<Quest> getQuests() {
		return quests;
	}

	public void addQuest(Quest quest) {
		quests.add(quest);
	}

	public void removeQuest(Quest quest) {
		quests.remove(quest);
	}

	@Override
	public int compareTo(QuestPoolController o) {
		return Integer.compare(id, o.getId());
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry()
					.registerIndexed("pool", "%s #%d".formatted(data.name() == null ? "no name" : data.name(), id))
					.register("pool_id", id)
					.register("pool_name", data.name() == null ? Lang.NotSet : data.name())
					.register("pool_npc_id", data.npcId() == null ? Lang.NotSet : data.npcId())
					.register("pool_npc_name",
							() -> data.npcId() == null ? Lang.NotSet.toString()
									: (npc == null ? "unknown" : npc.getNpc().getName()))
					.register("pool_max_quests", data.maxQuests())
					.register("pool_quests_per_launch", data.questsPerLaunch())
					.register("pool_redo", MessageUtils.getYesNo(data.redoAllowed()))
					.register("pool_duplicates", MessageUtils.getYesNo(data.avoidDuplicates()))
					.register("pool_show_as_category", MessageUtils.getYesNo(data.showAsCategory()))
					.register("pool_time", Utils.millisToHumanString(data.timeDiff()))
					.register("pool_hologram", QuestOption.formatNullableValue(data.hologram(), Lang.PoolHologramText))
					.register("pool_quests",
							() -> quests.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", ")))
					.register("pool_quests_amount", () -> Integer.toString(quests.size()));
		}
		return placeholders;
	}

	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.CHEST, Lang.poolItemName.format(this),
				Lang.poolItemShowAsCategory.format(this),
				Lang.poolItemNPC.format(this),
				Lang.poolItemMaxQuests.format(this),
				Lang.poolItemQuestsPerLaunch.format(this),
				Lang.poolItemRedo.format(this),
				Lang.poolItemTime.format(this),
				Lang.poolItemHologram.format(this),
				Lang.poolItemAvoidDuplicates.format(this),
				"§7" + data.requirements().getSizeString(),
				Lang.poolItemQuestsList.format(this));
	}

	@Override
	public CompletableFuture<Boolean> resetPlayer(Quester acc) {
		return acc.getDataHolder().removePoolData(this).thenApply(__ -> true);
	}

	@Override
	public void resetPlayerTimer(Quester acc) {
		acc.getDataHolder().getPoolDataIfPresent(this).ifPresent(data -> data.setLastGive(0));
	}

	public void questCompleted(Quester acc, Quest quest) {
		if (!data.avoidDuplicates())
			return;
		var poolDatas = acc.getDataHolder().getPoolData(this);

		var completedQuests = poolDatas.getCompletedQuests();
		completedQuests.add(quest.getId());
		poolDatas.setCompletedQuests(completedQuests);
	}

	@Override
	public @NotNull CanGiveResult canGive(Player p) {
		Quester quester = PlayerManager.getPlayerAccount(p);

		Optional<QuesterPoolData> questerData = quester.getDataHolder().getPoolDataIfPresent(this);

		if (questerData.isPresent()) {
			long time = (questerData.get().getLastGive() + data.timeDiff()) - System.currentTimeMillis();
			if (time > 0)
				return new CanGiveResult(false, Lang.POOL_NO_TIME.quickFormat("time_left", Utils.millisToHumanString(time)));
		}

		var requirementsMatch = data.requirements().allMatch(p);
		if (!requirementsMatch.result())
			return new CanGiveResult(false, requirementsMatch.reason());

		List<Quest> notDoneQuests = data.avoidDuplicates() && questerData.isPresent() ? quests.stream()
				.filter(quest -> !questerData.get().getCompletedQuests().contains(quest.getId()))
				.collect(Collectors.toList())
				: quests;

		if (notDoneQuests.isEmpty()) { // all quests completed
			if (!data.redoAllowed() || quests.stream().noneMatch(quest -> quest.isRepeatable() && quest.canStart(p, false)))
				return new CanGiveResult(false, Lang.POOL_ALL_COMPLETED.toString());
		} else if (quester.getDataHolder().getAllQuestsData().stream()
				.filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest()))
				.count() >= data.maxQuests())
			return new CanGiveResult(false, Lang.POOL_MAX_QUESTS.format(this));

		if (notDoneQuests.stream().noneMatch(quest -> quest.canStart(p, false)))
			return new CanGiveResult(false, Lang.POOL_NO_AVAILABLE.toString());

		return new CanGiveResult(true, null);
	}

	@Override
	public CompletableFuture<String> give(Player p) {
		Quester quester = PlayerManager.getPlayerAccount(p);
		QuesterPoolData questerData = quester.getDataHolder().getPoolData(this);

		long time = (questerData.getLastGive() + data.timeDiff()) - System.currentTimeMillis();
		if (time > 0)
			return CompletableFuture
					.completedFuture(Lang.POOL_NO_TIME.quickFormat("time_left", Utils.millisToHumanString(time)));

		return CompletableFuture.supplyAsync(() -> {
			List<Quest> started = new ArrayList<>(data.questsPerLaunch());
			try {
				for (int i = 0; i < data.questsPerLaunch(); i++) {
					PoolGiveResult result = giveOne(p, quester, questerData, !started.isEmpty()).get();
					if (result.quest != null) {
						started.add(result.quest);
						questerData.setLastGive(System.currentTimeMillis());
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
				throw new CompletionException(ex);
			}

			return "started quest(s) " + started.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", "));
		});
	}

	private CompletableFuture<PoolGiveResult> giveOne(Player p, Quester acc, QuesterPoolData datas,
			boolean hadOne) {
		if (!data.requirements().allMatch(p, !hadOne))
			return CompletableFuture.completedFuture(new PoolGiveResult(""));

		List<Quest> notCompleted = data.avoidDuplicates() ? quests.stream()
				.filter(quest -> !datas.getCompletedQuests().contains(quest.getId())).collect(Collectors.toList()) : quests;
		if (notCompleted.isEmpty()) {
			// all quests completed: we check if the player can redo some of them
			notCompleted = replenishQuests(acc, datas);
			if (notCompleted.isEmpty())
				return CompletableFuture.completedFuture(new PoolGiveResult(Lang.POOL_ALL_COMPLETED.toString()));
		} else if (acc.getDataHolder().getAllQuestsData().stream()
				.filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest()))
				.count() >= data.maxQuests()) {
			// player has too much quests in this pool to be able to start one more
			return CompletableFuture.completedFuture(new PoolGiveResult(Lang.POOL_MAX_QUESTS.format(this)));
		}

		List<Quest> notStarted = notCompleted.stream().filter(quest -> !quest.hasStarted(acc)).collect(Collectors.toList());
		if (notStarted.isEmpty()) {
			// means all quests that are not yet completed are already started.
			// we should then check if the player can redo some of the quests it has completed
			notStarted = replenishQuests(acc, datas);
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

	private List<Quest> replenishQuests(@NotNull Quester quester, @NotNull QuesterPoolData datas) {
		if (!data.redoAllowed())
			return Collections.emptyList();
		List<Quest> notDoneQuests = quests.stream()
				.filter(Quest::isRepeatable)
				.filter(quest -> !quest.hasStarted(quester))
				.collect(Collectors.toList());
		if (!notDoneQuests.isEmpty()) {
			datas.setCompletedQuests(quests
					.stream()
					.filter(quest -> !notDoneQuests.contains(quest))
					.map(Quest::getId)
					.collect(Collectors.toSet()));
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Replenished available quests of {} for pool {}",
				quester.getDetailedName(), id);
		return notDoneQuests;
	}

	@Override
	public @NotNull OptionalLong getRemainingCooldown(@NotNull Quester quester) {
		Optional<QuesterPoolData> questerData = quester.getDataHolder().getPoolDataIfPresent(this);

		if (questerData.isEmpty())
			return OptionalLong.empty();

		long time = (questerData.get().getLastGive() + data.timeDiff()) - System.currentTimeMillis();
		return time > 0 ? OptionalLong.of(time) : OptionalLong.empty();
	}

	@Override
	public Collection<Quest> getQuestsRemaining(@NotNull Quester quester) {
		if (!quester.getDataHolder().hasPoolData(this))
			return quests;

		var questerData = quester.getDataHolder().getPoolData(this);

		return quests.stream()
				.filter(quest -> !quest.hasStarted(quester) && !questerData.getCompletedQuests().contains(quest.getId()))
				.toList();
	}

	@Override
	public Collection<Quest> getQuestsInProgress(@NotNull Quester quester) {
		return quests.stream().filter(quest -> quest.hasStarted(quester)).toList();
	}

	@Override
	public Collection<Quest> getQuestsCompleted(@NotNull Quester quester) {
		if (!quester.getDataHolder().hasPoolData(this))
			return Collections.emptyList();

		var questerData = quester.getDataHolder().getPoolData(this);

		return questerData.getCompletedQuests().stream().map(QuestsAPI.getAPI().getQuestsManager()::getQuest).toList();
	}

	void unload() {
		if (npc != null) npc.removePool(this);
	}

	public void unloadStarter() {
		npc = null;
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
