package fr.skytasul.quests.api.rewards;

import fr.skytasul.quests.api.players.Quester;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardGiveContext {

	private final @NotNull Quester quester;

	private boolean interrupBranch;
	private Map<Player, List<String>> earnings = new HashMap<>();

	public RewardGiveContext(@NotNull Quester quester) {
		this.quester = quester;
	}

	public @NotNull Quester getQuester() {
		return quester;
	}

	public boolean isBranchInterrupted() {
		return interrupBranch;
	}

	public void interruptBranch() {
		this.interrupBranch = true;
	}

	public @NotNull Map<Player, @NotNull List<String>> getEarnings() {
		return earnings;
	}

	public void addEarning(@NotNull Player player, @NotNull AbstractReward reward) {
		earnings.computeIfAbsent(player, __ -> new ArrayList<>()).add(reward.getDescription(player));
	}

	public void addEarning(@NotNull Player player, @NotNull String reward) {
		earnings.computeIfAbsent(player, __ -> new ArrayList<>()).add(reward);
	}

	public void addEarning(@NotNull AbstractReward reward) {
		for (var player : quester.getOnlinePlayers())
			addEarning(player, reward);
	}

	public void addEarning(@NotNull String reward) {
		for (var player : quester.getOnlinePlayers())
			addEarning(player, reward);
	}

}
