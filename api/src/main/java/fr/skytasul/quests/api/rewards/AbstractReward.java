package fr.skytasul.quests.api.rewards;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.serializable.SerializableObject;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public abstract class AbstractReward extends QuestObject {

	protected AbstractReward() {
		this(null);
	}

	protected AbstractReward(@Nullable String customDescription) {
		super(QuestsAPI.getAPI().getRewards(), customDescription);
	}

	@Override
	public @NotNull RewardCreator getCreator() {
		return (RewardCreator) super.getCreator();
	}

	/**
	 * Give the reward to the quester in the context
	 *
	 * @param context context of the reward giving
	 */
	public abstract void give(@NotNull RewardGiveContext context);

	@Override
	protected void sendCustomDescriptionHelpMessage(@NotNull Player p) {
		Lang.CHOOSE_REWARD_CUSTOM_DESCRIPTION.send(p);
	}

	@Override
	protected final void clickInternal(@NotNull QuestObjectClickEvent event) {
		itemClick(event);
	}

	protected abstract void itemClick(@NotNull QuestObjectClickEvent event);

	@Override
	public abstract @NotNull AbstractReward clone();

	public static @NotNull AbstractReward deserialize(Map<String, Object> map) {
		return SerializableObject.deserialize(map, QuestsAPI.getAPI().getRewards());
	}

	public boolean isAsync() {
		return false;
	}

}
