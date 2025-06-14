package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.api.players.PlayerManager;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.EmptyEntitySelectorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class QuesterParameter implements ParameterType<BukkitCommandActor, Quester> {

	private @NotNull QuesterManager questerManager;
	private @NotNull PlayerManager playerManager;

	public QuesterParameter(@NotNull QuesterManager questerManager, @NotNull PlayerManager playerManager) {
		this.questerManager = questerManager;
		this.playerManager = playerManager;
	}

	@Override
	public Quester parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
		var string = input.readString();
		var player = Bukkit.getPlayer(string);
		if (player != null)
			return playerManager.getQuester(player);
		// TODO implement quester search
		throw new EmptyEntitySelectorException(string);
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
		return (context) -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
	}

}
