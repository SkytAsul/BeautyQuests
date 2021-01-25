package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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

	public List<Command> commands = new ArrayList<>();
	
	public CommandReward(){
		super("commandReward");
	}

	public CommandReward(List<Command> list){
		this();
		if (list != null) this.commands.addAll(list);
	}

	public List<String> give(Player p) {
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
		Inventories.create(p, new ListGUI<Command>(Lang.INVENTORY_COMMANDS_LIST.toString(), DyeColor.ORANGE, commands) {
			
			@Override
			public void createObject(Function<Command, ItemStack> callback) {
				new CommandGUI(command -> callback.apply(command), this::reopen).create(p);
			}
			
			@Override
			public void clickObject(Command object, ItemStack item, ClickType clickType) {
				new CommandGUI(command -> {
					updateObject(object, command);
					reopen();
				}, this::reopen).setFromExistingCommand(object).create(p);
			}

			@Override
			public ItemStack getObjectItemStack(Command cmd) {
				return ItemUtils.item(XMaterial.CHAIN_COMMAND_BLOCK, Lang.commandsListValue.format(cmd.label), Lang.commandsListConsole.format(cmd.console ? Lang.Yes : Lang.No));
			}
			
			@Override
			public void finish(List<Command> objects) {
				commands = objects;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		});
	}
	
	protected void save(Map<String, Object> datas){
		datas.put("commands", Utils.serializeList(commands, Command::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		commands.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("commands"), Command::deserialize));
	}

}
