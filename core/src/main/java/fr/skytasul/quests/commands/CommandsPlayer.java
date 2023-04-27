package fr.skytasul.quests.commands;

import java.util.Optional;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.rewards.CheckpointReward;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandsPlayer implements OrphanCommand {
	
	@Default
	@CommandPermission ("beautyquests.command.listPlayer")
	public void menu(BukkitCommandActor actor, ExecutableCommand command, @revxrsal.commands.annotation.Optional String subcommand) {
		if (subcommand != null) throw new revxrsal.commands.exception.InvalidSubcommandException(command.getPath(), subcommand);
		PlayerAccount acc = PlayersManager.getPlayerAccount(actor.requirePlayer());
		if (acc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Player " + actor.getName() + " has got no account. This is a CRITICAL issue.");
			throw new CommandErrorException("no player datas");
		}else new PlayerListGUI(acc).open(actor.getAsPlayer());
	}
	
	@Subcommand ("checkpoint")
	@CommandPermission ("beautyquests.command.checkpoint")
	public void checkpoint(Player player, Quest quest) {
		PlayerAccount account = PlayersManager.getPlayerAccount(player);
		if (account.hasQuestDatas(quest)) {
			PlayerQuestDatas datas = account.getQuestDatas(quest);
			QuestBranch branch = quest.getBranchesManager().getBranch(datas.getBranch());
			int max = datas.isInEndingStages() ? branch.getRegularStages().size() : datas.getStage();

			// TODO: should use quest flow

			for (int id = max - 1; id >= 0; id--) {
				AbstractStage stage = branch.getRegularStage(id).getStage();
				Optional<CheckpointReward> optionalCheckpoint = stage.getRewards().stream().filter(CheckpointReward.class::isInstance).findAny().map(CheckpointReward.class::cast);
				if (optionalCheckpoint.isPresent()) {
					optionalCheckpoint.get().applies(player);
					return;
				}
			}
			Lang.COMMAND_CHECKPOINT_NO.send(player, quest.getName());
		}else Lang.COMMAND_CHECKPOINT_NOT_STARTED.send(player);
	}
	
}
