package fr.skytasul.quests.api.editors.checkers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class DurationParser implements AbstractParser<Long> {
	
	private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+) *([a-zA-Z]*)|(\\d+) *([a-zA-Z]+)");
	
	private final MinecraftTimeUnit targetUnit;
	private final MinecraftTimeUnit defaultUnit;
	
	public DurationParser(MinecraftTimeUnit targetUnit) {
		this(targetUnit, targetUnit);
	}
	
	public DurationParser(MinecraftTimeUnit targetUnit, MinecraftTimeUnit defaultUnit) {
		this.targetUnit = targetUnit;
		this.defaultUnit = defaultUnit;
	}
	
	@Override
	public Long parse(Player p, String msg) throws Throwable {
		Matcher matcher = DURATION_PATTERN.matcher(msg);
		long duration = 0;
		while (matcher.find()) {
			String num = matcher.group(1);
			if (StringUtils.isEmpty(num)) num = matcher.group(3);
			String unit = matcher.group(2);
			if (StringUtils.isEmpty(unit)) unit = matcher.group(4);
			
			MinecraftTimeUnit munit = StringUtils.isEmpty(unit) ? defaultUnit : MinecraftTimeUnit.of(unit);
			if (munit == null) {
				p.sendMessage("§cUnknown unit " + unit);
				return null;
			}
			duration += munit.in(targetUnit, Long.parseLong(num));
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
		
		private DurationParser cachedParser;
		
		private MinecraftTimeUnit(int previousDuration, String... names) {
			this.previousDuration = previousDuration;
			this.names = names;
		}
		
		public DurationParser getParser() {
			if (cachedParser == null) cachedParser = new DurationParser(this, this);
			return cachedParser;
		}
		
		static {
			for (MinecraftTimeUnit unit : values())
				for (String name : unit.names) UNITS.put(name, unit);
		}
		
		public long in(MinecraftTimeUnit unit, long duration) {
			if (ordinal() < unit.ordinal()) return 0;
			long finalDuration = duration;
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
