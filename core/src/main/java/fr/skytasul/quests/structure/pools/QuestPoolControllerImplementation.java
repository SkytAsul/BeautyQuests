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
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
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

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.QuestPoolController");

	private final int id;
	private final QuestPoolData data;

	BqNpcImplementation npc;
	Set<Quest> quests = new HashSet<>();

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
	public Set<Quest> getQuests() {
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
					.register("pool_requirements_amount", data.requirements().getSizeString())
					.register("pool_start_rewards_amount", data.startRewards().getSizeString())
					.register("pool_end_rewards_amount", data.endRewards().getSizeString())
					.register("pool_quests",
							() -> quests.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", ")))
					.register("pool_quests_amount", () -> Integer.toString(quests.size()))
			;
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
				Lang.poolItemQuestsList.format(this),
				Lang.poolItemRequirements.format(this),
				Lang.poolItemStartRewards.format(this),
				Lang.poolItemEndRewards.format(this));
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
		var poolDatas = acc.getDataHolder().getPoolData(this);

		var completedQuests = poolDatas.getCompletedQuests();
		completedQuests.add(quest.getId());
		poolDatas.setCompletedQuests(completedQuests);

		if (!data.redoAllowed() && completedQuests.size() == quests.size()) {
			LOGGER.debug("{0} completed all quests in pool {1}", acc.getDetailedName(), id);
			data.endRewards().giveRewards(acc)
					.whenComplete(LOGGER.logError("Failed to give rewards to {0}", acc.getDetailedName()));
		}
	}

	@Override
	public @NotNull CanGiveResult canGive(Player p) {
		Quester quester = PlayerManager.getPlayerAccount(p);

		Optional<QuesterPoolData> questerData = quester.getDataHolder().getPoolDataIfPresent(this);

		if (data.timeDiff() > 0 && questerData.isPresent()) {
			long time = (questerData.get().getLastGive() + data.timeDiff()) - System.currentTimeMillis();
			if (time > 0)
				return new CanGiveResult(CanGiveResultType.ON_COOLDOWN,
						Lang.POOL_NO_TIME.quickFormat("time_left", Utils.millisToHumanString(time)));
		}

		var requirementsMatch = data.requirements().allMatch(p);
		if (!requirementsMatch.result())
			return new CanGiveResult(CanGiveResultType.REQUIREMENTS_UNMET, requirementsMatch.reason());

		if (quests.isEmpty()) {
			LOGGER.warning("Quest pool {0} has got no quest in it.", id);
			return new CanGiveResult(CanGiveResultType.NONE_AVAILABLE, "There are no quests in the pool.");
		}

		Collection<Quest> notCompleted = getNotCompletedQuests(questerData);

		if (notCompleted.isEmpty()) {
			// all quests completed: we check if the player can redo some of them
			notCompleted = replenishQuests(quester, questerData.orElseThrow());
			if (notCompleted.isEmpty())
				return new CanGiveResult(CanGiveResultType.ALL_COMPLETED, Lang.POOL_ALL_COMPLETED.toString());
		} else if (quester.getDataHolder().getAllQuestsData().stream()
				.filter(quest -> quest.hasStarted() && quests.contains(quest.getQuest()))
				.count() >= data.maxQuests()) {
			// player has too much quests in this pool to be able to start one more
			return new CanGiveResult(CanGiveResultType.MAX_QUESTS, Lang.POOL_MAX_QUESTS.format(this));
		}

		List<Quest> notStarted =
				notCompleted.stream().filter(quest -> !quest.hasStarted(quester)).collect(Collectors.toList());
		if (notStarted.isEmpty()) {
			// means all quests that are not yet completed are already started.
			// we should then check if the player can redo some of the quests it has completed
			if (questerData.isPresent())
				notStarted = replenishQuests(quester, questerData.get());
		}

		List<Quest> available = notStarted.stream().filter(quest -> quest.canStart(p, false)).collect(Collectors.toList());
		// at this point, "available" contains all quests that the player has not yet completed, that it is
		// not currently doing and that meet the requirements to launch

		if (available.isEmpty())
			return new CanGiveResult(CanGiveResultType.NONE_AVAILABLE, Lang.POOL_NO_AVAILABLE.toString());

		return new CanGiveResult(CanGiveResultType.OK, null);
	}

	private Collection<Quest> getNotCompletedQuests(Optional<QuesterPoolData> questerData) {
		return questerData.isEmpty() || (!data.avoidDuplicates() && data.redoAllowed())
				? quests
				: quests.stream()
						.filter(quest -> !questerData.get().getCompletedQuests().contains(quest.getId()))
						.toList();
	}

	@Override
	public CompletableFuture<String> give(Player p) {
		Quester quester = PlayerManager.getPlayerAccount(p);

		boolean hadPoolData = quester.getDataHolder().hasPoolData(this);
		QuesterPoolData questerData = quester.getDataHolder().getPoolData(this);

		return CompletableFuture.supplyAsync(() -> {
			boolean shouldGiveStartRewards = !hadPoolData;
			List<Quest> started = new ArrayList<>(data.questsPerLaunch());
			try {
				for (int i = 0; i < data.questsPerLaunch(); i++) {
					var canGiveResults = canGive(p);
					if (canGiveResults.isSuccess()) {
						if (shouldGiveStartRewards) {
							data.startRewards().giveRewards(quester).get();
							shouldGiveStartRewards = false;
						}
						giveOne(p, quester, questerData).get().ifPresent(started::add);
						// giveOne can return an empty optional if the player refused
						// a quest. We should still continue the loop.
					} else {
						if (started.isEmpty())
							return canGiveResults.reason();
						else
							break;
					}
				}
			} catch (InterruptedException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("Interrupted!", ex);
				Thread.currentThread().interrupt();
			} catch (ExecutionException ex) {
				throw new CompletionException(ex);
			} finally {
				if (!started.isEmpty())
					questerData.setLastGive(System.currentTimeMillis());
			}

			return "started quest(s) " + started.stream().map(x -> "#" + x.getId()).collect(Collectors.joining(", "));
		});
	}

	private CompletableFuture<Optional<Quest>> giveOne(Player p, Quester quester, QuesterPoolData datas) {
		Collection<Quest> notCompleted = getNotCompletedQuests(Optional.of(datas));

		List<Quest> available = notCompleted.stream().filter(quest -> quest.canStart(p, false)).toList();
		// at this point, "available" contains all quests that the player has not yet completed, that it is
		// not currently doing and that meet the requirements to launch

		if (available.isEmpty()) {
			throw new IllegalStateException("There is no quest available");
		}

		var future = new CompletableFuture<Optional<Quest>>();
		QuestUtils.runOrSync(() -> {
			Quest quest = available.get(ThreadLocalRandom.current().nextInt(available.size()));
			quest.attemptStart(p).whenComplete((result, exception) -> {
				if (exception != null) {
					future.completeExceptionally(exception);
				} else {
					future.complete(result ? Optional.of(quest) : Optional.empty());
				}
			});
		});
		return future;
	}

	private List<Quest> replenishQuests(@NotNull Quester quester, @NotNull QuesterPoolData datas) {
		if (!data.redoAllowed())
			return Collections.emptyList();
		List<Quest> notDoneQuests = quests.stream()
				.filter(Quest::isRepeatable)
				.filter(quest -> !quest.hasStarted(quester))
				.collect(Collectors.toList());
		if (!notDoneQuests.isEmpty() && quester.isActive()) {
			datas.setCompletedQuests(quests
					.stream()
					.filter(quest -> !notDoneQuests.contains(quest))
					.map(Quest::getId)
					.collect(Collectors.toSet()));
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Replenished available quests of {} for pool {}",
					quester.getDetailedName(), id);
		}
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

	public void unload() {
		if (npc != null) npc.removePool(this);
	}

	public void unloadStarter() {
		npc = null;
	}

}
