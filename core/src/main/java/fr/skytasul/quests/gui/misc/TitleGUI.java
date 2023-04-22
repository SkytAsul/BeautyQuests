package fr.skytasul.quests.gui.misc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Title;

public class TitleGUI implements CustomInventory {
	
	private static final int SLOT_TITLE = 0;
	private static final int SLOT_SUBTITLE = 1;
	private static final int SLOT_FADE_IN = 3;
	private static final int SLOT_STAY = 4;
	private static final int SLOT_FADE_OUT = 5;
	
	private Consumer<Title> end;
	private String title;
	private String subtitle;
	private int fadeIn = Title.FADE_IN;
	private int stay = Title.STAY;
	private int fadeOut = Title.FADE_OUT;
	
	private boolean canFinish = false;
	
	private Inventory inv;
	
	public TitleGUI(Consumer<Title> end) {
		this.end = end;
	}
	
	public TitleGUI edit(Title edit) {
		if (edit != null) {
			title = edit.title;
			subtitle = edit.subtitle;
			fadeIn = edit.fadeIn;
			stay = edit.stay;
			fadeOut = edit.fadeOut;
		}
		return this;
	}
	
	private void updateFinishState() {
		canFinish = title != null || subtitle != null;
		if (inv != null) {
			Material material = canFinish ? Material.DIAMOND : Material.COAL;
			ItemStack item = inv.getItem(8);
			if (item.getType() != material) item.setType(material);
		}
	}
	
	public void setTitle(String title) {
		this.title = title;
		ItemUtils.lore(inv.getItem(SLOT_TITLE), QuestOption.formatNullableValue(title));
	}
	
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
		ItemUtils.lore(inv.getItem(SLOT_SUBTITLE), QuestOption.formatNullableValue(subtitle));
	}
	
	public void setFadeIn(int fadeIn) {
		this.fadeIn = fadeIn;
		ItemUtils.lore(inv.getItem(SLOT_FADE_IN), QuestOption.formatNullableValue(Lang.Ticks.format(fadeIn), fadeIn == Title.FADE_IN));
	}
	
	public void setStay(int stay) {
		this.stay = stay;
		ItemUtils.lore(inv.getItem(SLOT_STAY), QuestOption.formatNullableValue(Lang.Ticks.format(stay), stay == Title.STAY));
	}
	
	public void setFadeOut(int fadeOut) {
		this.fadeOut = fadeOut;
		ItemUtils.lore(inv.getItem(SLOT_FADE_OUT), QuestOption.formatNullableValue(Lang.Ticks.format(fadeOut), fadeOut == Title.FADE_OUT));
	}
	
	@Override
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_EDIT_TITLE.toString());
		
		inv.setItem(SLOT_TITLE, ItemUtils.item(XMaterial.NAME_TAG, Lang.title_title.toString()));
		inv.setItem(SLOT_SUBTITLE, ItemUtils.item(XMaterial.NAME_TAG, Lang.title_subtitle.toString()));
		inv.setItem(SLOT_FADE_IN, ItemUtils.item(XMaterial.CLOCK, Lang.title_fadeIn.toString()));
		inv.setItem(SLOT_STAY, ItemUtils.item(XMaterial.CLOCK, Lang.title_stay.toString()));
		inv.setItem(SLOT_FADE_OUT, ItemUtils.item(XMaterial.CLOCK, Lang.title_fadeOut.toString()));
		
		inv.setItem(7, ItemUtils.itemCancel);
		inv.setItem(8, ItemUtils.itemDone.toMutableStack());
		
		// updating lores
		setTitle(title);
		setSubtitle(subtitle);
		setFadeIn(fadeIn);
		setStay(stay);
		setFadeOut(fadeOut);
		updateFinishState();
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case SLOT_TITLE:
			startStringEditor(p, Lang.TITLE_TITLE.toString(), this::setTitle);
			break;
		case SLOT_SUBTITLE:
			startStringEditor(p, Lang.TITLE_SUBTITLE.toString(), this::setSubtitle);
			break;
		case SLOT_FADE_IN:
			startIntEditor(p, Lang.TITLE_FADEIN.toString(), this::setFadeIn);
			break;
		case SLOT_STAY:
			startIntEditor(p, Lang.TITLE_STAY.toString(), this::setStay);
			break;
		case SLOT_FADE_OUT:
			startIntEditor(p, Lang.TITLE_FADEOUT.toString(), this::setFadeOut);
			break;
		case 7:
			Inventories.closeAndExit(p);
			end.accept(null);
			break;
		case 8:
			Inventories.closeAndExit(p);
			end.accept(new Title(title, subtitle, fadeIn, stay, fadeOut));
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(() -> end.accept(null));
		return CloseBehavior.REMOVE;
	}

	private void startStringEditor(Player p, String helpMsg, Consumer<String> setter) {
		Utils.sendMessage(p, helpMsg);
		new TextEditor<String>(p, () -> {
			p.openInventory(inv);
		}, msg -> {
			setter.accept(msg);
			updateFinishState();
			p.openInventory(inv);
		}).passNullIntoEndConsumer().enter();
	}
	
	private void startIntEditor(Player p, String helpMsg, Consumer<Integer> setter) {
		Utils.sendMessage(p, helpMsg);
		new TextEditor<>(p, () -> {
			p.openInventory(inv);
		}, msg -> {
			setter.accept(msg);
			p.openInventory(inv);
		}, NumberParser.INTEGER_PARSER_POSITIVE).enter();
	}
	
}
