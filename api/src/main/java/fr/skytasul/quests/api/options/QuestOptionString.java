package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.TextListEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;

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

	private String[] getLore() {
		if (getItemDescription() == null) return new String[] { formatValue(getValue()) };

		String description = formatDescription(getItemDescription());
		return new String[] { description, "", formatValue((isMultiline() && getValue() != null ? "{nl}" : "") + getValue()) };
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		sendIndication(event.getPlayer());
		if (isMultiline()) {
			List<String> splitText = getValue() == null || getValue().isEmpty()
					? new ArrayList<>()
					: new ArrayList<>(Arrays.asList(getValue().split("\\{nl\\}")));
			new TextListEditor(event.getPlayer(), list -> {
				setValue(list.stream().collect(Collectors.joining("{nl}")));
				ItemUtils.lore(event.getClicked(), getLore());
				event.reopen();
			}, splitText).start();
		}else {
			new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
				if (obj == null)
					resetValue();
				else
					setValue(obj);
				ItemUtils.lore(event.getClicked(), getLore());
				event.reopen();
			}).passNullIntoEndConsumer().start();
		}
	}

	public abstract void sendIndication(Player p);

	public abstract XMaterial getItemMaterial();

	public abstract String getItemName();

	public String getItemDescription() {
		return null;
	}

	public boolean isMultiline() {
		return false;
	}

}
