package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
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
import fr.skytasul.quests.utils.compatibility.Dependencies;

public class RewardsGUI implements CustomInventory {

	private Inventory inv;
	private HashMap<Integer, Map<String, Object>> datas = new HashMap<>();

	private Consumer<List<AbstractReward>> end;
	private Map<Class<?>, AbstractReward> lastRewards = new HashMap<>();

	public RewardsGUI(Consumer<List<AbstractReward>> end, List<AbstractReward> rewards){
		this.end = end;
		for (AbstractReward req : rewards){
			lastRewards.put(req.getClass(), req);
		}
	}


	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, (int) StrictMath.ceil(RewardCreator.getCreators().size() * 1.0 / 9) * 9 + 9, Lang.INVENTORY_REWARDS.toString());

		inv.setItem(4, ItemUtils.itemDone);
		LinkedList<RewardCreator> ls = RewardCreator.getCreators();
		for (RewardCreator crea : ls){
			int id = ls.indexOf(crea) + 9;
			inv.setItem(id, crea.item.clone());
			if (lastRewards.containsKey(crea.clazz)){
				Map<String, Object> ldatas = initDatas(ls.indexOf(crea), crea);
				datas.put(id, ldatas);
				crea.runnables.edit(ldatas, lastRewards.get(crea.clazz), inv.getItem(id));
				usedLore(inv.getItem(id));
			}else ItemUtils.lore(inv.getItem(id), "", Lang.Unused.toString());
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public void removeReward(Map<String, Object> datas){
		for (Entry<Integer, Map<String, Object>> en : this.datas.entrySet()){
			if (en.getValue() == datas){
				remove(en.getKey());
				return;
			}
		}
	}

	public void remove(int slot){
		inv.setItem(slot, ItemUtils.lore(((RewardCreator) datas.get(slot).get("666DONOTREMOVE-creator")).item.clone(), "", Lang.Unused.toString()));
		datas.remove(slot);
	}

	private void usedLore(ItemStack is){
		ItemUtils.loreAdd(is, "", Lang.Used.toString(), Lang.Remove.toString());
	}

	private Map<String, Object> initDatas(int id, RewardCreator crea){
		Map<String, Object> data = new HashMap<>();
		data.put("666DONOTREMOVE-creator", crea);
		data.put("slot", id + 9);
		return data;
	}

	/**
	 * Get the RewardsGUI, open it for player if specified, and re implement the player in the inventories system if on true
	 * @param p player to open (can be null)
	 * @param reImplement re implement the player in the inventories system
	 * @return this RewardsGUI
	 */
	public RewardsGUI reopen(Player p, boolean reImplement){
		if (p != null){
			if (reImplement) Inventories.put(p, this, inv);
			p.openInventory(inv);
		}
		return this;
	}


	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot == 4){
			List<AbstractReward> req = new ArrayList<>();
			for (Entry<Integer, Map<String, Object>> data : datas.entrySet()){
				req.add(((RewardCreator) data.getValue().get("666DONOTREMOVE-creator")).runnables.finish(data.getValue()));
			}
			Inventories.closeAndExit(p);
			end.accept(req);
			return true;
		}
		if (!datas.containsKey(slot)){
			RewardCreator crea = RewardCreator.getCreators().get(slot - 9);
			datas.put(slot, initDatas(slot - 9, crea));
			ItemUtils.lore(current);
			crea.runnables.itemClick(p, datas.get(slot), this, current);
			usedLore(current);
		}else {
			if (click == ClickType.MIDDLE){
				remove(slot);
			}else {
				RewardCreator.getCreators().get(slot - 9).runnables.itemClick(p, datas.get(slot), this, current);
			}
		}
		return true;
	}



	public static void initialize(){
		DebugUtils.logMessage("Initlializing default rewards.");

		QuestsAPI.registerReward(CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), new CommandReward.Creator());
		QuestsAPI.registerReward(ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), new ItemReward.Creator());
		QuestsAPI.registerReward(MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), new MessageReward.Creator());
		if (Dependencies.vault) QuestsAPI.registerReward(MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), new MoneyReward.Creator());
		if (Dependencies.vault) QuestsAPI.registerReward(PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), new PermissionReward.Creator());
		QuestsAPI.registerReward(TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), new TeleportationReward.Creator());
		QuestsAPI.registerReward(XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), new XPReward.Creator());
	}

}