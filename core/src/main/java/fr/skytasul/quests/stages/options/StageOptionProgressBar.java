package fr.skytasul.quests.stages.options;

import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.Quester;
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
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class StageOptionProgressBar<T extends AbstractStage & HasProgress> extends StageOption<T> {

	private final @NotNull Map<Quester, ProgressBar> bars = new HashMap<>();

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
	public void stageStart(Quester quester, StageController stage) {
		if (areBarsEnabled())
			bars.computeIfAbsent(quester, __ -> new ProgressBar(quester, (T) stage.getStage()));
	}

	@Override
	public void stageEnd(Quester quester, StageController stage) {
		removeBar(quester);
	}

	@Override
	public void stageJoin(Player p, @NotNull Quester quester, StageController stage) {
		if (areBarsEnabled())
			bars.computeIfAbsent(quester, __ -> new ProgressBar(quester, (T) stage.getStage())).update();
	}

	@Override
	public void stageLeave(Player p, @NotNull Quester quester, StageController stage) {
		removeBar(quester);
	}

	@Override
	public void stageUpdated(@NotNull Quester quester, @NotNull StageController stage) {
		ProgressBar bar = bars.get(quester);
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
		return getProgressConfig().areBossBarsEnabled();
	}

	protected void removeBar(@NotNull Quester quester) {
		if (bars.containsKey(quester))
			bars.remove(quester).remove();
	}

	class ProgressBar {
		private final Quester quester;
		private final T progress;
		private final long totalAmount;
		private final PlaceholderRegistry placeholders;

		private final BossBar bar;

		private BukkitTask timer;

		public ProgressBar(Quester quester, T progress) {
			this.quester = quester;
			this.progress = progress;
			this.totalAmount = progress.getTotalAmount();
			this.placeholders = PlaceholderRegistry.combine(progress); // to make a copy
			ProgressPlaceholders.registerProgress(placeholders, "progress", progress);

			Overlay style = null;
			if (totalAmount % 20 == 0) {
				style = Overlay.NOTCHED_20;
			} else if (totalAmount % 10 == 0) {
				style = Overlay.NOTCHED_10;
			} else if (totalAmount % 12 == 0) {
				style = Overlay.NOTCHED_12;
			} else if (totalAmount % 6 == 0) {
				style = Overlay.NOTCHED_6;
			} else
				style = Overlay.PROGRESS;

			bar = BossBar.bossBar(Component.empty(), 0, Color.YELLOW, style);

			update();
			bar.addViewer(quester);
		}

		public void remove() {
			bar.removeViewer(quester);
			if (timer != null)
				timer.cancel();
		}

		public void update() {
			timer();

			long playerRemaining = progress.getRemainingAmount(quester);
			if (playerRemaining >= 0 && playerRemaining <= totalAmount) {
				float progress = (totalAmount - playerRemaining) * 1F / totalAmount;
				bar.progress(progress);
			} else
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Amount of objects invalid in " + progress.getController().toString()
								+ " for player " + quester.getNameAndID() + ": " + playerRemaining + " / " + totalAmount);

			String formattedName = MessageUtils.format(getProgressConfig().getBossBarFormat(), placeholders,
					PlaceholdersContext.of(quester, true, null));
			bar.name(LegacyComponentSerializer.legacySection().deserialize(formattedName));
			bar.addViewer(quester);
		}

		private void timer() {
			if (getProgressConfig().getBossBarTimeout() <= 0)
				return;

			if (timer != null)
				timer.cancel();

			timer = Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(), () -> {
				bar.removeViewer(quester);
				timer = null;
			}, getProgressConfig().getBossBarTimeout() * 20L);
		}
	}

}
