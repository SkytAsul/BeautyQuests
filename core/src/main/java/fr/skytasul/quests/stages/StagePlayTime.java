package fr.skytasul.quests.stages;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.PlayerPlaceholdersContext;
import fr.skytasul.quests.api.utils.progress.HasProgress;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StagePlayTime extends AbstractStage implements HasProgress {

	private final long playTicks;
	private final TimeMode timeMode;

	private Map<Player, BukkitTask> tasks = new HashMap<>();

	public StagePlayTime(StageController controller, long ticks, TimeMode timeMode) {
		super(controller);
		this.playTicks = ticks;
		this.timeMode = timeMode;
	}

	public long getTicksToPlay() {
		return playTicks;
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_PLAY_TIME.toString();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexedContextual("time_remaining_human", PlayerPlaceholdersContext.class,
				context -> Utils.millisToHumanString(getPlayerAmount(context.getPlayerAccount())));
		ProgressPlaceholders.registerProgress(placeholders, "time", this);
	}

	private long getRemaining(PlayerAccount acc) {
		switch (timeMode) {
			case ONLINE:
				long remaining = getData(acc, "remainingTime", Long.class);
				long lastJoin = getData(acc, "lastJoin", Long.class);
				long playedTicks = (System.currentTimeMillis() - lastJoin) / 50;
				return remaining - playedTicks;
			case OFFLINE:
				World world = Bukkit.getWorld(getData(acc, "worldUuid", UUID.class));
				if (world == null) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot get remaining time of " + acc.getNameAndID()
							+ " for " + controller + " because the world has changed.",
							acc.getNameAndID() + hashCode() + "time",
							15);
					return -1;
				}

				long startTime = getData(acc, "worldStartTime", Long.class);
				long elapsedTicks = world.getGameTime() - startTime;
				return playTicks - elapsedTicks;
			case REALTIME:
				startTime = getData(acc, "startTime", Long.class);
				elapsedTicks = (System.currentTimeMillis() - startTime) / 50;
				return playTicks - elapsedTicks;
		}
		throw new UnsupportedOperationException();
	}

	private void launchTask(Player p, long remaining) {
		tasks.put(p, Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> finishStage(p),
				remaining < 0 ? 0 : remaining));
	}

	@Override
	public long getPlayerAmount(@NotNull PlayerAccount account) {
		return getRemaining(account) * 50L;
	}

	@Override
	public long getTotalAmount() {
		return playTicks * 50L;
	}

	@Override
	public void joined(Player p) {
		super.joined(p);
		if (timeMode == TimeMode.ONLINE)
			updateObjective(p, "lastJoin", System.currentTimeMillis());
		launchTask(p, getRemaining(PlayersManager.getPlayerAccount(p)));
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
		if (timeMode == TimeMode.ONLINE)
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
		switch (timeMode) {
			case ONLINE:
				datas.put("remainingTime", playTicks);
				datas.put("lastJoin", System.currentTimeMillis());
				break;
			case OFFLINE:
				World world = Bukkit.getWorlds().get(0);
				datas.put("worldStartTime", world.getGameTime());
				datas.put("worldUuid", world.getUID().toString());
				break;
			case REALTIME:
				datas.put("startTime", System.currentTimeMillis());
				break;
		}
	}

	@Override
	public void unload() {
		super.unload();
		tasks.forEach(this::cancelTask);
		tasks.clear();
	}

	@Override
	public void setValidationRequirements(@NotNull RequirementList validationRequirements) {
		super.setValidationRequirements(validationRequirements);
		if (!validationRequirements.isEmpty())
			QuestsPlugin.getPlugin().getLogger().warning(validationRequirements.size()
					+ " requirements are set for a \"play time\" stage, but requirements are unsupported for this stage type.\n"
					+ controller.toString());
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("playTicks", playTicks);
		section.set("timeMode", timeMode.name());
	}

	public static StagePlayTime deserialize(ConfigurationSection section, StageController controller) {
		return new StagePlayTime(controller, section.getLong("playTicks"),
				TimeMode.valueOf(section.getString("timeMode", "ONLINE").toUpperCase()));
	}

	public enum TimeMode {
		ONLINE(Lang.stagePlayTimeModeOnline.toString()),
		OFFLINE(Lang.stagePlayTimeModeOffline.toString()) {
			@Override
			public boolean isActive() {
				// no way to get full world time before 1.16.5
				return MinecraftVersion.MAJOR > 16 || (MinecraftVersion.MAJOR == 16 && MinecraftVersion.MINOR == 5);
			}
		},
		REALTIME(Lang.stagePlayTimeModeRealtime.toString());

		private final String description;

		private TimeMode(String description) {
			this.description = description;
		}

		public boolean isActive() {
			return true;
		}
	}

	public static class Creator extends StageCreation<StagePlayTime> {

		private long ticks;
		private TimeMode timeMode = TimeMode.ONLINE;

		private int slotTicks;
		private int slotTimeMode;

		public Creator(@NotNull StageCreationContext<StagePlayTime> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.refreshItemName(SLOT_REQUIREMENTS,
					"§n" + Lang.validationRequirements + "§c " + Lang.Disabled.toString().toUpperCase());

			slotTicks = line.setItem(7, ItemUtils.item(XMaterial.CLOCK, Lang.changeTicksRequired.toString()), event -> {
				Lang.GAME_TICKS.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					setTicks(obj);
					event.reopen();
				}, MinecraftTimeUnit.TICK.getParser()).start();
			});

			slotTimeMode = line.setItem(8,
							ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.stagePlayTimeChangeTimeMode.toString(),
									QuestOption.formatNullableValue(timeMode.description, timeMode == TimeMode.ONLINE)),
							event -> {
						TimeMode next = timeMode;
						do {
							next = TimeMode.values()[(next.ordinal() + 1) % TimeMode.values().length];
						} while (!next.isActive());
						setTimeMode(next);
					});
		}

		public void setTicks(long ticks) {
			this.ticks = ticks;
			getLine().refreshItemLoreOptionValue(slotTicks, Lang.Ticks.quickFormat("ticks", ticks));
		}

		public void setTimeMode(TimeMode timeMode) {
			if (this.timeMode != timeMode) {
				this.timeMode = timeMode;
				getLine().refreshItemLore(slotTimeMode,
						QuestOption.formatNullableValue(timeMode.description, timeMode == TimeMode.ONLINE));
			}
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
			setTimeMode(stage.timeMode);
		}

		@Override
		public StagePlayTime finishStage(StageController controller) {
			return new StagePlayTime(controller, ticks, timeMode);
		}

	}

}
