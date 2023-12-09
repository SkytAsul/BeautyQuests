package fr.skytasul.quests.options;

import java.util.stream.Collectors;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectGUI;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.utils.XMaterial;

public class OptionCancelRewards extends QuestOptionRewards {

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
	public void click(QuestCreationGuiClickEvent event) {
		new QuestObjectGUI<>(Lang.INVENTORY_CANCEL_ACTIONS.toString(), QuestObjectLocation.CANCELLING, QuestsAPI.getAPI()
				.getRewards().getCreators().stream().filter(x -> !x.canBeAsync()).collect(Collectors.toList()), objects -> {
					setValue(new RewardList(objects));
					ItemUtils.lore(event.getClicked(), getLore());
					event.reopen();
				}, getValue()).open(event.getPlayer());
	}

}
