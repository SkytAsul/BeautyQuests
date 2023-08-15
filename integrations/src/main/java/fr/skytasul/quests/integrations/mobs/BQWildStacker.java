package fr.skytasul.quests.integrations.mobs;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import fr.skytasul.quests.api.QuestsAPI;

public class BQWildStacker implements Listener {

	private BQWildStacker() {}

	public static void initialize() {
		QuestsAPI.getAPI().registerMobStacker(entity -> {
			if (entity instanceof LivingEntity && !(entity instanceof Player))
				return WildStackerAPI.getEntityAmount((LivingEntity) entity);
			return 1;
		});
	}

}
