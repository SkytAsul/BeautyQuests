package fr.skytasul.quests.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;

public class ItemUtils {
	
	/**
	 * Create an ItemStack instance from a generic XMaterial
	 * @param type material type
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(XMaterial type, String name, String... lore) {
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.addItemFlags(ItemFlag.values());
		is.setItemMeta(im);
		if (lore != null && lore.length != 0) lore(is, lore);
		return is;
	}
	
	/**
	 * Create an ItemStack instance of a skull item
	 * @param name name of the item
	 * @param skull skull's owner name
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack skull(String name, String skull, String... lore) {
		ItemStack is = XMaterial.playerSkullItem();
		SkullMeta im = (SkullMeta) is.getItemMeta();
		im.setDisplayName(name);
		im.setOwner(skull);
		is.setItemMeta(im);
		if (lore != null && lore.length != 0) lore(is, lore);
		return is;
	}

	/**
	 * Set the lore of an item (override old lore)
	 * @param is ItemStack instance to edit
	 * @param lore new lore of the item, formatted as a String array
	 * @return the same ItemStack instance, with the new lore
	 */
	public static ItemStack lore(ItemStack is, String... lore) {
		ItemMeta im = is.getItemMeta();
		List<String> ls = new ArrayList<>();
		if (lore != null && lore.length != 0){
			for (String s : lore){
				if (s == null) {
					ls.add("§a");
					continue;
				}
				List<String> lss = new ArrayList<>();
				for (String as : StringUtils.splitByWholeSeparator(s, "\\n")){
					lss.add(as);
				}
				String last = "";
				for (String ss : lss){
					ss = last + ss;
					int i = ss.lastIndexOf("§");
					if (i != -1) last = ss.charAt(i) + "" + ss.charAt(i + 1);
					ls.add(ss);
				}
			}
		}
		im.setLore(ls);
		is.setItemMeta(im);
		
		return is;
	}
	
	/**
	 * Add some lore of an ItemStack instance, and keep the old lore
	 * @param is ItemStack instance to edit
	 * @param add lore to add, formatted as a String array
	 * @return the same ItemStack instance, with the new lore added at the end
	 */
	public static ItemStack loreAdd(ItemStack is, String... add){
		if (!is.getItemMeta().hasLore()){
			lore(is, add);
			return is;
		}
		List<String> ls = is.getItemMeta().getLore();
		ls.addAll(Arrays.asList(add));
		lore(is, ls.toArray(new String[0]));
		return is;
	}

	public static String[] getLore(ItemStack is) {
		if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) return null;
		return is.getItemMeta().getLore().toArray(new String[0]);
	}

	/**
	 * Change the owner of an skull ItemStack
	 * @param is skull ItemStack instance
	 * @param ownerName new owner name
	 * @return same ItemStack instance, with skull's owner changed
	 */
	public static ItemStack owner(ItemStack is, String ownerName) {
		Validate.isTrue(is.getItemMeta() instanceof SkullMeta, "ItemStack must be a Skull");
		SkullMeta im = (SkullMeta) is.getItemMeta();
		im.setOwner(ownerName);
		is.setItemMeta(im);
		return is;
	}

	public static String getOwner(ItemStack is) {
		if (!is.getType().name().equals("SKULL_ITEM") && !is.getType().name().equals("PLAYER_HEAD")) return null;
		return ((SkullMeta) is.getItemMeta()).getOwner();
	}

	/**
	 * Change the name of an ItemStack instance
	 * @param is ItemStack instance to edit
	 * @param name new name of the item
	 * @return same ItemStack instance with the new name
	 */
	public static ItemStack name(ItemStack is, String name) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}
	
	/**
	 * Add a string at the end of the name of an ItemStack instance
	 * @param is ItemStack instance to edit
	 * @param add String to add at the end of existant name
	 * @return same ItemStack instance with edited name
	 */
	public static ItemStack nameAdd(ItemStack is, String add) {
		return name(is, getName(is) + add);
	}
	
	public static String getName(ItemStack is){
		return getName(is, false);
	}
	
	/**
	 * Get the name of an ItemStack (if no custom name, then it will return the material name)
	 * @param is ItemStack instance
	 * @param format true if the material name should be translated using the Vanilla Translations System
	 * @return the name of the ItemStack
	 */
	public static String getName(ItemStack is, boolean format) {
		if (is == null) return null;
		if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return (format) ? MinecraftNames.getMaterialName(XMaterial.fromItemStack(is)) : XMaterial.fromMaterial(is.getType()).getLastName();
		return is.getItemMeta().getDisplayName();
	}
	
	public static boolean hasEnchant(ItemStack is, Enchantment en){
		return is.getItemMeta().hasEnchant(en);
	}
	
	public static ItemStack addEnchant(ItemStack is, Enchantment en, int level){
		ItemMeta im = is.getItemMeta();
		im.addEnchant(en, level, true);
		is.setItemMeta(im);
		return is;
	}
	
	public static ItemStack removeEnchant(ItemStack is, Enchantment en){
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(en);
		is.setItemMeta(im);
		return is;
	}
	

	/**
	 * Immutable ItemStack instance with lore : <i>inv.stages.laterPage</i> and material : <i>pageItem</i>
	 */
	public static ImmutableItemStack itemLaterPage = new ImmutableItemStack(item(QuestsConfiguration.getPageMaterial(), Lang.laterPage.toString()));

	/**
	 * Immutable ItemStack instance with lore : <i>inv.stages.nextPage</i> and material : <i>pageItem</i>
	 */
	public static ImmutableItemStack itemNextPage = new ImmutableItemStack(item(QuestsConfiguration.getPageMaterial(), Lang.nextPage.toString()));

	/**
	 * Immutable ItemStack instance with name : <i>inv.cancel</i> and material : barrier
	 */
	public static ImmutableItemStack itemCancel = new ImmutableItemStack(item(XMaterial.BARRIER, Lang.cancel.toString()));

	/**
	 * Immutable ItemStack instance with name : <i>inv.done</i> and material : diamond
	 */
	public static ImmutableItemStack itemDone = new ImmutableItemStack(item(XMaterial.DIAMOND, Lang.done.toString()));
	
	/**
	 * Get a glass pane ItemStack instance with the color wanted
	 * @param color DyeColor wanted
	 * @return ItemStack instance of a Stained Glass Pane
	 */
	public static ItemStack itemSeparator(DyeColor color){
		return item(XMaterial.requestXMaterial("STAINED_GLASS_PANE", (byte) color.ordinal()), "§7");
	}

	/**
	 * Get a "switch" item : ink sack
	 * @param name name of the item
	 * @param enabled is the switch enabled by default
	 * @param lore lore of the item
	 * @return ItemStack instance of the created switch
	 */
	public static ItemStack itemSwitch(String name, boolean enabled, String... lore){
		return item(XMaterial.requestXMaterial("INK_SACK", (byte) (enabled ? 10 : 8)), (enabled ? "§a" : "§7") + name, lore);
	}
	
	/**
	 * Toggle a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @param itemSwitch switch item
	 * @return new state of the switch
	 */
	public static boolean toggle(ItemStack itemSwitch){
		String name = getName(itemSwitch);
		boolean toggled = name.charAt(1) != 'a'; // toggling
		return set(itemSwitch, toggled);
	}
	
	/**
	 * Set the state of a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @see #toggle(ItemStack)
	 * @param itemSwitch switch item
	 * @param enable new state of the switch
	 * @return same state
	 */
	public static boolean set(ItemStack itemSwitch, boolean enable){
		if (itemSwitch == null) return enable;
		String name = getName(itemSwitch);
		name(itemSwitch, (enable ? "§a" : "§7") + name.substring(2));
		if (XMaterial.isNewVersion()){
			itemSwitch.setType(enable ? XMaterial.LIME_DYE.parseMaterial() : XMaterial.GRAY_DYE.parseMaterial());
		}else itemSwitch.setDurability((short) (enable ? 10 : 8));
		return enable;
	}
	
}
