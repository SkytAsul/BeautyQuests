package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

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
		p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	}
	
	@Override
	public String toString() {
		return title + ", " + subtitle + ", " + Lang.Ticks.format(fadeIn + stay + fadeOut);
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("title", title);
		if (subtitle != null) map.put("subtitle", subtitle);
		if (fadeIn != FADE_IN) map.put("fadeIn", fadeIn);
		if (stay != STAY) map.put("stay", stay);
		if (fadeOut != FADE_OUT) map.put("fadeOut", fadeOut);
		
		return map;
	}
	
	public static Title deserialize(Map<String, Object> map) {
		String title = (String) map.get("title");
		String subtitle = (String) map.getOrDefault("subtitle", null);
		int fadeIn = map.containsKey("fadeIn") ? (int) map.get("fadeIn") : FADE_IN;
		int stay = map.containsKey("stay") ? (int) map.get("stay") : STAY;
		int fadeOut = map.containsKey("fadeOut") ? (int) map.get("fadeOut") : FADE_OUT;
		return new Title(title, subtitle, fadeIn, stay, fadeOut);
	}
	
}
