package fr.skytasul.quests.api.holograms;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface BqInternalHologramFactory {

	@NotNull
	BqInternalHologram createHologram(@NotNull Location lc, boolean defaultVisible);

	public interface BqInternalHologramFactoryWithVisibility extends BqInternalHologramFactory {

		@Override
		@NotNull
		BqInternalHologram.BqInternalHologramWithVisibility createHologram(@NotNull Location lc, boolean defaultVisible);

	}

	public interface BqInternalHologramFactoryWithItems extends BqInternalHologramFactory {

		@Override
		@NotNull
		BqInternalHologram.BqInternalHologramWithItems createHologram(@NotNull Location lc, boolean defaultVisible);

	}

}
