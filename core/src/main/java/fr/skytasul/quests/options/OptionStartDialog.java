package fr.skytasul.quests.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.editors.DialogEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.stages.types.Dialogable;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.utils.types.DialogRunnerImplementation;

public class OptionStartDialog extends QuestOption<Dialog> implements Dialogable {
	
	private DialogRunnerImplementation runner;
	
	public OptionStartDialog() {
		super(OptionStarterNPC.class);
	}
	
	@Override
	public Object save() {
		MemoryConfiguration section = new MemoryConfiguration();
		getValue().serialize(section);
		return section;
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(Dialog.deserialize(config.getConfigurationSection(key)));
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
		return new String[] {formatDescription(Lang.startDialogLore.toString()), "",
				getValue() == null ? Lang.NotSet.toString()
						: "§7" + Lang.AmountDialogLines.quickFormat("lines_amount", getValue().getMessages().size())};
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.startDialog.toString(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		Lang.NPC_TEXT.send(event.getPlayer());
		if (getValue() == null) setValue(new Dialog());
		new DialogEditor(event.getPlayer(), () -> {
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}, getValue()).start();
	}
	
	@Override
	public Dialog getDialog() {
		return getValue();
	}
	
	@Override
	public BqNpc getNPC() {
		return getAttachedQuest().getOptionValueOrDef(OptionStarterNPC.class);
	}
	
	@Override
	public void detach() {
		super.detach();
		if (runner != null) {
			runner.unload();
			runner = null;
		}
	}
	
	@Override
	public DialogRunner getDialogRunner() {
		if (runner == null) {
			runner = new DialogRunnerImplementation(getValue(), getNPC());
			runner.addEndAction(p -> getAttachedQuest().attemptStart(p));
		}
		return runner;
	}
	
}
