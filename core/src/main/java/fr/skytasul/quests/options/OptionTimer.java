package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OptionTimer extends QuestOption<Integer> {

	public OptionTimer() {
		super(OptionRepeatable.class);
	}

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getInt(key));
	}

	@Override
	public Integer cloneValue(Integer value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		return Utils.millisToHumanString(getValue() * 60L * 1000L);
	}

	@Override
	public boolean shouldDisplay(OptionSet options) {
		return options.getOption(OptionRepeatable.class).getValue();
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.CLOCK, Lang.timer.toString(), getLore());
	}

	private String[] getLore() {
		return new String[] {formatDescription(Lang.timerLore.toString()), "", formatValue()};
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(event.getPlayer(),
				MinecraftTimeUnit.MINUTE.getParser(), event::reopen, time -> {
					setValue(time.intValue());
					event.reopen();
				}).addReset(() -> {
					resetValue();
					event.reopen();
				}, "null")
				.setInitialString(getValue().toString())
				.setIndication(Lang.TIMER.toString())
				.build().start();
	}

}
