package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.Utils;

public class StagePlayTime extends AbstractStage {

	private final long playTicks;
	
	private Map<Player, BukkitTask> tasks = new HashMap<>();
	
	public StagePlayTime(StageController controller, long ticks) {
		super(controller);
		this.playTicks = ticks;
	}
	
	public long getTicksToPlay() {
		return playTicks;
	}
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_PLAY_TIME.format(descriptionFormat(acc, source));
	}
	
	@Override
	public Supplier<Object>[] descriptionFormat(PlayerAccount acc, DescriptionSource source) {
		return new Supplier[] { () -> Utils.millisToHumanString(getRemaining(acc) * 50L) };
	}
	
	private long getRemaining(PlayerAccount acc) {
		long remaining = Utils.parseLong(getData(acc, "remainingTime"));
		long lastJoin = Utils.parseLong(getData(acc, "lastJoin"));
		long playedTicks = (System.currentTimeMillis() - lastJoin) / 50;
		return remaining - playedTicks;
	}
	
	private void launchTask(Player p, long remaining) {
		tasks.put(p, Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> finishStage(p),
				remaining < 0 ? 0 : remaining));
	}
	
	@Override
	public void joined(Player p) {
		super.joined(p);
		updateObjective(p, "lastJoin", System.currentTimeMillis());
		launchTask(p, Utils.parseLong(getData(p, "remainingTime")));
	}
	
	@Override
	public void left(Player p) {
		super.left(p);
		BukkitTask task = tasks.remove(p);
		if (task != null) {
			cancelTask(p, task);
		}else {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("Unavailable task in \"Play Time\" stage " + toString() + " for player " + p.getName());
		}
	}

	private void cancelTask(Player p, BukkitTask task) {
		task.cancel();
		updateObjective(p, "remainingTime", getRemaining(PlayersManager.getPlayerAccount(p)));
	}
	
	@Override
	public void started(PlayerAccount acc) {
		super.started(acc);

		if (acc.isCurrent())
			launchTask(acc.getPlayer(), playTicks);
	}
	
	@Override
	public void ended(PlayerAccount acc) {
		super.ended(acc);

		if (acc.isCurrent())
			tasks.remove(acc.getPlayer()).cancel();
	}

	@Override
	public void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("remainingTime", playTicks);
		datas.put("lastJoin", System.currentTimeMillis());
	}
	
	@Override
	public void unload() {
		super.unload();
		tasks.forEach(this::cancelTask);
		tasks.clear();
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("playTicks", playTicks);
	}
	
	public static StagePlayTime deserialize(ConfigurationSection section, StageController controller) {
		return new StagePlayTime(controller, section.getLong("playTicks"));
	}
	
	public static class Creator extends StageCreation<StagePlayTime> {
		
		private long ticks;

		public Creator(@NotNull StageCreationContext<StagePlayTime> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(7, ItemUtils.item(XMaterial.CLOCK, Lang.changeTicksRequired.toString()), event -> {
				Lang.GAME_TICKS.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					setTicks(obj);
					event.reopen();
				}, MinecraftTimeUnit.TICK.getParser()).start();
			});
		}
		
		public void setTicks(long ticks) {
			this.ticks = ticks;
			getLine().refreshItemLore(7, Lang.optionValue.format(ticks + " ticks"));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Lang.GAME_TICKS.send(p);
			new TextEditor<>(p, context::removeAndReopenGui, obj -> {
				setTicks(obj);
				context.reopenGui();
			}, MinecraftTimeUnit.TICK.getParser()).start();
		}
		
		@Override
		public void edit(StagePlayTime stage) {
			super.edit(stage);
			setTicks(stage.playTicks);
		}
		
		@Override
		public StagePlayTime finishStage(StageController controller) {
			return new StagePlayTime(controller, ticks);
		}
		
	}
	
}
