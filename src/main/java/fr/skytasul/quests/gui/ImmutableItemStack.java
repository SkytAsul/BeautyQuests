package fr.skytasul.quests.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ImmutableItemStack extends ItemStack {

	public ImmutableItemStack(ItemStack item){
		super(item);
	}
	
	public void setAmount(int amount){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public void setData(MaterialData data){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public void setDurability(short durability){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public boolean setItemMeta(ItemMeta itemMeta){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public void setType(Material type){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public void addEnchantment(Enchantment ench, int level){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public void addUnsafeEnchantment(Enchantment ench, int level){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	public int removeEnchantment(Enchantment ench){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
}
