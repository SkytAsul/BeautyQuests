package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.TextListEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.utils.XMaterial;

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
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		sendIndication(p);
		if (isMultiline()) {
			List<String> splitText = getValue() == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(getValue().split("\\{nl\\}")));
			new TextListEditor(p, list -> {
				setValue(list.stream().collect(Collectors.joining("{nl}")));
				ItemUtils.lore(item, getLore());
				gui.reopen(p);
			}, splitText).enter();
		}else {
			new TextEditor<String>(p, () -> gui.reopen(p), obj -> {
				setValue(obj);
				ItemUtils.lore(item, getLore());
				gui.reopen(p);
			}, () -> {
				resetValue();
				ItemUtils.lore(item, getLore());
				gui.reopen(p);
			}).enter();
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
