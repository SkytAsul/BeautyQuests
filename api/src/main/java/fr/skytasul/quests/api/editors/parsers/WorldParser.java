package fr.skytasul.quests.api.editors.parsers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldParser implements AbstractParser<World> {

	public World parse(String msg) throws ParsingError {
		World world = Bukkit.getWorld(msg);
		if (world == null)
			throw new ParsingError("§cThis world does not exist.");
		return world;
	}

	@Override
	public @Nullable String serialize(@NotNull World value) {
		return value.getName();
	}

}
