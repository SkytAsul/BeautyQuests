package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.events.QuesterJoinEvent;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionAutoQuest extends QuestOption<OptionAutoQuest.Mode> implements Listener {

	@Override
	public @Nullable Object save() {
		return getValue().name();
	}

	@Override
	public void load(@NotNull ConfigurationSection config, @NotNull String key) {
		if (config.isBoolean(key))
			setValue(config.getBoolean(key) ? Mode.NEW_PLAYERS_ONLY : Mode.DISABLED);
		else
			setValue(Mode.valueOf(config.getString(key)));
	}

	@Override
	public @Nullable Mode cloneValue(@Nullable Mode value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		return getValue().title.toString();
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull OptionSet options) {
		Mode selectedMode = getValue();
		return ItemUtils.item(selectedMode.material, selectedMode.color + Lang.autoModeTitle.toString(),
				QuestOption.formatDescription(Lang.autoModeDescription.toString()), "",
				formatValue());
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		setValue(Mode.values()[(getValue().ordinal() + 1) % Mode.values().length]);
		event.refreshItem();
	}

	@Override
	public void attach(@NotNull Quest quest) {
		super.attach(quest);

		if (getValue() != Mode.ALL_PLAYERS)
			return;
		Bukkit.getScheduler().runTask(QuestsPlugin.getPlugin(), () -> {
			// we cannot start the quests before all options have been attached
			for (Quester quester : QuestsAPI.getAPI().getQuesterManager().getLoadedQuesters()) {
				if (!quester.isActive() || !quest.getQuesterStrategy().isQuesterApplicable(quester)
						|| quester.getDataHolder().hasQuestData(quest))
					continue;
				QuestsPlugin.getPlugin().getLoggerExpanded().debug(
						"Automatically launching quest {0} to {1} after quest creation/edition",
						quest.getId(), quester.getDetailedName());
				quest.start(quester, false);
			}
		});
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onAccountJoin(QuesterJoinEvent e) {
		Quest quest = getAttachedQuest();
		if (!quest.getQuesterStrategy().isQuesterApplicable(e.getQuester()))
			return;

		boolean shouldStart = switch (getValue()) {
			case DISABLED -> false;
			case ALL_PLAYERS -> !e.getQuester().getDataHolder().hasQuestData(quest);
			case NEW_PLAYERS_ONLY -> e.isFirstJoin();
		};
		if (shouldStart) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug(
					"Automatically launching quest {0} to {1} after logging in (mode {2})",
					quest.getId(), e.getQuester().getDetailedName(), getValue());
			quest.start(e.getQuester(), false);
		}
	}

	public enum Mode {
		DISABLED(XMaterial.GRAY_DYE, ChatColor.GRAY, Lang.autoModeDisabled),
		NEW_PLAYERS_ONLY(XMaterial.YELLOW_DYE, ChatColor.YELLOW, Lang.autoModeNewPlayers),
		ALL_PLAYERS(XMaterial.LIME_DYE, ChatColor.GREEN, Lang.autoModeAllPlayers),
		;

		private @NotNull XMaterial material;
		private @NotNull ChatColor color;
		private @NotNull Lang title;

		private Mode(@NotNull XMaterial material, @NotNull ChatColor color, @NotNull Lang title) {
			this.material = material;
			this.color = color;
			this.title = title;
		}
	}

}
