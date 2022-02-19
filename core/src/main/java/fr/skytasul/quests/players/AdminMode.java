package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.nms.NMS;

public class AdminMode {

	private static final List<CommandSender> senders = new ArrayList<>();
	
	public static void toggle(CommandSender sender){
		if (senders.contains(sender)){
			senders.remove(sender);
			Lang.ADMIN_MODE_LEFT.send(sender);
			if (sender instanceof Player && NMS.isValid() && senders.stream().anyMatch((x) -> x instanceof Player)) ParticleEffect.SMOKE_NORMAL.display(1, 1, 1, 0.1, 15, ((Player) sender).getEyeLocation(), getAdminPlayers());
		}else {
			senders.add(sender);
			Lang.ADMIN_MODE_ENTERED.send(sender);
			if (sender instanceof Player && NMS.isValid()) ParticleEffect.FLAME.display(1, 1, 1, 0.1, 15, ((Player) sender).getEyeLocation(), getAdminPlayers());
		}
	}
	
	public static void broadcast(String message){
		BeautyQuests.logger.write("[ADMIN]: " + message);
		for (CommandSender p : senders){
			p.sendMessage("§e" + message);
		}
	}
	
	public static List<CommandSender> getAdminSenders(){
		return senders;
	}
	
	public static List<Player> getAdminPlayers(){
		return senders.stream().filter((send) -> send instanceof Player).map((player) -> (Player) player).collect(Collectors.toList());
	}
	
}
