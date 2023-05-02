package fr.skytasul.quests.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;

public enum PlayerListCategory {

	FINISHED(
			1,
			XMaterial.WRITTEN_BOOK,
			Lang.finisheds.toString()),
	IN_PROGRESS(
			2,
			XMaterial.BOOK,
			Lang.inProgress.toString()),
	NOT_STARTED(
			3,
			XMaterial.WRITABLE_BOOK,
			Lang.notStarteds.toString());

	private final int slot;
	private final @NotNull XMaterial material;
	private final @NotNull String name;
	
	private PlayerListCategory(int slot, @NotNull XMaterial material, @NotNull String name) {
		this.slot = slot;
		this.material = material;
		this.name = name;
	}
	
	public int getSlot() {
		return slot;
	}

	public @NotNull XMaterial getMaterial() {
		return material;
	}

	public @NotNull String getName() {
		return name;
	}
	
	public static @Nullable PlayerListCategory fromString(@NotNull String name) {
		try {
			return PlayerListCategory.valueOf(name.toUpperCase());
		}catch (IllegalArgumentException ex) {}
		return null;
	}
	
}