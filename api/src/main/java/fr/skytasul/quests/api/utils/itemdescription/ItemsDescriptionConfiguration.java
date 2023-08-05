package fr.skytasul.quests.api.utils.itemdescription;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.options.description.DescriptionSource;

public interface ItemsDescriptionConfiguration {

	@NotNull
	String getSingleItemFormat();

	@NotNull
	String getMultipleItemsFormat();

	String getSplitPrefix();

	boolean isAloneSplitInlined();

	Set<DescriptionSource> getSplitSources();

	default boolean isSourceSplit(@NotNull DescriptionSource source) {
		if (source == DescriptionSource.FORCESPLIT)
			return true;
		if (source == DescriptionSource.FORCELINE)
			return false;
		return getSplitSources().contains(source);
	}

}
