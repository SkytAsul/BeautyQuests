package fr.skytasul.quests.gui.misc;

import java.util.HashMap;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.types.Command;

public class CommandGUI extends LayoutedGUI.LayoutedRowsGUI {
	
	private Consumer<Command> end;
	
	private String cmd;
	private boolean console = false;
	private boolean parse = false;
	private int delay = 0;
	
	private final @NotNull LayoutedButton doneButton;

	public CommandGUI(Consumer<Command> end, Runnable cancel) {
		super(Lang.INVENTORY_COMMAND.toString(), new HashMap<>(), new DelayCloseBehavior(cancel), 1);
		this.end = end;

		buttons.put(1,
				doneButton = LayoutedButton.createLoreValue(XMaterial.COMMAND_BLOCK, Lang.commandValue.toString(), () -> cmd,
				this::commandClick));
		buttons.put(3, LayoutedButton.create(() -> ItemUtils.itemSwitch(Lang.commandConsole.toString(), console),
				this::consoleClick));
		if (DependenciesManager.papi.isEnabled())
			buttons.put(4, LayoutedButton.create(() -> ItemUtils.itemSwitch(Lang.commandParse.toString(), parse),
					this::parseClick));
		buttons.put(5, LayoutedButton.createLoreValue(XMaterial.CLOCK, Lang.commandDelay.toString(), () -> delay,
				this::delayClick));
		buttons.put(8,
				LayoutedButton.create(() -> cmd == null ? ItemUtils.itemNotDone : ItemUtils.itemDone, this::doneClick));
	}

	public CommandGUI setFromExistingCommand(@Nullable Command cmd) {
		if (getInventory() != null)
			throw new IllegalStateException("GUI has already been built");
		if (cmd != null) {
			this.cmd = cmd.label;
			this.console = cmd.console;
			this.parse = cmd.parse;
			this.delay = cmd.delay;
		}
		return this;
	}
	
	private void commandClick(LayoutedClickEvent event) {
		Lang.COMMAND.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, cmd -> {
			this.cmd = cmd;
			refresh(doneButton);
			event.refreshItemReopen();
		}).passNullIntoEndConsumer().useStrippedMessage().start();
	}

	private void consoleClick(LayoutedClickEvent event) {
		console = !console;
		event.refreshItem();
	}

	private void parseClick(LayoutedClickEvent event) {
		parse = !parse;
		event.refreshItem();
	}

	private void delayClick(LayoutedClickEvent event) {
		Lang.COMMAND_DELAY.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, x -> {
			delay = x;
			event.refreshItemReopen();
		}, NumberParser.INTEGER_PARSER_POSITIVE).start();
	}

	private void doneClick(LayoutedClickEvent event) {
		if (cmd != null) {
			event.close();
			end.accept(new Command(cmd, console, parse, delay));
		}
	}

}
