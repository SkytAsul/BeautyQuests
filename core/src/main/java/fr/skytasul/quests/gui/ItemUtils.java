package fr.skytasul.quests.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

import net.md_5.bungee.api.ChatColor;

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
		im.addItemFlags(ItemFlag.values());
		is.setItemMeta(applyMeta(im, name, lore));
		return is;
	}
	
	/**
	 * Create an ItemStack instance from a generic XMaterial
	 * @param type material type
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(XMaterial type, String name, List<String> lore) {
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.values());
		is.setItemMeta(applyMeta(im, name, lore));
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
		if (skull != null) im.setOwner(skull);
		is.setItemMeta(applyMeta(im, name, lore));
		return is;
	}
	
	private static ItemMeta applyMeta(ItemMeta im, String name, Object lore) {
		List<String> editLore = null;
		if (name != null) {
			editLore = Utils.wordWrap(name, 50);
			if (editLore.size() == 0) {
				name = "";
			}else if (editLore.size() == 1) {
				name = editLore.get(0);
				editLore = null;
			}else {
				name = editLore.remove(0);
			}
			im.setDisplayName(name);
		}
		
		if (lore == null) {
			if (editLore != null) im.setLore(getLoreLines(editLore));
		}else {
			if (lore instanceof List) {
				List<String> loreList = (List<String>) lore;
				if (!loreList.isEmpty()) {
					if (editLore != null) {
						editLore.addAll(loreList);
						loreList = editLore;
					}
					im.setLore(getLoreLines(loreList));
				}
			}else {
				String[] loreArray = (String[]) lore;
				if (loreArray.length != 0) {
					if (editLore != null) {
						editLore.addAll(Arrays.asList(loreArray));
						im.setLore(getLoreLines(editLore));
					}else im.setLore(getLoreLines(loreArray));
				}
			}
		}
		return im;
	}

	/**
	 * Set the lore of an item (override old lore)
	 * @param is ItemStack instance to edit
	 * @param lore new lore of the item, formatted as a String array
	 * @return the same ItemStack instance, with the new lore
	 */
	public static ItemStack lore(ItemStack is, String... lore) {
		ItemMeta im = is.getItemMeta();
		im.setLore(getLoreLines(lore));
		is.setItemMeta(im);
		
		return is;
	}
	
	/**
	 * Set the lore of an item (override old lore)
	 * @param is ItemStack instance to edit
	 * @param lore new lore of the item, formatted as a String array
	 * @return the same ItemStack instance, with the new lore
	 */
	public static ItemStack lore(ItemStack is, List<String> lore) {
		ItemMeta im = is.getItemMeta();
		im.setLore(getLoreLines(lore));
		is.setItemMeta(im);
		
		return is;
	}
	
	private static List<String> getLoreLines(String... lore) {
		if (lore != null) {
			List<String> finalLines = new ArrayList<>();
			if (lore != null) {
				for (int i = 0; i < lore.length; i++) {
					String line = lore[i];
					if (line == null) {
						if (i + 1 == lore.length) break; // if last line and null : not shown
						finalLines.add("§a");
					}else finalLines.addAll(Utils.wordWrap(line, 40));
				}
			}
			return finalLines;
		}else return Collections.emptyList();
	}
	
	private static List<String> getLoreLines(List<String> lore) {
		if (lore != null) {
			List<String> finalLines = new ArrayList<>();
			if (lore != null) {
				for (int i = 0; i < lore.size(); i++) {
					String line = lore.get(i);
					if (line == null) {
						if (i + 1 == lore.size()) break; // if last line and null : not shown
						finalLines.add("§a");
					}else finalLines.addAll(Utils.wordWrap(line, 40));
				}
			}
			return finalLines;
		}else return Collections.emptyList();
	}
	
	/**
	 * Add some lore of an ItemStack instance, and keep the old lore
	 * @param is ItemStack instance to edit
	 * @param add lore to add, formatted as a String array
	 * @return the same ItemStack instance, with the new lore added at the end
	 */
	public static ItemStack loreAdd(ItemStack is, String... add){
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) {
			im.setLore(getLoreLines(add));
			is.setItemMeta(im);
			return is;
		}
		List<String> ls = im.getLore();
		ls.addAll(Arrays.asList(add));
		im.setLore(getLoreLines(ls));
		is.setItemMeta(im);
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
		if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return (format) ? MinecraftNames.getMaterialName(is) : XMaterial.matchXMaterial(is).name();
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
	 * @see #itemNextPage
	 */
	public static final ImmutableItemStack itemLaterPage = new ImmutableItemStack(item(QuestsConfiguration.getPageMaterial(), Lang.laterPage.toString()));

	/**
	 * Immutable ItemStack instance with lore : <i>inv.stages.nextPage</i> and material : <i>pageItem</i>
	 * @see #itemLaterPage
	 */
	public static final ImmutableItemStack itemNextPage = new ImmutableItemStack(item(QuestsConfiguration.getPageMaterial(), Lang.nextPage.toString()));

	/**
	 * Immutable ItemStack instance with name : <i>inv.cancel</i> and material : barrier
	 */
	public static final ImmutableItemStack itemCancel = new ImmutableItemStack(item(XMaterial.BARRIER, Lang.cancel.toString()));

	/**
	 * Immutable ItemStack instance with name : <i>inv.done</i> and material : diamond
	 * @see #itemNotDone
	 */
	public static final ImmutableItemStack itemDone = new ImmutableItemStack(addEnchant(item(XMaterial.DIAMOND, Lang.done.toString()), Enchantment.DURABILITY, 0));
	
	/**
	 * Immutable ItemStack instance with name: <i>inv.done</i> but red and strikethrough, material: charcoal
	 * @see #itemDone
	 */
	public static final ImmutableItemStack itemNotDone = new ImmutableItemStack(item(XMaterial.CHARCOAL, "§c§l§m" + ChatColor.stripColor(Lang.done.toString())));
	
	/**
	 * Get a glass pane ItemStack instance with the color wanted
	 * @param color DyeColor wanted
	 * @return ItemStack instance of a Stained Glass Pane
	 */
	public static ItemStack itemSeparator(DyeColor color){
		return item(XMaterial.matchXMaterial(color.name() + "_STAINED_GLASS_PANE").get(), "§7");
	}

	/**
	 * Get a "switch" item : ink sack
	 * @param name name of the item
	 * @param enabled is the switch enabled by default
	 * @param lore lore of the item
	 * @return ItemStack instance of the created switch
	 */
	public static ItemStack itemSwitch(String name, boolean enabled, String... lore){
		return item(XMaterial.requestOldXMaterial("INK_SACK", (byte) (enabled ? 10 : 8)), (enabled ? "§a" : "§7") + name, lore);
	}
	
	/**
	 * Toggle a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @param itemSwitch switch item
	 * @return new state of the switch
	 */
	public static boolean toggle(ItemStack itemSwitch){
		String name = getName(itemSwitch);
		boolean toggled = name.charAt(1) != 'a'; // toggling
		set(itemSwitch, toggled);
		return toggled;
	}
	
	/**
	 * Set the state of a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @see #toggle(ItemStack)
	 * @param itemSwitch switch item
	 * @param enable new state of the switch
	 * @return same state
	 */
	public static ItemStack set(ItemStack itemSwitch, boolean enable) {
		if (itemSwitch == null) return null;
		String name = getName(itemSwitch);
		name(itemSwitch, (enable ? "§a" : "§7") + name.substring(2));
		if (XMaterial.isNewVersion()){
			itemSwitch.setType(enable ? XMaterial.LIME_DYE.parseMaterial() : XMaterial.GRAY_DYE.parseMaterial());
		}else itemSwitch.setDurability((short) (enable ? 10 : 8));
		return itemSwitch;
	}
	
}
