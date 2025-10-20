package fr.skytasul.quests.api.stages;

import org.jetbrains.annotations.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public sealed interface StageIndex {

	@Override
	@NotNull
	String toString();

	static final Pattern FLOW_PATTERN = Pattern.compile("(\\d+):(E)?(\\d+)");

	static @NotNull StageIndex fromString(@NotNull String string) throws IllegalArgumentException {
		Matcher matcher = FLOW_PATTERN.matcher(string);

		if (!matcher.matches())
			throw new IllegalArgumentException("Invalid stage index string: " + string);

		int branchId = Integer.parseInt(matcher.group(1));
		int stageId = Integer.parseInt(matcher.group(3));
		if (matcher.group(2) != null) {
			// means it matched the E meaning it's an ending stage
			return new EndingStageIndex(branchId, stageId);
		} else {
			return new RegularStageIndex(branchId, stageId);
		}
	}

	record RegularStageIndex(int branch, int stageIndex) implements StageIndex {
		@Override
		public final String toString() {
			return "%d:%d".formatted(branch, stageIndex);
		}
	}

	record EndingStageIndex(int branch, int stageId) implements StageIndex {
		@Override
		public final String toString() {
			return "%d:E%d".formatted(branch, stageId);
		}
	}

}
