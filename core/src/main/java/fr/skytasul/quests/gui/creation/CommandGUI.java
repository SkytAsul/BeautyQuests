package fr.skytasul.quests.gui.creation;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
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
import fr.skytasul.quests.utils.types.Command;

public class CommandGUI implements CustomInventory {
	
	private Consumer<Command> end;
	private Runnable cancel;
	private Inventory inv;
	
	private String cmd;
	private boolean console = false;
	private int delay = 0;
	
	public CommandGUI(Consumer<Command> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
	}
	
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_COMMAND.toString());
		
		inv.setItem(0, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.commandValue.toString()));
		inv.setItem(1, ItemUtils.itemSwitch(Lang.commandConsole.toString(), false));
		inv.setItem(2, ItemUtils.item(XMaterial.CLOCK, Lang.commandDelay.toString()));

		inv.setItem(4, ItemUtils.itemDone);
		inv.getItem(4).setType(cmd == null ? Material.COAL : Material.DIAMOND);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public CommandGUI setFromExistingCommand(Command cmd) {
		if (cmd != null) {
			this.cmd = cmd.label;
			this.console = cmd.console;
			this.delay = cmd.delay;
			if (console) ItemUtils.set(inv.getItem(1), true);
			if (inv != null) inv.getItem(4).setType(Material.DIAMOND);
		}
		return this;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){
		case 0:
			Lang.COMMAND.send(p);
			new TextEditor<String>(p, () -> p.openInventory(inv), cmd -> {
				this.cmd = cmd;
				inv.getItem(4).setType(Material.DIAMOND);
				p.openInventory(inv);
			}, () -> p.openInventory(inv), null).useStrippedMessage().enter();
			break;
			
		case 1:
			console = ItemUtils.toggle(current);
			break;
			
		case 2:
			Lang.COMMAND_DELAY.send(p);
			new TextEditor<>(p, () -> p.openInventory(inv), x -> {
				delay = x;
				p.openInventory(inv);
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			break;

		case 4:
			if (current.getType() == Material.DIAMOND){
				Inventories.closeAndExit(p);
				end.accept(new Command(cmd, console, delay));
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
