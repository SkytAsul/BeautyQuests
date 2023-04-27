package fr.skytasul.quests.gui.misc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.utils.types.Title;

public class TitleGUI extends CustomInventory {
	
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
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 9, Lang.INVENTORY_EDIT_TITLE.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(SLOT_TITLE, ItemUtils.item(XMaterial.NAME_TAG, Lang.title_title.toString()));
		inventory.setItem(SLOT_SUBTITLE, ItemUtils.item(XMaterial.NAME_TAG, Lang.title_subtitle.toString()));
		inventory.setItem(SLOT_FADE_IN, ItemUtils.item(XMaterial.CLOCK, Lang.title_fadeIn.toString()));
		inventory.setItem(SLOT_STAY, ItemUtils.item(XMaterial.CLOCK, Lang.title_stay.toString()));
		inventory.setItem(SLOT_FADE_OUT, ItemUtils.item(XMaterial.CLOCK, Lang.title_fadeOut.toString()));
		
		inventory.setItem(7, ItemUtils.itemCancel);
		inventory.setItem(8, ItemUtils.itemDone.toMutableStack());
		
		// updating lores
		setTitle(title);
		setSubtitle(subtitle);
		setFadeIn(fadeIn);
		setStay(stay);
		setFadeOut(fadeOut);
		updateFinishState();
	}
	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case SLOT_TITLE:
			startStringEditor(player, Lang.TITLE_TITLE.toString(), this::setTitle);
			break;
		case SLOT_SUBTITLE:
			startStringEditor(player, Lang.TITLE_SUBTITLE.toString(), this::setSubtitle);
			break;
		case SLOT_FADE_IN:
			startIntEditor(player, Lang.TITLE_FADEIN.toString(), this::setFadeIn);
			break;
		case SLOT_STAY:
			startIntEditor(player, Lang.TITLE_STAY.toString(), this::setStay);
			break;
		case SLOT_FADE_OUT:
			startIntEditor(player, Lang.TITLE_FADEOUT.toString(), this::setFadeOut);
			break;
		case 7:
			close(player);
			end.accept(null);
			break;
		case 8:
			close(player);
			end.accept(new Title(title, subtitle, fadeIn, stay, fadeOut));
			break;
		default:
			break;
		}
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(() -> end.accept(null));
	}

	private void startStringEditor(Player p, String helpMsg, Consumer<String> setter) {
		MessageUtils.sendPrefixedMessage(p, helpMsg);
		new TextEditor<String>(p, () -> {
			p.openInventory(inv);
		}, msg -> {
			setter.accept(msg);
			updateFinishState();
			p.openInventory(inv);
		}).passNullIntoEndConsumer().start();
	}
	
	private void startIntEditor(Player p, String helpMsg, Consumer<Integer> setter) {
		MessageUtils.sendPrefixedMessage(p, helpMsg);
		new TextEditor<>(p, () -> {
			p.openInventory(inv);
		}, msg -> {
			setter.accept(msg);
			p.openInventory(inv);
		}, NumberParser.INTEGER_PARSER_POSITIVE).start();
	}
	
}
