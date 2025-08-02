package fr.skytasul.quests.commands.parameters;

import com.google.common.collect.ForwardingList;
import fr.skytasul.quests.api.commands.QuesterSelector;
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
import java.util.List;

public class QuesterSelectorParameter implements ParameterType<BukkitCommandActor, QuesterSelector> {

	private final @NotNull QuesterManager questerManager;
	private final @NotNull PlayerManager playerManager;

	public QuesterSelectorParameter(@NotNull QuesterManager questerManager, @NotNull PlayerManager playerManager) {
		this.questerManager = questerManager;
		this.playerManager = playerManager;
	}

	@Override
	public QuesterSelector parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
		var string = input.readString();

		var questerOpt = QuesterParameter.parseQuester(string, questerManager);
		if (questerOpt.isPresent()) {
			return new QuesterSelectorImpl(List.of(questerOpt.get()));
		} else {
			var entities = Bukkit.selectEntities(context.actor().sender(), string);
			var players = entities.stream().filter(Player.class::isInstance).map(Player.class::cast).toList();
			if (players.isEmpty())
				throw new EmptyEntitySelectorException(string);
			return new QuesterSelectorImpl(players.stream().map(x -> (Quester) playerManager.getQuester(x)).toList());
		}
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
		return new QuesterParameter.SuggestionProviderImplementation(questerManager, playerManager);
	}

}


class QuesterSelectorImpl extends ForwardingList<Quester> implements QuesterSelector {

	private final List<Quester> list;

	public QuesterSelectorImpl(List<Quester> list) {
		this.list = list;
	}

	@Override
	protected List<Quester> delegate() {
		return list;
	}

}

