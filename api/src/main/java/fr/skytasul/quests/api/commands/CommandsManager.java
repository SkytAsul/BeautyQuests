package fr.skytasul.quests.api.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.orphan.OrphanCommand;

public interface CommandsManager {

	@NotNull
	BukkitCommandHandler getHandler();

	void registerCommands(@Nullable String subpath, @NotNull OrphanCommand @NotNull... commands);

}
