package fr.skytasul.quests.gui.quests;

import java.util.ArrayList;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.PagedGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;

public class QuestsListGUI extends PagedGUI<Quest> {
	
	public RunnableObj run;

	public QuestsListGUI(RunnableObj run, PlayerAccount acc, boolean started, boolean notStarted, boolean finished){
		super(Lang.INVENTORY_QUESTS_LIST.toString(), DyeColor.CYAN,  new ArrayList<>());
		if (acc != null){
			if (started) super.objects.addAll(QuestsAPI.getQuestsStarteds(acc));
			if (notStarted) super.objects.addAll(QuestsAPI.getQuestsUnstarted(acc, false));
			if (finished) super.objects.addAll(QuestsAPI.getQuestsFinished(acc));
		}else super.objects.addAll(QuestsAPI.getQuests());
		this.run = run;
	}

	public ItemStack getItemStack(Quest qu){
		return ItemUtils.item(QuestsConfiguration.getItemMaterial(), "§6§l§o" + qu.getName() + "    §r§e#" + qu.getID(), Utils.format(Lang.TALK_NPC.toString(), qu.getStarter().getName()));
	}

	public void click(Quest existing){
		Inventories.closeAndExit(p);
		run.run(existing);
	}
	
	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REMOVE;
	}

}
