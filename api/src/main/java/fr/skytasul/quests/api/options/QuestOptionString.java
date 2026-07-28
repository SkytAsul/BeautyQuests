package fr.skytasul.quests.api.options;

import com.cryptomorin.xseries.XMaterial;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class QuestOptionString extends QuestOption<String> {

	public QuestOptionString(Class<? extends QuestOption<?>>... requiredQuestOptions) {
		super(requiredQuestOptions);
	}

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getString(key));
	}

	@Override
	public String cloneValue(String value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		if (isMultiline() && getValue() != null)
			return "{nl}" + getValue();
		return getValue();
	}

	private String[] getLore() {
		if (getItemDescription() == null)
			return new String[] {formatValue()};

		String description = formatDescription(getItemDescription());
		return new String[] {description, "", formatValue()};
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		var editorBuilder = QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderString(event.getPlayer(), event::reopen, string -> {
			if (getOptionCreator().defaultValue != null && "none".equals(string))
				setValue(null);
			else
				setValue(string);
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}).addReset(() -> {
			resetValue();
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, "null").setIndication(getIndication()).allowEmpty();
		if (isMultiline()) {
			editorBuilder.allowMultiline();
			if (getValue() != null)
				editorBuilder.setInitialString(getValue().replace("{nl}", "\n"));
		} else {
			editorBuilder.setInitialString(getValue());
		}
		editorBuilder.build().start();
	}

	public abstract String getIndication();

	public abstract XMaterial getItemMaterial();

	public abstract String getItemName();

	public String getItemDescription() {
		return null;
	}

	public boolean isMultiline() {
		return false;
	}

}
