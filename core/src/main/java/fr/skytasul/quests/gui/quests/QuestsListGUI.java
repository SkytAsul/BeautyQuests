package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public class QuestsListGUI extends PagedGUI<Quest> {
	
	public Consumer<Quest> run;

	public QuestsListGUI(Consumer<Quest> run, PlayerAccount acc, boolean started, boolean notStarted, boolean finished){
		super(Lang.INVENTORY_QUESTS_LIST.toString(), DyeColor.CYAN, new ArrayList<>(), null, Quest::getName);
		if (acc != null){
			if (started) super.objects.addAll(QuestsAPI.getQuests().getQuestsStarted(acc));
			if (notStarted) super.objects.addAll(QuestsAPI.getQuests().getQuestsNotStarted(acc, false, true));
			if (finished) super.objects.addAll(QuestsAPI.getQuests().getQuestsFinished(acc, false));
		}else super.objects.addAll(QuestsAPI.getQuests().getQuests());
		this.run = run;
	}

	@Override
	public ItemStack getItemStack(Quest qu){
		return ItemUtils.nameAndLore(qu.getQuestItem().clone(), "§6§l§o" + qu.getName() + "    §r§e#" + qu.getID(), qu.getDescription());
	}

	@Override
	public void click(Quest existing, ItemStack item, ClickType clickType){
		Inventories.closeAndExit(p);
		run.accept(existing);
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

}
