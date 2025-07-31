package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.quests.quester.QuestQuesterStrategy;
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
		return ItemUtils.item(XMaterial.PLAYER_HEAD, "Change quester strategy",
				QuestOption.formatNullableValue(getValue().getName(), !hasCustomValue()));
	}

	@Override
	public boolean shouldDisplay(@NotNull OptionSet options) {
		return QuestsAPI.getAPI().getQuestQuesterStrategyRegistry().getCreators().size() > 1;
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		new PagedGUI<>("Choose a strategy", DyeColor.MAGENTA,
				QuestsAPI.getAPI().getQuestQuesterStrategyRegistry().getCreators()) {
			@Override
			public @NotNull ItemStack getItemStack(@NotNull QuestQuesterStrategyCreator object) {
				return object.getItem();
			}

			@Override
			public void click(@NotNull QuestQuesterStrategyCreator existing, @NotNull ItemStack item,
					@NotNull ClickType clickType) {
				setValue(existing.newObject());
				event.getGui().updateOptionItem(OptionQuesterStrategy.this);
				event.reopen();
			}
		}.open(event.getPlayer());
	}

	@Override
	public @Nullable List<@Nullable String> provideDescription(@NotNull QuestDescriptionContext context) {
		String description = getValue().getDescription();
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
