package fr.skytasul.quests.api.gui;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ItemUtils {

	private ItemUtils() {}

	private static final int LORE_LINE_LENGTH = 40;
	private static final int LORE_LINE_LENGTH_CRITICAL = 1000;

	/**
	 * Create an ItemStack instance from a generic XMaterial
	 * @param type material type
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(XMaterial type, String name, String... lore) {
		if (!type.isSupported()) {
			QuestsPlugin.getPlugin().getLogger()
					.warning("Trying to create an item for an unsupported material " + type.name());
			type = XMaterial.SPONGE;
		}
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		addSpecificFlags(im, is.getType());
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
		if (!type.isSupported()) {
			QuestsPlugin.getPlugin().getLogger()
					.warning("Trying to create an item for an unsupported material " + type.name());
			type = XMaterial.SPONGE;
		}
		ItemStack is = type.parseItem();
		ItemMeta im = is.getItemMeta();
		addSpecificFlags(im, is.getType());
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
		ItemStack is = XMaterial.PLAYER_HEAD.parseItem();
		SkullMeta im = (SkullMeta) is.getItemMeta();
		if (skull != null) im.setOwner(skull);
		is.setItemMeta(applyMeta(im, name, lore));
		return is;
	}

	private static ItemMeta applyMeta(ItemMeta im, String name, Object lore) {
		List<String> editLore = null;
		if (name != null) {
			editLore = ChatColorUtils.wordWrap(name, LORE_LINE_LENGTH, LORE_LINE_LENGTH_CRITICAL);
			if (editLore.isEmpty()) {
				name = "";
				editLore = null;
			}else if (editLore.size() == 1) {
				name = editLore.get(0);
				editLore = null;
			}else {
				name = editLore.remove(0);
			}
			im.setDisplayName(name);
		}

		if (lore instanceof List) {
			List<String> loreList = (List<String>) lore;
			if (!loreList.isEmpty()) {
				if (editLore != null) {
					editLore.addAll(loreList);
					loreList = editLore;
				}
				im.setLore(getLoreLines(loreList));
				return im;
			}
		}else if (lore instanceof String[]) {
			String[] loreArray = (String[]) lore;
			if (loreArray.length != 0) {
				if (editLore != null) {
					editLore.addAll(Arrays.asList(loreArray));
					im.setLore(getLoreLines(editLore));
				}else im.setLore(getLoreLines(loreArray));
				return im;
			}
		}
		if (editLore != null) im.setLore(getLoreLines(editLore));
		return im;
	}

	public static ItemStack nameAndLore(ItemStack is, String name, String... lore) {
		is.setItemMeta(applyMeta(is.getItemMeta(), name, lore));
		return is;
	}

	public static ItemStack nameAndLore(ItemStack is, String name, List<String> lore) {
		is.setItemMeta(applyMeta(is.getItemMeta(), name, lore));
		return is;
	}

	public static ItemStack clearVisibleAttributes(ItemStack is) {
		ItemMeta im = is.getItemMeta();

		// remove name and lore
		im.setDisplayName(null);
		im.setLore(null);

		addSpecificFlags(im, is.getType());

		is.setItemMeta(im);
		return is;
	}

	public static ItemMeta addSpecificFlags(ItemMeta im, Material material) {
		// add flags to hide various descriptions,
		// depending on the item type/attributes/other things
		if (im.hasEnchants())
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		if (MinecraftVersion.MAJOR >= 11 && im.isUnbreakable())
			im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if (material.getMaxDurability() != 0 || (MinecraftVersion.MAJOR > 12 && im.hasAttributeModifiers()))
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		if (im instanceof BookMeta || im instanceof PotionMeta || im instanceof EnchantmentStorageMeta
				|| (MinecraftVersion.MAJOR >= 12 && im instanceof KnowledgeBookMeta))
			im.addItemFlags(Utils.valueOfEnum(ItemFlag.class, "HIDE_POTION_EFFECTS", "HIDE_ADDITIONAL_TOOLTIP"));
		if (im instanceof LeatherArmorMeta)
			im.addItemFlags(ItemFlag.HIDE_DYE);

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

	public static ItemStack loreOptionValue(ItemStack is, @Nullable Object value) {
		return lore(is, QuestOption.formatNullableValue(value));
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
					}else finalLines.addAll(ChatColorUtils.wordWrap(line, LORE_LINE_LENGTH, LORE_LINE_LENGTH_CRITICAL));
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
					}else finalLines.addAll(ChatColorUtils.wordWrap(line, LORE_LINE_LENGTH, LORE_LINE_LENGTH_CRITICAL));
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
		if (im.addEnchant(en, level, true))
			is.setItemMeta(im);
		return is;
	}

	public static ItemStack removeEnchant(ItemStack is, Enchantment en){
		ItemMeta im = is.getItemMeta();
		if (im.removeEnchant(en))
			is.setItemMeta(im);
		return is;
	}

	public static boolean isGlittering(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		return (MinecraftVersion.isHigherThan(20, 6) && im.hasEnchantmentGlintOverride() && im.getEnchantmentGlintOverride())
				|| im.hasEnchants();
	}

	public static void setGlittering(ItemStack is, boolean glitter) {
		ItemMeta im = is.getItemMeta();
		if (MinecraftVersion.isHigherThan(20, 6)) {
			im.setEnchantmentGlintOverride(glitter ? Boolean.TRUE : null);
		} else {
			if (glitter)
				im.addEnchant(Enchantment.getByName("DURABILITY"), 0, true);
			else
				im.removeEnchant(Enchantment.getByName("DURABILITY"));
		}
		is.setItemMeta(im);
	}

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
		return item(enabled ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE, (enabled ? "§a" : "§7") + name, lore);
	}

	/**
	 * Toggle a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @param itemSwitch switch item
	 * @return new state of the switch
	 */
	public static boolean toggleSwitch(ItemStack itemSwitch){
		String name = getName(itemSwitch);
		boolean toggled = name.charAt(1) != 'a'; // toggling
		setSwitch(itemSwitch, toggled);
		return toggled;
	}

	/**
	 * Set the state of a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @see #toggleSwitch(ItemStack)
	 * @param itemSwitch switch item
	 * @param enable new state of the switch
	 * @return same state
	 */
	public static ItemStack setSwitch(ItemStack itemSwitch, boolean enable) {
		if (itemSwitch == null) return null;
		String name = getName(itemSwitch);
		name(itemSwitch, (enable ? "§a" : "§7") + name.substring(2));
		if (MinecraftVersion.MAJOR >= 13) {
			itemSwitch.setType(enable ? XMaterial.LIME_DYE.parseMaterial() : XMaterial.GRAY_DYE.parseMaterial());
		}else itemSwitch.setDurability((short) (enable ? 10 : 8));
		return itemSwitch;
	}

}
