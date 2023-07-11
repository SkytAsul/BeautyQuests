package fr.skytasul.quests.api.utils;

import java.util.Set;
import fr.skytasul.quests.api.options.description.DescriptionSource;

public interface SplittableDescriptionConfiguration {

	String getItemNameColor();

	String getItemAmountColor();

	String getSplitPrefix();

	String getSplitAmountFormat();

	boolean isAloneSplitAmountShown();

	boolean isAloneSplitInlined();

	Set<DescriptionSource> getSplitSources();

	default boolean isAloneSplitAmountShown(DescriptionSource source) {
		return getSplitSources().contains(source) && isAloneSplitAmountShown();
	}

}
