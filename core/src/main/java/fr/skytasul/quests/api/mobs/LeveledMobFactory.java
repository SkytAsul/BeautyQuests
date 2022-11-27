package fr.skytasul.quests.api.mobs;

import org.bukkit.entity.Entity;

public interface LeveledMobFactory<T> extends MobFactory<T> {

	double getMobLevel(T type, Entity entity);

}
