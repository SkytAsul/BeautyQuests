package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class QuestParameter implements ParameterType<CommandActor, Quest> {

	@Override
	public Quest parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull CommandActor> context) {
		int id = input.readInt();
		Quest quest = QuestsAPI.getAPI().getQuestsManager().getQuest(id);
		if (quest == null)
			throw new CommandErrorException(Lang.QUEST_INVALID.quickFormat("quest_id", id));
		return quest;
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull CommandActor> defaultSuggestions() {
		return context -> QuestsAPI.getAPI().getQuestsManager().getQuests()
				.stream()
				.map(quest -> Integer.toString(quest.getId()))
				.toList();
	}

}
