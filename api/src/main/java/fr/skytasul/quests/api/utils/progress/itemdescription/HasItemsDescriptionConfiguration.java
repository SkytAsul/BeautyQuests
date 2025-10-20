package fr.skytasul.quests.api.utils.progress.itemdescription;

import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.progress.HasProgress;

public interface HasItemsDescriptionConfiguration {

	@NotNull
	default ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
		return QuestsConfiguration.getConfig().getStageDescriptionConfig();
	}

	public interface HasSingleObject extends HasItemsDescriptionConfiguration, HasProgress {

		@NotNull
		String getObjectName();

		long getObjectAmount();

		@Override
		default long getTotalAmount() {
			return getObjectAmount();
		}

	}

	public interface HasMultipleObjects<T> extends HasItemsDescriptionConfiguration, HasProgress {

		@NotNull
		Collection<CountableObject<T>> getObjects();

		default @Nullable CountableObject<T> getObject(int index) {
			return index >= getObjects().size() ? null : (CountableObject<T>) getObjects().toArray()[index];
		}

		@NotNull
		String getObjectName(CountableObject<T> object);

		@NotNull
		Map<CountableObject<T>, Integer> getRemainingAmounts(@NotNull Quester quester);

		default long getRemainingAmount(@NotNull Quester quester, CountableObject<T> object) {
			return getRemainingAmounts(quester).get(object);
		}

		@Override
		default long getRemainingAmount(@NotNull Quester quester) {
			return getRemainingAmounts(quester).values().stream().mapToInt(Integer::intValue).sum();
		}

		@Override
		default long getTotalAmount() {
			return getObjects().stream().mapToLong(CountableObject::getAmount).sum();
		}

	}

}
