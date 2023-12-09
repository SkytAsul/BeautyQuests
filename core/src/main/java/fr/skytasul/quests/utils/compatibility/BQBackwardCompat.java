package fr.skytasul.quests.utils.compatibility;

import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.stages.StageType;

public class BQBackwardCompat {

	public static @NotNull Optional<StageType<?>> loadStageFromConfig(@Nullable String stageType,
			@NotNull ConfigurationSection config) {

		if ("INTERACT".equals(stageType)) {
			stageType = config.contains("location") ? "INTERACT_LOCATION" : "INTERACT_BLOCK";
			return QuestsAPI.getAPI().getStages().getType(stageType);
		}

		return Optional.empty();
	}

}
