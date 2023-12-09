package fr.skytasul.quests.api.rewards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;

public class RewardList extends ArrayList<@NotNull AbstractReward> {

	private static final long serialVersionUID = 1238520030299575535L;

	public RewardList() {}

	public RewardList(@NotNull Collection<@NotNull AbstractReward> rewards) {
		super(rewards);
	}

	public List<String> giveRewards(@NotNull Player p) throws InterruptingBranchException {
		InterruptingBranchException interrupting = null;

		List<String> msg = new ArrayList<>();
		for (AbstractReward rew : this) {
			try {
				List<String> messages = rew.give(p);
				if (messages != null)
					msg.addAll(messages);
			} catch (InterruptingBranchException ex) {
				if (interrupting != null) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Interrupting the same branch via rewards twice!");
				} else {
					interrupting = ex;
				}
			} catch (Throwable e) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when giving reward " + rew.getName() + " to " + p.getName(), e);
			}
		}

		if (interrupting != null)
			throw interrupting;
		return msg;
	}

	public void attachQuest(@NotNull Quest quest) {
		forEach(reward -> reward.attach(quest));
	}

	public void detachQuest() {
		forEach(reward -> reward.detach());
	}

	public boolean hasAsync() {
		return stream().anyMatch(AbstractReward::isAsync);
	}

	public String getSizeString() {
		return getSizeString(size());
	}

	public @NotNull List<Map<String, Object>> serialize() {
		return SerializableObject.serializeList(this);
	}

	public static RewardList deserialize(@NotNull List<Map<?, ?>> mapList) {
		return new RewardList(SerializableObject.deserializeList(mapList, AbstractReward::deserialize));
	}

	public static String getSizeString(int size) {
		return Lang.rewards.quickFormat("amount", size);
	}

}
