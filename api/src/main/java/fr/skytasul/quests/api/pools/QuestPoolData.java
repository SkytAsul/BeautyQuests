package fr.skytasul.quests.api.pools;

import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardList;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record QuestPoolData(
		@Nullable String name,
		@Nullable String npcId,
		@Nullable String hologram,
		int maxQuests,
		int questsPerLaunch,
		boolean redoAllowed,
		long timeDiff,
		boolean avoidDuplicates,
		boolean showAsCategory,
		@NotNull RequirementList requirements,
		@NotNull RewardList startRewards,
		@NotNull RewardList endRewards) {

	public void save(ConfigurationSection config) {
		config.set("name", name);
		config.set("hologram", hologram);
		config.set("maxQuests", maxQuests);
		config.set("questsPerLaunch", questsPerLaunch);
		config.set("redoAllowed", redoAllowed);
		config.set("timeDiff", timeDiff);
		config.set("npcID", npcId);
		config.set("avoidDuplicates", avoidDuplicates);
		config.set("showAsCategory", showAsCategory);
		if (!requirements.isEmpty())
			config.set("requirements", requirements.serialize());
		if (!startRewards.isEmpty())
			config.set("startRewards", startRewards.serialize());
		if (!endRewards.isEmpty())
			config.set("endRewards", endRewards.serialize());
	}

	public static QuestPoolData deserialize(ConfigurationSection config) {
		return new QuestPoolData(
				config.getString("name"),
				config.getString("npcID"),
				config.getString("hologram"),
				config.getInt("maxQuests"),
				config.getInt("questsPerLaunch", 1),
				config.getBoolean("redoAllowed"),
				config.getLong("timeDiff"),
				config.getBoolean("avoidDuplicates", true),
				config.getBoolean("showAsCategory", false), // false for migration from 2.0
				RequirementList.deserialize(config.getMapList("requirements")),
				RewardList.deserialize(config.getMapList("startRewards")),
				RewardList.deserialize(config.getMapList("endRewards"))
		);
	}

}
