package fr.skytasul.quests.integrations.mobs;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import fr.skytasul.quests.api.QuestsAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BQWildStacker {

	private BQWildStacker() {}

	public static void initialize() {
		QuestsAPI.getAPI().registerMobStacker(entity -> {
			if (entity instanceof LivingEntity && !(entity instanceof Player))
				return WildStackerAPI.getEntityAmount((LivingEntity) entity);
			return 1;
		});
	}

}
