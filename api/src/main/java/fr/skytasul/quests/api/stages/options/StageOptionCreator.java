package fr.skytasul.quests.api.stages.options;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.stages.AbstractStage;

public class StageOptionCreator<T extends AbstractStage> extends SerializableCreator<StageOption<T>> {

	public StageOptionCreator(@NotNull String id, @NotNull Class<? extends StageOption<T>> clazz,
			@NotNull Supplier<@NotNull StageOption<T>> newObjectSupplier) {
		super(id, clazz, newObjectSupplier);
	}

	public static <T extends AbstractStage, O extends StageOption<T>> StageOptionCreator<T> create(@NotNull String id,
			@NotNull Class<O> optionClass, @NotNull Supplier<@NotNull O> supplier) {
		return new StageOptionCreator<>(id, optionClass, (@NotNull Supplier<@NotNull StageOption<T>>) supplier);
	}

}
