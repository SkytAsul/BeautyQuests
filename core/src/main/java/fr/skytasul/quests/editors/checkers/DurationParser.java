package fr.skytasul.quests.editors.checkers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class DurationParser implements AbstractParser<Integer> {
	
	private MinecraftTimeUnit minUnit;
	
	private Pattern durationPattern = Pattern.compile("^(\\d+) *([a-zA-Z]*)|(\\d+) *([a-zA-Z]+)");
	
	public DurationParser(MinecraftTimeUnit minUnit) {
		this.minUnit = minUnit;
	}
	
	@Override
	public Integer parse(Player p, String msg) throws Throwable {
		Matcher matcher = durationPattern.matcher(msg);
		int duration = 0;
		while (matcher.find()) {
			String num = matcher.group(1);
			if (StringUtils.isEmpty(num)) num = matcher.group(3);
			String unit = matcher.group(2);
			if (StringUtils.isEmpty(unit)) unit = matcher.group(4);
			
			MinecraftTimeUnit munit = StringUtils.isEmpty(unit) ? minUnit : MinecraftTimeUnit.of(unit);
			if (munit == null) {
				p.sendMessage("§cUnknown unit " + unit);
				return null;
			}
			duration += munit.in(minUnit, Integer.parseInt(num));
		}
		return duration;
	}
	
	public enum MinecraftTimeUnit {
		TICK(0, "tick", "ticks", "t"),
		SECOND(20, "second", "seconds", "s", "sec"),
		MINUTE(60, "minute", "minutes", "m", "min"),
		HOUR(60, "hour", "hours", "h"),
		DAY(24, "day", "days", "d"),
		WEEK(7, "week", "weeks", "w");
		
		private static final Map<String, MinecraftTimeUnit> UNITS = new HashMap<>();;
		
		private int previousDuration;
		private String[] names;
		
		private MinecraftTimeUnit(int previousDuration, String... names) {
			this.previousDuration = previousDuration;
			this.names = names;
		}
		
		static {
			for (MinecraftTimeUnit unit : values())
				for (String name : unit.names) UNITS.put(name, unit);
		}
		
		public int in(MinecraftTimeUnit unit, int duration) {
			if (ordinal() < unit.ordinal()) return 0;
			int finalDuration = duration;
			for (int i = ordinal(); i > unit.ordinal(); i--) {
				MinecraftTimeUnit previous = values()[i];
				finalDuration *= previous.previousDuration;
			}
			return finalDuration;
		}
		
		public static MinecraftTimeUnit of(String name) {
			return UNITS.get(name.toLowerCase());
		}
	}
	
}
