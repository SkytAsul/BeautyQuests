package fr.skytasul.quests.gui;

import static fr.skytasul.quests.api.gui.ItemUtils.addEnchant;
import static fr.skytasul.quests.api.gui.ItemUtils.item;
import static fr.skytasul.quests.api.gui.ItemUtils.name;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemFactory;
import fr.skytasul.quests.api.localization.Lang;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DefaultItemFactory implements ItemFactory {

	@Override
	public @NotNull ItemStack getPreviousPage() {
		return name(QuestsConfiguration.getConfig().getGuiConfig().getPreviousPageItem().clone(),
				Lang.laterPage.toString());
	}

	@Override
	public @NotNull ItemStack getNextPage() {
		return name(QuestsConfiguration.getConfig().getGuiConfig().getNextPageItem().clone(),
				Lang.nextPage.toString());
	}

	@Override
	public @NotNull ItemStack getCancel() {
		return item(XMaterial.BARRIER, Lang.cancel.toString());
	}

	@Override
	public @NotNull ItemStack getDone() {
		return addEnchant(item(XMaterial.DIAMOND, Lang.done.toString()), XEnchantment.UNBREAKING.getEnchant(), 0);
	}

	@Override
	public @NotNull ItemStack getNotDone() {
		return item(XMaterial.CHARCOAL, "§c§l§m" + ChatColor.stripColor(Lang.done.toString()));
	}

	@Override
	public @NotNull ItemStack getNone() {
		return item(XMaterial.BARRIER, "§cNone");
	}

}
