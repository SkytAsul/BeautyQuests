package fr.skytasul.quests.api.rewards;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Unmodifiable
public class RewardList extends AbstractList<AbstractReward> {

	private final @NotNull List<@NotNull AbstractReward> rewards;
	private final boolean hasAsync;

	private final List<fr.skytasul.quests.api.questers.Quester> asyncQuesters;

	public RewardList() {
		this.rewards = List.of();
		this.hasAsync = false;
		this.asyncQuesters = null;
	}

	public RewardList(@NotNull Collection<@NotNull AbstractReward> rewards) {
		this.rewards = List.of(rewards.toArray(AbstractReward[]::new));
		this.hasAsync = rewards.stream().anyMatch(AbstractReward::isAsync);
		this.asyncQuesters = new ArrayList<>();
	}

	public @NotNull CompletableFuture<RewardsGiveResult> giveRewards(@NotNull Quester quester) {
		var future = new CompletableFuture<RewardsGiveResult>();

		Runnable task = () -> {
			try {
				var context = new RewardGiveContext(quester);
				for (var reward : rewards) {
					try {
						reward.give(context);
					} catch (Exception ex) {
						QuestsPlugin.getPlugin().getLoggerExpanded()
								.severe("An exception occurred while giving a reward to {}", ex, quester.getDetailedName());
					}
				}
				future.complete(new RewardsGiveResult(context.isBranchInterrupted(), context.getEarnings()));
			} catch (Throwable ex) {
				future.completeExceptionally(ex);
			}
			if (asyncQuesters != null) // TODO check if it's called before executing following actions of the future
				asyncQuesters.remove(quester);
		};

		if (hasAsync) {
			asyncQuesters.add(quester);
			Bukkit.getScheduler().runTaskAsynchronously(QuestsPlugin.getPlugin(), task);
		} else {
			task.run();
		}

		return future;
	}

	public void giveSubrewards(@NotNull Player player, RewardGiveContext context) {
		if (!context.getQuester().getOnlinePlayers().contains(player))
			throw new IllegalArgumentException("Player is not apart of the context's quester");

		Quester playerQuester = PlayersManager.getPlayerAccount(player);
		try {
			this.giveRewards(playerQuester).get().mergeInContext(context);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException ex) {
			DefaultErrors.sendGeneric(playerQuester, "giving rewards");
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to give rewards to {}", ex,
					playerQuester.getDetailedName());
		}
	}

	public boolean isInAsyncReward(@NotNull Quester quester) {
		return asyncQuesters != null && asyncQuesters.contains(quester);
	}

	public void attachQuest(@NotNull Quest quest) {
		rewards.forEach(reward -> reward.attach(quest));
	}

	public void detachQuest() {
		rewards.forEach(reward -> reward.detach());
	}

	public boolean hasAsync() {
		return hasAsync;
	}

	@Override
	public AbstractReward get(int index) {
		return rewards.get(index);
	}

	@Override
	public int size() {
		return rewards.size();
	}

	public String getSizeString() {
		return getSizeString(rewards.size());
	}

	public @NotNull List<Map<String, Object>> serialize() {
		return SerializableObject.serializeList(rewards);
	}

	public static RewardList deserialize(@NotNull List<Map<?, ?>> mapList) {
		return new RewardList(SerializableObject.deserializeList(mapList, AbstractReward::deserialize));
	}

	public static String getSizeString(int size) {
		return Lang.rewards.quickFormat("amount", size);
	}

	public record RewardsGiveResult(boolean branchInterruption, Map<Player, List<String>> earnings) {
		public @NotNull List<String> getPlayerEarnings(@NotNull Player player) {
			return earnings.getOrDefault(player, List.of());
		}

		protected void mergeInContext(@NotNull RewardGiveContext context) {
			earnings.forEach(
					(player, playerEarnings) -> playerEarnings.forEach(earning -> context.addEarning(player, earning)));
			if (branchInterruption)
				context.interruptBranch();
		}
	}

}
