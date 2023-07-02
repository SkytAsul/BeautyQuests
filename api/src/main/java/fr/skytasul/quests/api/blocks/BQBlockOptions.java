package fr.skytasul.quests.api.blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BQBlockOptions {

	private final @NotNull BQBlockType type;
	private final @Nullable String customName;

	public BQBlockOptions(BQBlockType type, String customName) {
		this.type = type;
		this.customName = customName;
	}

	public @NotNull BQBlockType getType() {
		return type;
	}

	public @Nullable String getCustomName() {
		return customName;
	}

}
