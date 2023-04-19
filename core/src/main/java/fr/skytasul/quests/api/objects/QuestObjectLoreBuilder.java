package fr.skytasul.quests.api.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.bukkit.event.inventory.ClickType;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.utils.Utils;

public class QuestObjectLoreBuilder {

	private static final List<ClickType> ORDERED_CLICKS =
			Arrays.asList(ClickType.RIGHT, ClickType.LEFT, ClickType.SHIFT_RIGHT, ClickType.SHIFT_LEFT);
	private static final Comparator<ClickType> CLICKS_COMPARATOR = Comparator.comparingInt(click -> {
		int i = ORDERED_CLICKS.indexOf(click);
		if (i != -1)
			return i;
		return click.ordinal() + ORDERED_CLICKS.size();
	});

	private List<String> description = new ArrayList<>(5);
	private Map<ClickType, String> clicks = new TreeMap<>(CLICKS_COMPARATOR);

	public QuestObjectLoreBuilder() {}

	public void addDescriptionRaw(String line) {
		description.add(line);
	}

	public void addDescription(String line) {
		addDescriptionRaw(QuestOption.formatDescription(line));
	}

	public void addDescriptionAsValue(Object value) {
		addDescription(QuestOption.formatNullableValue(value == null ? null : value.toString()));
	}

	public void addClick(ClickType click, String action) {
		if (click == null)
			return;

		clicks.put(click, action);
	}

	public String[] toLoreArray() {
		String[] lore = new String[description.size() + 1 + clicks.size()];
		int i = 0;

		// we iterate in the reverse order
		for (int j = description.size() - 1; j >= 0; j--) {
			lore[i++] = description.get(j);
		}

		lore[i++] = "";
		for (Entry<ClickType, String> entry : clicks.entrySet()) {
			lore[i++] = "§8" + Utils.clickName(entry.getKey()) + " > §7" + entry.getValue();
		}

		return lore;
	}

}
