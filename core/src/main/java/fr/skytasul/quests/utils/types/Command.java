package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;

public class Command implements HasPlaceholders {

	public final String label;
	public final boolean console;
	public final boolean parse;
	public final int delay;
	
	private @Nullable PlaceholderRegistry placeholders;

	public Command(String label, boolean console, boolean parse, int delay) {
		this.label = label;
		this.console = console;
		this.parse = parse;
		this.delay = delay;
	}
	
	public void execute(Player o){
		Runnable run = () -> {
			String formattedCommand = MessageUtils.finalFormat(label, null, PlaceholdersContext.of(o, parse));

			CommandSender sender = console ? Bukkit.getConsoleSender() : o;
			Bukkit.dispatchCommand(sender, formattedCommand);
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(sender.getName() + " performed command " + formattedCommand);
		};
		if (delay == 0 && Bukkit.isPrimaryThread()) {
			run.run();
		}else Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), run, delay);
	}
	
	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null) {
			placeholders = new PlaceholderRegistry()
					.registerIndexed("command_label", label)
					.registerIndexed("command_console", console ? Lang.Yes : Lang.No)
					.register("command_parsed", parse ? Lang.Yes : Lang.No)
					.register("command_delay", Lang.Ticks.quickFormat("ticks", delay));
		}
		return placeholders;
	}

	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("label", label);
		map.put("console", console);
		if (parse) map.put("parse", parse);
		if (delay > 0) map.put("delay", delay);
		
		return map;
	}
	
	public static Command deserialize(Map<String, Object> map){
		return new Command((String) map.get("label"), (boolean) map.get("console"), (boolean) map.getOrDefault("parse", Boolean.FALSE), (int) map.getOrDefault("delay", 0));
	}
	
}
