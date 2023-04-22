package fr.skytasul.quests.api.mobs;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface LeveledMobFactory<T> extends MobFactory<T> {

	double getMobLevel(@NotNull T type, @NotNull Entity entity);

}
