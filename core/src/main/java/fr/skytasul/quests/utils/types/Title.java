package fr.skytasul.quests.utils.types;

import fr.skytasul.quests.api.localization.Lang;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.util.Ticks;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Title {

	public static final int FADE_IN = 10;
	public static final int STAY = 70;
	public static final int FADE_OUT = 20;

	public final String title;
	public final String subtitle;
	public final int fadeIn;
	public final int stay;
	public final int fadeOut;

	private final net.kyori.adventure.title.Title adventureTitle;

	public Title(String title, String subtitle) {
		this(title, subtitle, FADE_IN, STAY, FADE_OUT);
	}

	public Title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;

		this.adventureTitle = net.kyori.adventure.title.Title.title(deserializeText(title), deserializeText(subtitle),
				Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)));
	}

	public Title(Title other) {
		this(other.title, other.subtitle, other.fadeIn, other.stay, other.fadeOut);
	}

	public void send(Audience audience) {
		audience.showTitle(adventureTitle);
	}

	private @NotNull Component deserializeText(@Nullable String string) {
		return LegacyComponentSerializer.legacySection().deserializeOr(string, Component.empty());
	}

	@Override
	public String toString() {
		return title + ", " + subtitle + ", " + Lang.Ticks.quickFormat("ticks", fadeIn + stay + fadeOut);
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
