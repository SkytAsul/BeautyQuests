package fr.skytasul.quests.commands.parameters;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

public class BqNpcParameter implements ParameterType<CommandActor, BqNpc> {

	@Override
	public BqNpc parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull CommandActor> context) {
		String id = input.readString();
		BqNpc npc = QuestsPlugin.getPlugin().getNpcManager().getById(id);
		if (npc == null)
			throw new CommandErrorException(Lang.NPC_DOESNT_EXIST.quickFormat("npc_id", id));
		return npc;
	}

	@Override
	public @NotNull SuggestionProvider<@NotNull CommandActor> defaultSuggestions() {
		return __ -> BeautyQuests.getInstance().getNpcManager().getAvailableIds();
	}

}
