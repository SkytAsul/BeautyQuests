package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy.EditableStrategy;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategyCreator;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class OptionQuesterStrategy extends QuestOption<QuestQuesterStrategy> implements QuestDescriptionProvider {

	@Override
	public @Nullable Object save() {
		var section = new MemoryConfiguration();
		getValue().serialize(section);
		return section.getValues(false);
	}

	@Override
	public void load(@NotNull ConfigurationSection config, @NotNull String key) {
		setValue(QuestQuesterStrategy.deserialize(config.getConfigurationSection(key),
				QuestsAPI.getAPI().getQuestQuesterStrategyRegistry()));
	}

	@Override
	public @Nullable QuestQuesterStrategy cloneValue(@Nullable QuestQuesterStrategy value) {
		return (QuestQuesterStrategy) value.clone();
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull OptionSet options) {
		var strategyCreator = (QuestQuesterStrategyCreator) getValue().getCreator();
		var isEditable = getValue() instanceof EditableStrategy;

		return ItemUtils.item(XMaterial.PLAYER_HEAD, "§b" + Lang.optionQuesterStrategy, "",
				QuestOption.formatNullableValue(strategyCreator.getName(), !hasCustomValue()), "",
				QuestOption.formatDescription(strategyCreator.getDescription()), "",
				"§8" + Lang.ClickLeft + " > §7" + Lang.pickAnother,
				isEditable ? "§8" + Lang.ClickRight + " > §7" + Lang.edit : null);
	}

	@Override
	public boolean shouldDisplay(@NotNull OptionSet options) {
		return QuestsAPI.getAPI().getQuestQuesterStrategyRegistry().getCreators().size() > 1;
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		if (event.getClick() == ClickType.LEFT) {
			new PagedGUI<>("Choose a strategy", DyeColor.MAGENTA,
					QuestsAPI.getAPI().getQuestQuesterStrategyRegistry().getCreators()) {
				@Override
				public @NotNull ItemStack getItemStack(@NotNull QuestQuesterStrategyCreator object) {
					return ItemUtils.item(XMaterial.PLAYER_HEAD, object.getName(),
							QuestOption.formatDescription(object.getDescription()));
				}

				@Override
				public void click(@NotNull QuestQuesterStrategyCreator existing, @NotNull ItemStack item,
						@NotNull ClickType clickType) {
					setValue(existing.newObject());
					event.getGui().updateOptionItem(OptionQuesterStrategy.this);
					event.reopen();
				}
			}.open(event.getPlayer());
		} else if (event.getClick() == ClickType.RIGHT && getValue() instanceof EditableStrategy strategy) {
			strategy.click(event);
		}
	}

	@Override
	public @Nullable List<@Nullable String> provideDescription(@NotNull QuestDescriptionContext context) {
		String description = getValue().getTooltip(context);
		if (description == null)
			return null;
		return List.of(description);
	}

	@Override
	public @NotNull String getDescriptionId() {
		return "quester_strategy";
	}

	@Override
	public double getDescriptionPriority() {
		return 30;
	}

}
