package fr.skytasul.quests.api.holograms;

import fr.skytasul.quests.api.tracking.BqTrackingHologram;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BqHologramManager {

	@Nullable
	BqInternalHologramFactory getInternalFactory();

	void setInternalFactory(@NotNull Key name, @NotNull BqInternalHologramFactory factory);

	BqTrackingHologram createTrackingHologram(BqTrackingHologram.TrackingData data);

}
