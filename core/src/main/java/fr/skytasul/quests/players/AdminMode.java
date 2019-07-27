package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.nms.NMS;

public class AdminMode {

	private static List<Player> players = new ArrayList<>();
	
	public static void toggle(Player p){
		if (players.contains(p)){
			players.remove(p);
			Lang.ADMIN_MODE_LEFT.send(p);
			if (NMS.isValid() && !players.isEmpty()) ParticleEffect.SMOKE_NORMAL.display(1, 1, 1, 0.1, 15, p.getEyeLocation(), players);
		}else {
			players.add(p);
			Lang.ADMIN_MODE_ENTERED.send(p);
			if (NMS.isValid()) ParticleEffect.FLAME.display(1, 1, 1, 0.1, 15, p.getEyeLocation(), players);
		}
	}
	
	public static void broadcast(String message){
		BeautyQuests.logger.write("[LOG]: " + message);
		for (Player p : players){
			if (p.isOnline()) p.sendMessage(message);
		}
	}
	
}
