package fr.skytasul.quests.editors.checkers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import fr.skytasul.quests.utils.Lang;

public class ColorParser implements AbstractParser<Color> {
	
	public static final ColorParser PARSER = new ColorParser();
	
	private final Pattern hexPattern = Pattern.compile("^#([a-fA-F0-9]{6})$");
	private final Pattern rgbPattern = Pattern.compile("^\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b,? ?\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b,? ?\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b$");
	
	private ColorParser() {}
	
	@Override
	public Color parse(Player p, String msg) throws Throwable {
		int red, green, blue;
		
		Matcher hexMatcher = hexPattern.matcher(msg);
		if (hexMatcher.matches()) {
			String hex = hexMatcher.group(1);
			red = Integer.parseInt(hex.substring(0, 2), 16);
			green = Integer.parseInt(hex.substring(2, 4), 16);
			blue = Integer.parseInt(hex.substring(4, 6), 16);
		}else {
			Matcher rgbMatcher = rgbPattern.matcher(msg);
			if (rgbMatcher.matches()) {
				red = Integer.parseInt(rgbMatcher.group(1));
				green = Integer.parseInt(rgbMatcher.group(2));
				blue = Integer.parseInt(rgbMatcher.group(3));
			}else {
				try {
					// just in case the user has entered a named color
					java.awt.Color awtColor = ChatColor.valueOf(msg.toUpperCase().replace(' ', '_')).asBungee().getColor();
					red = awtColor.getRed();
					green = awtColor.getGreen();
					blue = awtColor.getBlue();
				}catch (IllegalArgumentException | NullPointerException ex) {
					Lang.INVALID_COLOR.send(p);
					return null;
				}
			}
		}
		return Color.fromRGB(red, green, blue);
	}
	
}
