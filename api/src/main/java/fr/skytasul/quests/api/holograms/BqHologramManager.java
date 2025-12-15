package fr.skytasul.quests.api.holograms;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface BqHologramManager {

	boolean supportPerPlayerVisibility();

	boolean supportItems();

	@NotNull
	BqHologram createHologram(@NotNull Location lc, boolean defaultVisible);

}
