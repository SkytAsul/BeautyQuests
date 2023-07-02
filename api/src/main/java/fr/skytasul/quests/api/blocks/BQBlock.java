package fr.skytasul.quests.api.blocks;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.utils.MinecraftNames;

public abstract class BQBlock {

	private final @NotNull BQBlockType type;
	private final @Nullable String customName;

	private @Nullable XMaterial cachedMaterial;
	private @Nullable String cachedName;
	
	protected BQBlock(@NotNull BQBlockOptions options) {
		this.type = options.getType();
		this.customName = options.getCustomName();
	}

	protected abstract @NotNull String getDataString();
	
	public abstract boolean applies(@NotNull Block block);
	
	public abstract @NotNull XMaterial retrieveMaterial();
	
	public final @NotNull XMaterial getMaterial() {
		if (cachedMaterial == null) cachedMaterial = retrieveMaterial();
		return cachedMaterial;
	}
	
	public @NotNull String getDefaultName() {
		return MinecraftNames.getMaterialName(getMaterial());
	}

	public final @NotNull String getName() {
		if (cachedName == null)
			cachedName = customName == null ? getDefaultName() : customName;

		return cachedName;
	}

	public final @NotNull String getAsString() {
		return getHeader() + getDataString() + getFooter();
	}
	
	private @NotNull String getHeader() {
		String header = QuestsAPI.getAPI().getBlocksManager().getHeader(type);
		return header == null ? "" : (header + BQBlocksManager.HEADER_SEPARATOR);
	}

	private @NotNull String getFooter() {
		return customName == null ? "" : BQBlocksManager.CUSTOM_NAME_FOOTER + customName;
	}

	@Override
	public String toString() {
		return "BQBlock{" + getAsString() + "}";
	}
	
}
