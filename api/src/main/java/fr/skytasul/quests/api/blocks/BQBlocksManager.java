package fr.skytasul.quests.api.blocks;

import java.util.Collection;
import java.util.Spliterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.stages.types.Locatable;

public interface BQBlocksManager {

	public static final String HEADER_SEPARATOR = ":";
	public static final String CUSTOM_NAME_FOOTER = "|customname:";

	public @NotNull BQBlock deserialize(@NotNull String string) throws IllegalArgumentException;

	public void registerBlockType(@NotNull String header, @NotNull BQBlockType type);

	public @Nullable String getHeader(@NotNull BQBlockType type);

	public @NotNull Spliterator<Locatable.@NotNull Located> getNearbyBlocks(
			@NotNull Locatable.MultipleLocatable.NearbyFetcher fetcher,
			@NotNull Collection<fr.skytasul.quests.api.blocks.BQBlock> types);

	public @NotNull BQBlock createSimple(@NotNull XMaterial material, @Nullable String customName);

}
