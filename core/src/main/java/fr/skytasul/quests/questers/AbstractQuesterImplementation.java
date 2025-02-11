package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterData;
import fr.skytasul.quests.api.questers.QuesterProvider;
import fr.skytasul.quests.api.utils.DataSavingException;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractQuesterImplementation implements Quester {

	private final @NotNull QuesterProvider provider;
	private final @NotNull QuesterData dataHolder;

	private @Nullable PlaceholderRegistry placeholders;
	private @Nullable Pointers audiencePointers;

	protected AbstractQuesterImplementation(@NotNull QuesterProvider provider, @NotNull QuesterData dataHolder) {
		this.provider = provider;
		this.dataHolder = dataHolder;
	}

	@Override
	public @NotNull QuesterProvider getProvider() {
		return provider;
	}

	@Override
	public @NotNull QuesterData getDataHolder() {
		return dataHolder;
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry();
			createdPlaceholdersRegistry(placeholders);
		}
		return placeholders;
	}

	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		placeholders
				.registerIndexed("quester_name", this::getFriendlyName)
				.register("quester_identifier", this::getIdentifier)
				.register("quester_detailed_name", this::getDetailedName);

		// TODO eventually remove: for backward compatibility (2.0)
		placeholders
				.register("player_name", this::getFriendlyName)
				.register("player", this::getDetailedName);
	}

	protected void createdPointers(@NotNull Pointers.Builder builder) {
		builder
				.withDynamic(Identity.NAME, this::getDetailedName)
				.withDynamic(Identity.DISPLAY_NAME, () -> Component.text(getFriendlyName()))
				.build();
	}

	@Override
	public @NotNull Pointers pointers() {
		if (audiencePointers == null) {
			var builder = Pointers.builder();
			createdPointers(builder);
			this.audiencePointers = builder.build();
		}
		return audiencePointers;
	}

	public void save() throws DataSavingException {
		dataHolder.save();
	}

	public void unload() {
		dataHolder.unload();
	}

}
