package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.CommandGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
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

	@Override
	public AbstractReward clone() {
		return new CommandReward(commands);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + Lang.commands.format(commands.size()), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Inventories.create(p, new ListGUI<Command>(commands, 9) {
			public void click(Command existing, ItemStack item) {
				Inventories.create(p, new CommandGUI((cmd) -> this.finishItem(cmd))).setFromExistingCommand(existing);
			}

			public String name() {
				return Lang.INVENTORY_COMMANDS_LIST.toString();
			}

			public void finish() {
				ItemUtils.lore(clicked, getLore());
				gui.reopen(p);
			}

			public ItemStack getItemStack(Command cmd) {
				return ItemUtils.item(XMaterial.LIME_STAINED_GLASS_PANE, Lang.commandsListValue.format(cmd.label), Lang.commandsListConsole.format(cmd.console ? Lang.Yes : Lang.No));
			}
		});
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

}
