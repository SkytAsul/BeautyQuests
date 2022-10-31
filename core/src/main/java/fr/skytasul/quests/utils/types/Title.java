package fr.skytasul.quests.utils.types;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;

public class Title {
	
	public static final int FADE_IN = 10;
	public static final int STAY = 70;
	public static final int FADE_OUT = 20;
	
	public final String title;
	public final String subtitle;
	public final int fadeIn;
	public final int stay;
	public final int fadeOut;
	
	public Title(String title, String subtitle) {
		this(title, subtitle, FADE_IN, STAY, FADE_OUT);
	}
	
	public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}
	
	public Title(Title other) {
		title = other.title;
		subtitle = other.subtitle;
		fadeIn = other.fadeIn;
		stay = other.stay;
		fadeOut = other.fadeOut;
	}
	
	public void send(Player p) {
		p.sendTitle(title == null ? "" : title, subtitle, fadeIn, stay, fadeOut);
	}
	
	@Override
	public String toString() {
		return title + ", " + subtitle + ", " + Lang.Ticks.format(fadeIn + stay + fadeOut);
	}
	
	public void serialize(ConfigurationSection section) {
		if (title != null) section.set("title", title);
		if (subtitle != null) section.set("subtitle", subtitle);
		if (fadeIn != FADE_IN) section.set("fadeIn", fadeIn);
		if (stay != STAY) section.set("stay", stay);
		if (fadeOut != FADE_OUT) section.set("fadeOut", fadeOut);
	}
	
	public static Title deserialize(ConfigurationSection section) {
		String title = section.getString("title", null);
		String subtitle = section.getString("subtitle", null);
		int fadeIn = section.getInt("fadeIn", FADE_IN);
		int stay = section.getInt("stay", STAY);
		int fadeOut = section.getInt("fadeOut", FADE_OUT);
		return new Title(title, subtitle, fadeIn, stay, fadeOut);
	}
	
}
