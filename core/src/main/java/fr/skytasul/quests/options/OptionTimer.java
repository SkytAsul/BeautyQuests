package fr.skytasul.quests.options;

import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

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
	public boolean shouldDisplay(OptionSet options) {
		return options.getOption(OptionRepeatable.class).getValue();
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.CLOCK, Lang.timer.toString(), getLore());
	}

	private String[] getLore() {
		return new String[] {formatDescription(Lang.timerLore.toString()), "",
				formatValue(Utils.millisToHumanString(getValue() * 60L * 1000L))};
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		Lang.TIMER.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
			if (obj == null)
				resetValue();
			else
				setValue(obj.intValue());
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, MinecraftTimeUnit.MINUTE.getParser()).passNullIntoEndConsumer().start();
	}

}
