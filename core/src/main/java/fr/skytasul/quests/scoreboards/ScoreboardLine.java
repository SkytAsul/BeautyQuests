package fr.skytasul.quests.scoreboards;

import fr.skytasul.quests.api.utils.ChatColorUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import java.util.Map;

public class ScoreboardLine {

	private String value;
	private int refresh = 0;
	private int length = 0;

	public ScoreboardLine(String value) {
		Validate.notNull(value);
		this.value = ChatColor.translateAlternateColorCodes('&',
				ChatColorUtils.translateHexColorCodes(value
						.replace("{questName}", "{quest_name}")
						.replace("{questDescription}", "{quest_advancement}")));
	}

	public String getValue() {
		return value;
	}

	public int getRefreshTime() {
		return refresh;
	}

	public int getMaxLength() {
		return length;
	}

	public static ScoreboardLine deserialize(Map<String, Object> map) {
		ScoreboardLine line = new ScoreboardLine((String) map.get("value"));

		if (map.containsKey("refresh"))
			line.refresh = (int) map.get("refresh");
		if (map.containsKey("length"))
			line.length = (int) map.get("length");

		return line;
	}

}
