package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.rewards.CommandReward;
import fr.skytasul.quests.rewards.ItemReward;
import fr.skytasul.quests.rewards.MessageReward;
import fr.skytasul.quests.rewards.MoneyReward;
import fr.skytasul.quests.rewards.PermissionReward;
import fr.skytasul.quests.rewards.TeleportationReward;
import fr.skytasul.quests.rewards.XPReward;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;

public class RewardsGUI extends ListGUI<AbstractReward> {

	private Consumer<List<AbstractReward>> end;

	public RewardsGUI(Consumer<List<AbstractReward>> end, List<AbstractReward> rewards){
		super(rewards.stream().map(AbstractReward::clone).collect(Collectors.toCollection(ArrayList::new)), 18);
		this.end = end;
	}

	/**
	 * Get the RewardsGUI, open it for player if specified, and re implement the player in the inventories system if on true
	 * @param p player to open (can be null)
	 * @param reImplement re implement the player in the inventories system
	 * @return this RewardsGUI
	 */
	public RewardsGUI reopen(Player p) {
		if (p != null){
			Inventories.put(p, this, inv);
			p.openInventory(inv);
		}
		return this;
	}

	@Override
	public String name() {
		return Lang.INVENTORY_REWARDS.toString();
	}
	
	@Override
	public ItemStack getItemStack(AbstractReward object) {
		return ItemUtils.loreAdd(object.getItemStack(), "", Lang.Remove.toString());
	}
	
	@Override
	public void click(AbstractReward existing, ItemStack item) {
		if (existing == null) {
			new PagedGUI<RewardCreator<?>>(Lang.INVENTORY_REWARDS.toString(), DyeColor.CYAN, RewardCreator.creators.values()) {
				
				@Override
				public ItemStack getItemStack(RewardCreator<?> object) {
					return object.item;
				}
				
				@Override
				public void click(RewardCreator<?> existing) {
					finishItem(existing.newRewardSupplier.get());
				}
				
			};
		}else existing.itemClick(p, this, item);
	}
	
	@Override
	public void finish() {
		end.accept(objects);
	}


	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.registerReward(new RewardCreator<>(CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.registerReward(ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), new ItemReward.Creator());
		QuestsAPI.registerReward(MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), new MessageReward.Creator());
		if (DependenciesManager.vault) QuestsAPI.registerReward(MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), new MoneyReward.Creator());
		if (DependenciesManager.vault) QuestsAPI.registerReward(PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), new PermissionReward.Creator());
		QuestsAPI.registerReward(TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), new TeleportationReward.Creator());
		QuestsAPI.registerReward(XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), new XPReward.Creator());
	}

}