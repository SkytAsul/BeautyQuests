package fr.skytasul.quests.stages.options;

import fr.skytasul.quests.api.BossBarManager.BQBossBar;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import fr.skytasul.quests.api.utils.progress.HasProgress;
import fr.skytasul.quests.api.utils.progress.ProgressBarConfig;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class StageOptionProgressBar<T extends AbstractStage & HasProgress> extends StageOption<T> {

	private final @NotNull Map<Player, ProgressBar> bars = new HashMap<>();

	public StageOptionProgressBar(@NotNull Class<T> stageClass) {
		super(stageClass);
	}

	@Override
	public @NotNull StageOption<T> clone() {
		return new StageOptionProgressBar<>(getStageClass());
	}

	@Override
	public void startEdition(@NotNull StageCreation<T> creation) {
		// no data to save
	}

	@Override
	public boolean shouldSave() {
		return false;
	}

	@Override
	public void save(@NotNull ConfigurationSection section) {
		// no data to save
	}

	@Override
	public void load(@NotNull ConfigurationSection section) {
		// no data to save
	}

	@Override
	public void stageStart(Quester acc, StageController stage) {
		if (acc.isCurrent())
			createBar(acc.getPlayer(), (T) stage.getStage());
	}

	@Override
	public void stageEnd(Quester acc, StageController stage) {
		if (acc.isCurrent())
			removeBar(acc.getPlayer());
	}

	@Override
	public void stageJoin(Player p, StageController stage) {
		createBar(p, (T) stage.getStage());
	}

	@Override
	public void stageLeave(Player p, StageController stage) {
		removeBar(p);
	}

	@Override
	public void stageUpdated(@NotNull Player player, @NotNull StageController stage) {
		ProgressBar bar = bars.get(player);
		if (bar != null)
			bar.update();
	}

	@Override
	public void stageUnload(@NotNull StageController stage) {
		bars.values().forEach(ProgressBar::remove);
		bars.clear();
	}

	public ProgressBarConfig getProgressConfig() {
		return QuestsConfiguration.getConfig().getStageDescriptionConfig();
	}

	public boolean areBarsEnabled() {
		return getProgressConfig().areBossBarsEnabled() && QuestsAPI.getAPI().hasBossBarManager();
	}

	protected void createBar(@NotNull Player p, T progress) {
		if (areBarsEnabled()) {
			if (bars.containsKey(p)) { // NOSONAR Map#computeIfAbsent cannot be used here as we should log the issue
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Trying to create an already existing bossbar for player " + p.getName());
				return;
			}
			bars.put(p, new ProgressBar(p, progress));
		}
	}

	protected void removeBar(@NotNull Player p) {
		if (bars.containsKey(p))
			bars.remove(p).remove();
	}

	class ProgressBar {
		private final Quester acc;
		private final BQBossBar bar;
		private final T progress;
		private final long totalAmount;
		private final PlaceholderRegistry placeholders;

		private BukkitTask timer;

		public ProgressBar(Player p, T progress) {
			this.progress = progress;
			this.acc = PlayersManager.getPlayerAccount(p);
			this.totalAmount = progress.getTotalAmount();
			this.placeholders = PlaceholderRegistry.combine(progress); // to make a copy
			ProgressPlaceholders.registerProgress(placeholders, "progress", progress);

			BarStyle style = null;
			if (totalAmount % 20 == 0) {
				style = BarStyle.SEGMENTED_20;
			} else if (totalAmount % 10 == 0) {
				style = BarStyle.SEGMENTED_10;
			} else if (totalAmount % 12 == 0) {
				style = BarStyle.SEGMENTED_12;
			} else if (totalAmount % 6 == 0) {
				style = BarStyle.SEGMENTED_6;
			} else
				style = BarStyle.SOLID;

			bar = QuestsAPI.getAPI().getBossBarManager().buildBossBar("tmp", BarColor.YELLOW, style);
			update();
			bar.addPlayer(p);
		}

		public void remove() {
			bar.removeAll();
			if (timer != null)
				timer.cancel();
		}

		public void update() {
			timer();

			long playerRemaining = progress.getRemainingAmount(acc);
			if (playerRemaining >= 0 && playerRemaining <= totalAmount) {
				bar.setProgress((totalAmount - playerRemaining) * 1D / totalAmount);
			} else
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Amount of objects invalid in " + progress.getController().toString()
								+ " for player " + acc.getNameAndID() + ": " + playerRemaining + " / " + totalAmount);

			bar.setTitle(MessageUtils.format(getProgressConfig().getBossBarFormat(), placeholders,
					PlaceholdersContext.of(acc, true, null)));
			bar.addPlayer(acc.getPlayer());
		}

		private void timer() {
			if (getProgressConfig().getBossBarTimeout() <= 0)
				return;

			if (timer != null)
				timer.cancel();

			timer = Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(), () -> {
				bar.removePlayer(acc.getPlayer());
				timer = null;
			}, getProgressConfig().getBossBarTimeout() * 20L);
		}
	}

}
