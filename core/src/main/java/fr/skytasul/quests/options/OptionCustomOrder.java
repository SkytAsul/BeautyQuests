package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.OptionalInt;

public class OptionCustomOrder extends QuestOption<OptionalInt> {

	@Override
	public @Nullable Integer save() {
		return getValue().isEmpty() ? null : getValue().getAsInt();
	}

	@Override
	public void load(@NotNull ConfigurationSection config, @NotNull String key) {
		if (config.contains(key))
			setValue(OptionalInt.of(config.getInt(key)));
	}

	@Override
	public @Nullable OptionalInt cloneValue(@Nullable OptionalInt value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		return getValue().isEmpty() ? null : Integer.toString(getValue().getAsInt());
	}

	private String[] getLore() {
		return new String[] {QuestOption.formatDescription(Lang.optionCustomOrderLore.toString()), "", formatValue()};
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull OptionSet options) {
		return ItemUtils.item(XMaterial.REDSTONE, Lang.optionCustomOrder.toString(), getLore());
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(event.getPlayer(),
				NumberParser.INTEGER_PARSER, event::reopen, newOrder -> {
					setValue(OptionalInt.of(newOrder));
					event.reopen();
				}).addReset(() -> {
					resetValue();
					event.reopen();
				}, "null")
				.setInitialString(getValue().isEmpty() ? null : Integer.toString(getValue().getAsInt()))
				.setIndication(Lang.QUEST_CUSTOM_ORDER.toString()).build().start();
	}

}
