package fr.skytasul.quests.api.utils.itemdescription;

import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.types.HasProgress;
import fr.skytasul.quests.api.utils.CountableObject;

public interface HasItemsDescriptionConfiguration {

	@NotNull
	default ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
		return QuestsConfiguration.getConfig().getStageDescriptionConfig();
	}

	public interface HasSingleObject extends HasItemsDescriptionConfiguration, HasProgress {

		@NotNull
		String getObjectName();

		int getObjectAmount();

		@Override
		int getPlayerAmount(@NotNull PlayerAccount account);

		@Override
		default int getTotalAmount() {
			return getObjectAmount();
		}

	}

	public interface HasMultipleObjects<T> extends HasItemsDescriptionConfiguration {

		@NotNull
		Collection<CountableObject<T>> getObjects();

		default @Nullable CountableObject<T> getObject(int index) {
			return index >= getObjects().size() ? null : (CountableObject<T>) getObjects().toArray()[index];
		}

		@NotNull
		String getObjectName(CountableObject<T> object);

		@NotNull
		Map<CountableObject<T>, Integer> getPlayerAmounts(@NotNull PlayerAccount account);

		default int getTotalPlayerAmount(@NotNull PlayerAccount account) {
			return getPlayerAmounts(account).values().stream().mapToInt(Integer::intValue).sum();
		}

		default int getPlayerAmount(@NotNull PlayerAccount account, CountableObject<T> object) {
			return getPlayerAmounts(account).get(object);
		}

		default @NotNull HasSingleObject asTotalObject() {
			return new HasSingleObject() {
				@Override
				public @NotNull ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
					return getItemsDescriptionConfiguration();
				}

				@Override
				public int getPlayerAmount(@NotNull PlayerAccount account) {
					return getTotalPlayerAmount(account);
				}

				@Override
				public @NotNull String getObjectName() {
					return "total";
				}

				@Override
				public int getObjectAmount() {
					return getObjects().stream().mapToInt(CountableObject::getAmount).sum();
				}
			};
		}

	}

}
