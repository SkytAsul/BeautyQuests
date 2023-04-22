package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
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

	public static void depositPlayer(Player p, double money) {
		if (eco != null) eco.depositPlayer(p, money);
	}

	public static void withdrawPlayer(Player p, double money) {
		if (eco != null) eco.withdrawPlayer(p, money);
	}
	
	public static boolean has(Player p, double money) {
		if (eco == null) return false;
		return eco.has(p, money);
	}

	public static String format(double money) {
		if (eco == null) return "" + money;
		return eco.format(money);
	}

	public static void changePermission(Player p, String perm, boolean remove, String world) {
		//boolean has = vperm.playerHas(world, p, perm);
		if (remove) {
			/*if (has) */vperm.playerRemove(world, p, perm);
		}else /*if (!has)*/ vperm.playerAdd(world, p, perm);
	}

	public static void changeGroup(Player p, String group, boolean remove, String world) {
		//boolean has = vperm.playerInGroup(world, p, group);
		if (remove) {
			/*if (has) */vperm.playerRemoveGroup(world, p, group);
		}else /*if (!has)*/ vperm.playerAddGroup(world, p, group);
	}
	
}
