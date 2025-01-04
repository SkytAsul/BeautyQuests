package fr.skytasul.quests.commands;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.rewards.CheckpointReward;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.orphan.OrphanCommand;
import java.util.Objects;
import java.util.Optional;

public class CommandsPlayer implements OrphanCommand {

	@CommandPlaceholder
	@CommandPermission ("beautyquests.command.listPlayer")
	public void menu(BukkitCommandActor actor, @revxrsal.commands.annotation.Optional String subcommand) {
		if (subcommand != null)
			throw new UnknownCommandException(subcommand);
		Quester acc = PlayersManager.getPlayerAccount(actor.requirePlayer());
		if (acc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("Player " + actor.name() + " has got no account. This is a CRITICAL issue.");
			throw new CommandErrorException("no player datas");
		} else
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createPlayerQuestsMenu(acc).open(actor.asPlayer());
	}

	@Subcommand ("checkpoint")
	@CommandPermission ("beautyquests.command.checkpoint")
	public void checkpoint(Player player, Quest quest) {
		Quester account = PlayersManager.getPlayerAccount(player);
		if (account.hasQuestDatas(quest)) {
			PlayerQuestDatas datas = account.getQuestDatas(quest);

			Optional<CheckpointReward> optionalCheckpoint = datas.getQuestFlowStages()
					.map(controller -> controller.getStage().getRewards().stream()
							.filter(CheckpointReward.class::isInstance).findAny()
							.map(CheckpointReward.class::cast).orElse(null))
					.filter(Objects::nonNull)
					.reduce((left, right) -> right);

			if (optionalCheckpoint.isPresent())
				optionalCheckpoint.get().apply(player);
			else
				Lang.COMMAND_CHECKPOINT_NO.send(player, quest.getPlaceholdersRegistry());

		}else Lang.COMMAND_CHECKPOINT_NOT_STARTED.send(player);
	}

}
