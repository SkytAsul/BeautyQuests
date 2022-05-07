package fr.skytasul.quests.gui.creation;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.types.Command;

public class CommandGUI implements CustomInventory {
	
	private static final int SLOT_COMMAND = 1;
	private static final int SLOT_CONSOLE = 3;
	private static final int SLOT_PARSE = 4;
	private static final int SLOT_DELAY = 5;
	private static final int SLOT_FINISH = 8;
	
	private Consumer<Command> end;
	private Runnable cancel;
	private Inventory inv;
	
	private String cmd;
	private boolean console = false;
	private boolean parse = false;
	private int delay = 0;
	
	public CommandGUI(Consumer<Command> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
	}
	
	@Override
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_COMMAND.toString());
		
		inv.setItem(SLOT_COMMAND, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.commandValue.toString()));
		inv.setItem(SLOT_CONSOLE, ItemUtils.itemSwitch(Lang.commandConsole.toString(), console));
		if (DependenciesManager.papi.isEnabled()) inv.setItem(SLOT_PARSE, ItemUtils.itemSwitch(Lang.commandParse.toString(), parse));
		inv.setItem(SLOT_DELAY, ItemUtils.item(XMaterial.CLOCK, Lang.commandDelay.toString()));

		inv.setItem(SLOT_FINISH, ItemUtils.itemDone);
		inv.getItem(SLOT_FINISH).setType(cmd == null ? Material.COAL : Material.DIAMOND);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public CommandGUI setFromExistingCommand(Command cmd) {
		if (cmd != null) {
			this.cmd = cmd.label;
			this.console = cmd.console;
			this.parse = cmd.parse;
			this.delay = cmd.delay;
			if (inv != null && console) ItemUtils.set(inv.getItem(1), true);
			if (inv != null) inv.getItem(4).setType(Material.DIAMOND);
		}
		return this;
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){
		case SLOT_COMMAND:
			Lang.COMMAND.send(p);
			new TextEditor<String>(p, () -> p.openInventory(inv), cmd -> {
				this.cmd = cmd;
				inv.getItem(4).setType(Material.DIAMOND);
				p.openInventory(inv);
			}, () -> p.openInventory(inv), null).useStrippedMessage().enter();
			break;
			
		case SLOT_CONSOLE:
			console = ItemUtils.toggle(current);
			break;
			
		case SLOT_PARSE:
			parse = ItemUtils.toggle(current);
			break;
		
		case SLOT_DELAY:
			Lang.COMMAND_DELAY.send(p);
			new TextEditor<>(p, () -> p.openInventory(inv), x -> {
				delay = x;
				p.openInventory(inv);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;

		case SLOT_FINISH:
			if (current.getType() == Material.DIAMOND){
				Inventories.closeAndExit(p);
				end.accept(new Command(cmd, console, parse, delay));
			}
			break;
			
		}
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(cancel);
		return CloseBehavior.NOTHING;
	}

}
