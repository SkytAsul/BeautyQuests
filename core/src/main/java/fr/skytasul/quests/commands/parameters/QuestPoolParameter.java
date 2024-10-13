package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.pools.QuestPool;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class QuestPoolParameter implements ParameterType<CommandActor, QuestPool> {

	@Override
	public QuestPool parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull CommandActor> context) {
		int id = input.readInt();
		QuestPool QuestPool = QuestsAPI.getAPI().getPoolsManager().getPool(id);
		if (QuestPool == null)
			throw new CommandErrorException(Lang.POOL_INVALID.quickFormat("pool_id", id));
		return QuestPool;
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull CommandActor> defaultSuggestions() {
		return context -> QuestsAPI.getAPI().getPoolsManager().getPools()
				.stream()
				.map(QuestPool -> Integer.toString(QuestPool.getId()))
				.toList();
	}

}
