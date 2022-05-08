package fr.skytasul.quests.players;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.nms.NMS;

public class AdminMode {

	private static final Set<CommandSender> senders = new HashSet<>();
	
	private static ParticleEffect enterParticle = new ParticleEffect(Particle.FLAME, null, null);
	private static ParticleEffect leaveParticle = new ParticleEffect(Particle.SMOKE_NORMAL, null, null);
	
	public static void toggle(CommandSender sender){
		if (senders.add(sender)) {
			Lang.ADMIN_MODE_ENTERED.send(sender);
			if (sender instanceof Player && NMS.getMCVersion() >= 9)
				enterParticle.sendParticle(((Player) sender).getEyeLocation(), getAdminPlayers(), 1, 1, 1, 15);
		}else {
			senders.remove(sender);
			Lang.ADMIN_MODE_LEFT.send(sender);
			if (sender instanceof Player && NMS.getMCVersion() >= 9 && senders.stream().anyMatch(Player.class::isInstance))
				leaveParticle.sendParticle(((Player) sender).getEyeLocation(), getAdminPlayers(), 1, 1, 1, 15);
		}
	}
	
	public static void broadcast(String message){
		BeautyQuests.getInstance().getLoggerHandler().write("[ADMIN]: " + message);
		for (CommandSender p : senders){
			p.sendMessage("§e" + message);
		}
	}
	
	public static Set<CommandSender> getAdminSenders() {
		return senders;
	}
	
	public static List<Player> getAdminPlayers(){
		return senders.stream().filter(Player.class::isInstance).map(Player.class::cast).collect(Collectors.toList());
	}
	
}
