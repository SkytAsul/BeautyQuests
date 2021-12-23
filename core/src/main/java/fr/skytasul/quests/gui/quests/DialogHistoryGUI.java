package fr.skytasul.quests.gui.quests;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.Dialogable;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI.WrappedDialogable;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;

public class DialogHistoryGUI extends PagedGUI<WrappedDialogable> {
	
	public DialogHistoryGUI(PlayerQuestDatas datas) {
		super("Dialog history", DyeColor.LIGHT_BLUE, Collections.emptyList());
		Quest quest = datas.getQuest();
		Validate.notNull(quest, "Quest cannot be null");
		
		if (quest.hasOption(OptionStartDialog.class))
			objects.add(new WrappedDialogable(quest.getOption(OptionStartDialog.class)));
		
		Arrays.stream(datas.getQuestFlow().split(";"))
			.map(arg -> {
				String[] args = arg.split(":");
				int branchID = Integer.parseInt(args[0]);
				QuestBranch branch = quest.getBranchesManager().getBranch(branchID);
				if (branch == null) return null;
				if (args[1].startsWith("E")) {
					return branch.getEndingStage(Integer.parseInt(args[1].substring(1)));
				}else {
					return branch.getRegularStage(Integer.parseInt(args[1]));
				}
			})
			.filter(Dialogable.class::isInstance)
			.map(Dialogable.class::cast)
			.map(WrappedDialogable::new)
			.forEach(objects::add);
	}
	
	@Override
	public ItemStack getItemStack(WrappedDialogable object) {
		return null;
	}
	
	@Override
	public void click(WrappedDialogable existing, ItemStack item, ClickType clickType) {
		
	}
	
	class WrappedDialogable {
		final Dialogable dialogable;
		
		WrappedDialogable(Dialogable dialogable) {
			this.dialogable = dialogable;
		}
	}
	
}
