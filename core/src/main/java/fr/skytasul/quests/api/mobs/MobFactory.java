package fr.skytasul.quests.api.mobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.utils.compatibility.mobs.CompatMobDeathEvent;

/**
 * This class implements {@link Listener} to permit the implementation to have at least one {@link EventHandler}.
 * This event method will be used to fire the {@link #callEvent(Event, Object, Entity, Player)}.
 * 
 * @param <T> object which should represents a mob type from whatever plugin
 */
public abstract interface MobFactory<T> extends Listener {

	/**
	 * @return internal ID of this Mob Factory
	 */
	public abstract String getID();

	/**
	 * @return item which will represent this Mob Factory in the Mobs Create GUI
	 */
	public abstract ItemStack getFactoryItem();

	/**
	 * Called when a player click on the {@link #getFactoryItem()}
	 * @param p Player who clicked on the item
	 * @param run
	 */
	public abstract void itemClick(Player p, Consumer<T> run);

	/**
	 * @param value value returned from {@link #getValue(Object)}
	 * @return object created with the value
	 */
	public abstract T fromValue(String value);

	/**
	 * @param data object to get a String value from
	 * @return String value
	 */
	public abstract String getValue(T data);

	/**
	 * @param data object to get a name from
	 * @return name of the object
	 */
	public abstract String getName(T data);

	/**
	 * @param data object to get the entity type from
	 * @return entity type of the object
	 */
	public abstract EntityType getEntityType(T data);

	/**
	 * @param data object to get a description from
	 * @return list of string which will be displayed as the lore of the mob item
	 */
	public default List<String> getDescriptiveLore(T data) {
		return Collections.emptyList();
	}

	public default boolean mobApplies(T first, Object other) {
		return Objects.equals(first, other);
	}
	
	public default boolean bukkitMobApplies(T first, Entity entity) { // TODO abstract (introduced in 0.20)
		BeautyQuests.logger.warning("The mob factory " + getID() + " has not been updated. Nag its author about it!");
		return false;
	}

	/**
	 * Has to be called when a mob corresponding to this factory has been killed
	 * 
	 * @param originalEvent original event
	 * @param pluginMob mob killed
	 * @param entity bukkit entity killed
	 * @param player killer
	 */
	public default void callEvent(Event originalEvent, T pluginMob, Entity entity, Player player) {
		Validate.notNull(pluginMob, "Plugin mob object cannot be null");
		Validate.notNull(player, "Player cannot be null");
		if (originalEvent != null) {
			CompatMobDeathEvent existingCompat = eventsCache.getIfPresent(originalEvent);
			if (existingCompat != null && mobApplies(pluginMob, existingCompat.getPluginMob())) {
				BeautyQuests.logger.warning("MobFactory.callEvent() called twice!");
				return;
			}
		}

		OptionalInt optionalStackSize = QuestsAPI.getMobStackers().stream()
				.mapToInt(stacker -> stacker.getEntityStackSize(entity)).filter(size -> size > 1).max();

		CompatMobDeathEvent compatEvent = new CompatMobDeathEvent(pluginMob, player, entity, optionalStackSize.orElse(1));
		if (originalEvent != null) eventsCache.put(originalEvent, compatEvent);
		Bukkit.getPluginManager().callEvent(compatEvent);
	}

	static final Cache<Event, CompatMobDeathEvent> eventsCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build();
	public static final List<MobFactory<?>> factories = new ArrayList<>();

	public static MobFactory<?> getMobFactory(String id) {
		for (MobFactory<?> factory : factories) {
			if (factory.getID().equals(id)) return factory;
		}
		return null;
	}

}
