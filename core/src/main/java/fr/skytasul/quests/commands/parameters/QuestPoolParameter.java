package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.pools.QuestPoolController;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class QuestPoolParameter implements ParameterType<CommandActor, QuestPoolController> {

	@Override
	public QuestPoolController parse(@NotNull MutableStringStream input,
			@NotNull ExecutionContext<@NotNull CommandActor> context) {
		int id = input.readInt();
		var questPool = QuestsAPI.getAPI().getPoolsManager().getPool(id);
		if (questPool == null)
			throw new CommandErrorException(Lang.POOL_INVALID.quickFormat("pool_id", id));
		return questPool;
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull CommandActor> defaultSuggestions() {
		return context -> QuestsAPI.getAPI().getPoolsManager().getPools()
				.stream()
				.map(QuestPool -> Integer.toString(QuestPool.getId()))
				.toList();
	}

}
