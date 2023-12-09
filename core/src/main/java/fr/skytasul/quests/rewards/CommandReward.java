package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.gui.misc.CommandGUI;
import fr.skytasul.quests.utils.types.Command;

public class CommandReward extends AbstractReward {

	public List<Command> commands = new ArrayList<>();

	public CommandReward() {}
	
	public CommandReward(String customDescription, List<Command> list) {
		super(customDescription);
		if (list != null) this.commands.addAll(list);
	}

	@Override
	public List<String> give(Player p) {
		if (commands.isEmpty()) return null;
		for (Command cmd : commands){
			cmd.execute(p);
		}
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new CommandReward(getCustomDescription(), commands);
	}
	
	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(getCommandsSizeString());
	}

	private @NotNull String getCommandsSizeString() {
		return Lang.commands.quickFormat("amount", commands.size());
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ListGUI<Command>(Lang.INVENTORY_COMMANDS_LIST.toString(), DyeColor.ORANGE, commands) {
			
			@Override
			public void createObject(Function<Command, ItemStack> callback) {
				new CommandGUI(callback::apply, this::reopen).open(player);
			}
			
			@Override
			public void clickObject(Command object, ItemStack item, ClickType clickType) {
				new CommandGUI(command -> {
					updateObject(object, command);
					reopen();
				}, this::reopen).setFromExistingCommand(object).open(player);
			}

			@Override
			public ItemStack getObjectItemStack(Command cmd) {
				return ItemUtils.item(XMaterial.CHAIN_COMMAND_BLOCK, Lang.commandsListValue.format(cmd),
						createLoreBuilder(cmd)
								.addDescription(Lang.commandsListConsole
										.format(cmd.getPlaceholdersRegistry().shifted("command_console")))
								.toLoreArray());
			}
			
			@Override
			public void finish(List<Command> objects) {
				commands = objects;
				event.reopenGUI();
			}
			
		}.open(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("commands", Utils.serializeList(commands, Command::serialize));
	}

	@Override
	public void load(ConfigurationSection section){
		super.load(section);
		commands.addAll(Utils.deserializeList(section.getMapList("commands"), Command::deserialize));
	}

}
