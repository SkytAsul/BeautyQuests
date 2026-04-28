package fr.skytasul.quests.api.tracking;

import fr.skytasul.quests.api.stages.types.Locatable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import java.util.List;

// TODO make this abstract so it can be either hologram or particles
public interface BqTrackingHologram {

	@NotNull
	TrackingData getData();

	void setPlayerVisibility(@NotNull Player player, boolean visible);

	record TrackingData(
			@NotNull Locatable.Located tracked,
			@NotNull Duration positionRefreshInterval, // TODO don't refresh if no viewer
			@NotNull List<Object> lines,
			boolean requiresVisibilityControl,
			boolean defaultVisibility) {
	}

}
