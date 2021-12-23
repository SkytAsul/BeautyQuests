package fr.skytasul.quests.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.Dialogable;
import fr.skytasul.quests.editors.DialogEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Dialog;

public class OptionStartDialog extends QuestOption<Dialog> implements Dialogable {
	
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
	public Dialog cloneValue(Dialog value) {
		return value.clone();
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return options.getOption(OptionStarterNPC.class).getValue() != null;
	}
	
	private String[] getLore() {
		return new String[] { formatDescription(Lang.startDialogLore.toString()), "", getValue() == null ? Lang.NotSet.toString() : "§7" + getValue().messages.valuesSize() + " line(s)" };
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.startDialog.toString(), getLore());
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		Utils.sendMessage(p, Lang.NPC_TEXT.toString());
		if (getValue() == null) setValue(new Dialog());
		new DialogEditor(p, () -> {
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()).enter();
	}
	
	@Override
	public Dialog getDialog() {
		return getValue();
	}
	
	@Override
	public BQNPC getNPC() {
		return getAttachedQuest().getOptionValueOrDef(OptionStarterNPC.class);
	}
	
}
