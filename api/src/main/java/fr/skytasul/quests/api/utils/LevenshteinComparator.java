package fr.skytasul.quests.api.utils;

import org.jetbrains.annotations.NotNull;
import java.util.Comparator;
import java.util.function.Function;

public class LevenshteinComparator<T> implements Comparator<T> {

	private final Function<T, String> function;
	private final String reference;

	public LevenshteinComparator(@NotNull Function<T, String> function, @NotNull String reference) {
		this.function = function;
		this.reference = reference;
	}

	@Override
	public int compare(T o1, T o2) {
		int s1 = computeDistance(reference, function.apply(o1));
		int s2 = computeDistance(reference, function.apply(o2));
		return s1 - s2;
	}

	public static int computeDistance(String reference, String string) {
		reference = reference.toLowerCase();
		string = string.toLowerCase();

		int[] costs = new int[string.length() + 1];
		for (int i = 0; i <= reference.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= string.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (reference.charAt(i - 1) != string.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[string.length()] = lastValue;
		}
		return costs[string.length()];
	}

	public static void printDistance(String s1, String s2) {
		System.out.println(s1 + "-->" + s2 + ": " + computeDistance(s1, s2));
	}

}
