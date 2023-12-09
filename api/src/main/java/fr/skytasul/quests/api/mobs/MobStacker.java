package fr.skytasul.quests.api.mobs;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface MobStacker {

	int getEntityStackSize(@NotNull Entity entity);

}
