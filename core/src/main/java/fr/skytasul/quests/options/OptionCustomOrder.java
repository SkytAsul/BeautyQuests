package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
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

	private String[] getLore() {
		return new String[] {QuestOption.formatDescription(Lang.optionCustomOrderLore.toString()), "",
				QuestOption.formatNullableValue(getValue().isEmpty() ? null : getValue().getAsInt(), !hasCustomValue())};
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull OptionSet options) {
		return ItemUtils.item(XMaterial.REDSTONE, Lang.optionCustomOrder.toString(), getLore());
	}

	@Override
	public void click(@NotNull QuestCreationGuiClickEvent event) {
		Lang.QUEST_CUSTOM_ORDER.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, newValue -> {
			if (newValue == null)
				resetValue();
			else
				setValue(OptionalInt.of(newValue));
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, NumberParser.INTEGER_PARSER).passNullIntoEndConsumer().start();
	}

}
