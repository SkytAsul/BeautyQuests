package fr.skytasul.quests.api.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ImmutableItemStack extends ItemStack {

	private ItemStack realItemStack;
	
	public ImmutableItemStack(ItemStack item){
		super(item);
	}
	
	@Override
	public void setAmount(int amount){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public void setData(MaterialData data){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public void setDurability(short durability){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public boolean setItemMeta(ItemMeta itemMeta){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public void setType(Material type){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public void addEnchantment(Enchantment ench, int level){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public void addUnsafeEnchantment(Enchantment ench, int level){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public int removeEnchantment(Enchantment ench){
		throw new UnsupportedOperationException("This ItemStack instance is immutable");
	}
	
	@Override
	public boolean isSimilar(ItemStack stack) {
		if (realItemStack == null) realItemStack = toMutableStack();
		return realItemStack.isSimilar(stack);
	}
	
	@Override
	public ItemStack clone() {
		return new ItemStack(this);
	}
	
	public ItemStack toMutableStack() {
		return new ItemStack(this);
	}
	
	public ItemStack toMutableStack(int amount) {
		ItemStack item = new ItemStack(this);
		item.setAmount(amount);
		return item;
	}

}
