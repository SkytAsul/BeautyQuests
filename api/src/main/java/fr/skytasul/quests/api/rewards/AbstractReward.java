package fr.skytasul.quests.api.rewards;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.serializable.SerializableObject;

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
	 * Called when the reward should be given to the player
	 * @param p Player to give the reward
	 * @return title of all the subsequent reward (for instance : "4 gold")
	 */
	public abstract @Nullable List<@NotNull String> give(Player p) throws InterruptingBranchException;

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
