package fr.skytasul.quests.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Dialog;

public class OptionStartDialog extends QuestOption<Dialog> {
	
	public OptionStartDialog() {
		super(OptionStarterNPC.class);
	}
	
	@Override
	public Object save() {
		return getValue().serialize();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(Dialog.deserialize(Utils.mapFromConfigurationSection(config.getConfigurationSection(key))));
	}
	
	@Override
	public Dialog cloneValue() {
		return getValue().clone();
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return options.getOption(OptionStarterNPC.class).getValue() != null;
	}
	
	private String[] getLore() {
		return new String[] { formatDescription(Lang.startDialogLore.toString()), "", getValue() == null ? Lang.NotSet.toString() : "§7" + getValue().messages.valuesSize() + " string(s)" };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.startDialog.toString(), getLore());
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		Utils.sendMessage(p, Lang.NPC_TEXT.toString());
		if (getValue() == null) setValue(new Dialog(null));
		Editor.enterOrLeave(p, new DialogEditor(p, (obj) -> {
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()));
	}
	
}
