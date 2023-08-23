package fr.skytasul.quests.commands;

import java.util.Optional;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Default;
import fr.skytasul.quests.api.commands.revxrsal.annotation.Subcommand;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.BukkitCommandActor;
import fr.skytasul.quests.api.commands.revxrsal.bukkit.annotation.CommandPermission;
import fr.skytasul.quests.api.commands.revxrsal.command.ExecutableCommand;
import fr.skytasul.quests.api.commands.revxrsal.exception.CommandErrorException;
import fr.skytasul.quests.api.commands.revxrsal.exception.InvalidSubcommandException;
import fr.skytasul.quests.api.commands.revxrsal.orphan.OrphanCommand;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.rewards.CheckpointReward;

public class CommandsPlayer implements OrphanCommand {
	
	@Default
	@CommandPermission ("beautyquests.command.listPlayer")
	public void menu(BukkitCommandActor actor, ExecutableCommand command,
			@fr.skytasul.quests.api.commands.revxrsal.annotation.Optional String subcommand) {
		if (subcommand != null)
			throw new InvalidSubcommandException(command.getPath(), subcommand);
		PlayerAccount acc = PlayersManager.getPlayerAccount(actor.requirePlayer());
		if (acc == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Player " + actor.getName() + " has got no account. This is a CRITICAL issue.");
			throw new CommandErrorException("no player datas");
		} else
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createPlayerQuestsMenu(acc).open(actor.getAsPlayer());
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
			Lang.COMMAND_CHECKPOINT_NO.send(player, quest.getPlaceholdersRegistry());
		}else Lang.COMMAND_CHECKPOINT_NOT_STARTED.send(player);
	}
	
}
