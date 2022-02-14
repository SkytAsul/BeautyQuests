package fr.skytasul.quests.options;

import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionCancelRewards extends QuestOptionRewards {
	
	@Override
	protected void attachedAsyncReward(AbstractReward reward) {}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.TNT_MINECART;
	}
	
	@Override
	public String getItemName() {
		return Lang.cancelRewards.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.cancelRewardsLore.toString();
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		new QuestObjectGUI<>(Lang.INVENTORY_CANCEL_ACTIONS.toString(), QuestObjectLocation.CANCELLING, QuestsAPI.getRewards().getCreators().stream().filter(x -> !x.canBeAsync()).collect(Collectors.toList()), objects -> {
			setValue(objects);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()).create(p);
	}
	
}
