package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Vault {

	private static Economy eco;
	private static Permission vperm = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class).getProvider();
	
	static {
		RegisteredServiceProvider<Economy> ecoReg = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (ecoReg != null) eco = ecoReg.getProvider();
		RegisteredServiceProvider<Permission> permReg = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permReg != null) vperm = permReg.getProvider();
	}
	
	public static Economy getEconomy(){
		return eco;
	}
	
	public static Permission getVaultPermission(){
		return vperm;
	}

	public static void depositPlayer(Player p, int money){
		if (eco != null) eco.depositPlayer(p, money);
	}

	public static void withdrawPlayer(Player p, int money){
		if (eco != null) eco.withdrawPlayer(p, money);
	}
	
	public static void changePermission(Player p, String perm, boolean remove){
		if (vperm == null) return;
		for (World world : Bukkit.getWorlds()){
			boolean has = vperm.has(world.getName(), p.getName(), perm);
			if (remove) {
				if (has) vperm.playerRemove(world.getName(), p, perm);
			}else if (!has) vperm.playerAdd(world.getName(), p, perm);
		}
	}
	
	public static String currencyNameSingular(){
		return eco.currencyNameSingular();
	}
	
	public static String currencyNamePlural(){
		return eco.currencyNamePlural();
	}
	
}
