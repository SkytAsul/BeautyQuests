package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.CommandGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Command;

public class CommandReward extends AbstractReward {

	public final List<Command> commands = new ArrayList<>();
	
	public CommandReward(){
		super("commandReward");
	}

	public CommandReward(List<Command> list){
		this();
		if (list != null) this.commands.addAll(list);
	}

	public String give(Player p){
		if (commands.isEmpty()) return null;
		for (Command cmd : commands){
			cmd.execute(p);
		}
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("commands", Utils.serializeList(commands, Command::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		if (savedDatas.containsKey("command")){ // TODO: remove (edited since 0.14)
			commands.add(new Command((String) savedDatas.get("command"), (boolean) savedDatas.get("console"), 0));
		}else {
			commands.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("commands"), Command::deserialize));
		}
	}

	public static class Creator implements RewardCreationRunnables {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			if (!datas.containsKey("commands")) datas.put("commands", new ArrayList<>());
			Inventories.create(p, new ListGUI<Command>((List<Command>) datas.get("commands"), 9) {
				public void click(Command existing) {
					Inventories.create(p, new CommandGUI((cmd) -> this.finishItem(cmd))).setFromExistingCommand(existing);
				}

				public String name() {
					return Lang.INVENTORY_COMMANDS_LIST.toString();
				}

				public void finish() {
					gui.reopen(p, true);
				}

				public ItemStack getItemStack(Command cmd) {
					return ItemUtils.item(XMaterial.LIME_STAINED_GLASS_PANE, Lang.commandsListValue.format(cmd.label), Lang.commandsListConsole.format(cmd.console ? Lang.Yes : Lang.No));
				}
			});
		}

		public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack item) {
			CommandReward rew = (CommandReward) reward;
			datas.put("commands", new ArrayList<>(rew.commands));
			ItemUtils.lore(item, Lang.commands.format(rew.commands.size()));
		}

		public CommandReward finish(Map<String, Object> datas) {
			return new CommandReward((List<Command>) datas.get("commands"));
		}

	}

}
