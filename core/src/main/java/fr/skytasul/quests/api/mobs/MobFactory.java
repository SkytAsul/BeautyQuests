package fr.skytasul.quests.api.mobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.utils.compatibility.mobs.CompatMobDeathEvent;
import net.citizensnpcs.npc.ai.NPCHolder;

/**
 * This class implements {@link Listener} to permit the implementation to have at least one {@link EventHandler}.
 * This event method will be used to fire {@link #callEvent(Object, Entity, Player)}.
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
		return Collections.EMPTY_LIST;
	}

	/**
	 * Has to be called when a mob corresponding to this factory has been killed
	 * @param data mob killed
	 * @param entity bukkit entity killed
	 * @param p killer
	 */
	public default void callEvent(T data, Entity entity, Player p) {
		if (p instanceof NPCHolder) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(data, p, entity));
	}

	public static final List<MobFactory<?>> factories = new ArrayList<>();

	public static MobFactory<?> getMobFactory(String id) {
		for (MobFactory<?> factory : factories) {
			if (factory.getID().equals(id)) return factory;
		}
		return null;
	}

}
