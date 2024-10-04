package fr.skytasul.quests.api.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.orphan.OrphanCommand;

public interface CommandsManager {

	@NotNull
	Lamp<BukkitCommandActor> getHandler();

	void registerCommands(@Nullable String subpath, @NotNull OrphanCommand @NotNull... commands);

}
