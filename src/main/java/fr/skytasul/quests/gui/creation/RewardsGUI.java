package fr.skytasul.quests.gui.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.PermissionsEditor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ListGUI;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.rewards.CommandReward;
import fr.skytasul.quests.rewards.ItemReward;
import fr.skytasul.quests.rewards.MessageReward;
import fr.skytasul.quests.rewards.MoneyReward;
import fr.skytasul.quests.rewards.PermissionReward;
import fr.skytasul.quests.rewards.TeleportationReward;
import fr.skytasul.quests.rewards.XPReward;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.types.Command;
import fr.skytasul.quests.utils.types.RunnableObj;

public class RewardsGUI implements CustomInventory {

	private Inventory inv;
	private HashMap<Integer, Map<String, Object>> datas = new HashMap<>();

	private RunnableObj end;
	private Map<Class<?>, AbstractReward> lastRewards = new HashMap<>();

	public RewardsGUI(RunnableObj end, List<AbstractReward> rewards){
		this.end = end;
		for (AbstractReward req : rewards){
			lastRewards.put(req.getClass(), req);
		}
	}


	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, (int) StrictMath.ceil(RewardCreator.getCreators().size() * 1.0 / 9) * 9 + 9, Lang.INVENTORY_REWARDS.toString());

		inv.setItem(4, ItemUtils.itemDone());
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
			end.run(req);
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
		DebugUtils.broadcastDebugMessage("Initlializing default rewards.");

		QuestsAPI.registerReward(CommandReward.class, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), new CommandR());
		QuestsAPI.registerReward(ItemReward.class, ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), new ItemR());
		QuestsAPI.registerReward(MessageReward.class, ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), new MessageR());
		if (Dependencies.vault) QuestsAPI.registerReward(MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), new MoneyR());
		if (Dependencies.vault) QuestsAPI.registerReward(PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), new PermissionR());
		QuestsAPI.registerReward(TeleportationReward.class, ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), new TeleportationR());
		QuestsAPI.registerReward(XPReward.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), new XPR());
	}

}





/*                         RUNNABLES                    */
class CommandR implements RewardCreationRunnables{

	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		if (!datas.containsKey("commands")) datas.put("commands", new ArrayList<>());
		Inventories.create(p, new ListGUI<Command>((List<Command>) datas.get("commands")) {
			public void click(Command existing){
				Inventories.create(p, new CommandGUI((obj) -> {
					Command cmd = (Command) obj;
					this.finishItem(cmd);
				})).setFromExistingCommand(existing);
			}

			public String name(){
				return Lang.INVENTORY_COMMANDS_LIST.toString();
			}

			public void finish(){
				gui.reopen(p, true);
			}

			public ItemStack getItemStack(Command cmd){
				return ItemUtils.item(XMaterial.LIME_STAINED_GLASS_PANE, Lang.commandsListValue.format(cmd.label), Lang.commandsListConsole.format(cmd.console));
			}
		});
	}


	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack item){
		CommandReward rew = (CommandReward) reward;
		datas.put("cmd", new ArrayList<>(rew.commands));
		ItemUtils.lore(item, Lang.commands.format(rew.commands.size()));
	}


	public CommandReward finish(Map<String, Object> datas){
		return new CommandReward((List<Command>) datas.get("commands"));
	}

}

class ItemR implements RewardCreationRunnables{

	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		if (!datas.containsKey("items")) datas.put("items", new ArrayList<>());
		Inventories.create(p, new ItemsGUI(() -> {
			gui.reopen(p, true);
		}, (List<ItemStack>) datas.get("items")));
	}


	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		datas.put("items", ((ItemReward) reward).items);
	}


	public AbstractReward finish(Map<String, Object> datas){
		return new ItemReward((List<ItemStack>) datas.get("items"));
	}

}

class MessageR implements RewardCreationRunnables{


	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		Lang.END_MESSAGE.send(p);
		TextEditor wt = new TextEditor(p, (obj) -> {
			datas.put("text", obj);
			gui.reopen(p, false);
			ItemUtils.lore(clicked, (String) obj);
		});
		wt.cancel = () -> {
				if (!datas.containsKey("text")) gui.removeReward(datas);
				gui.reopen(p, false);
		};
		Editor.enterOrLeave(p, wt);
	}


	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		MessageReward rew = (MessageReward) reward;
		datas.put("text", rew.text);
		ItemUtils.lore(is, rew.text);
	}


	public AbstractReward finish(Map<String, Object> datas){
		return new MessageReward((String) datas.get("text"));
	}

}

class MoneyR implements RewardCreationRunnables{


	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		Lang.CHOOSE_MONEY_REWARD.send(p);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			datas.put("money", (int) obj);
			gui.reopen(p, false);
			ItemUtils.lore(clicked, "Money : " + obj);
		}, new NumberParser(Integer.class, false, true)));
	}


	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		MoneyReward rew = (MoneyReward) reward;
		datas.put("money", rew.money);
		ItemUtils.lore(is, "Money : " + rew.money);
	}


	public AbstractReward finish(Map<String, Object> datas){
		return new MoneyReward((int) datas.get("money"));
	}

}

class PermissionR implements RewardCreationRunnables{

	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		Lang.CHOOSE_PERM_REWARD.send(p);
		PermissionsEditor wt = new PermissionsEditor(p, (obj) -> {
			Map<String, Boolean> map = (Map<String, Boolean>) obj;
			if (map.isEmpty()) {
				gui.removeReward(datas);
			}else {
				datas.put("permissions", map);
				ItemUtils.lore(clicked, "Permissions : " + map.size());
			}
			gui.reopen(p, false);
		}, datas.containsKey("permissions") ? (Map<String, Boolean>) datas.get("permissions") : new HashMap<>());
		Editor.enterOrLeave(p, wt);
	}

	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		PermissionReward rew = (PermissionReward) reward;
		datas.put("permissions", rew.permissions);
		ItemUtils.lore(is, "Permissions : " + rew.permissions.size());
	}

	public AbstractReward finish(Map<String, Object> datas){
		return new PermissionReward((Map<String, Boolean>) datas.get("permissions"));
	}

}

class TeleportationR implements RewardCreationRunnables{

	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		Lang.MOVE_TELEPORT_POINT.send(p);
		Editor.enterOrLeave(p, new WaitClick(p, () -> {
				Location lc = p.getLocation();
				datas.put("loc", lc);
				ItemUtils.lore(clicked, Utils.locationToString(lc, true));
				gui.reopen(p, false);
		}, NPCGUI.validMove.clone()));
	}

	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		TeleportationReward rew = (TeleportationReward) reward;
		Location lc = rew.teleportation;
		datas.put("loc", lc);
		ItemUtils.lore(is, Utils.locationToString(lc, true));
	}

	public AbstractReward finish(Map<String, Object> datas){
		return new TeleportationReward((Location) datas.get("loc"));
	}

}

class XPR implements RewardCreationRunnables{

	public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked){
		String last = "" + (datas.containsKey("xp") ? datas.get("xp") : 0);
		Utils.sendMessage(p, Lang.XP_GAIN.toString(), last);
		Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
			Utils.sendMessage(p, Lang.XP_EDITED.toString(), last, obj);
			datas.put("xp", (int) obj);
			gui.reopen(p, false);
			ItemUtils.lore(clicked, obj + " xp");
		}, new NumberParser(Integer.class, true)));
	}

	public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is){
		XPReward rew = (XPReward) reward;
		datas.put("xp", rew.exp);
		ItemUtils.lore(is, rew.exp + " xp");
	}

	public AbstractReward finish(Map<String, Object> datas){
		return new XPReward((int) datas.get("xp"));
	}

}