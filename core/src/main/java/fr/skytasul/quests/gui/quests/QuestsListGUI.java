package fr.skytasul.quests.gui.quests;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.function.Consumer;

public class QuestsListGUI extends PagedGUI<Quest> {
	
	public Consumer<Quest> run;

	public QuestsListGUI(Consumer<Quest> run, Quester acc, boolean started, boolean notStarted, boolean finished){
		super(Lang.INVENTORY_QUESTS_LIST.toString(), DyeColor.CYAN, new ArrayList<>(), null, Quest::getName);
		if (acc != null){
			if (started) super.objects.addAll(QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(acc));
			if (notStarted) super.objects.addAll(QuestsAPI.getAPI().getQuestsManager().getQuestsNotStarted(acc, false, true));
			if (finished) super.objects.addAll(QuestsAPI.getAPI().getQuestsManager().getQuestsFinished(acc, false));
		}else super.objects.addAll(QuestsAPI.getAPI().getQuestsManager().getQuests());
		this.run = run;
	}

	@Override
	public ItemStack getItemStack(Quest qu){
		return ItemUtils.nameAndLore(qu.getQuestItem().clone(), "§6§l§o" + qu.getName() + "    §r§e#" + qu.getId(), qu.getDescription());
	}

	@Override
	public void click(Quest existing, ItemStack item, ClickType clickType){
		close(player);
		run.accept(existing);
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REMOVE;
	}

}
