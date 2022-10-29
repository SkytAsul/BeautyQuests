package fr.skytasul.quests.api.stages.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.stages.types.Locatable.MultipleLocatable;
import fr.skytasul.quests.utils.DebugUtils;

/**
 * This interface indicates that an object can provide some locations on demand.
 * <p>
 * Valid subinterfaces are {@link PreciseLocatable} and {@link MultipleLocatable}.
 * A class must not directly implement this interface but one of those.
 * <p>
 * Classes implementing one of the subinterfaces are also supposed to have the
 * {@link LocatedType} annotation to indicate which types of located objects can be
 * retrieved. If no annotation is attached, it will be assumed that all kinds of
 * located objects can be retrieved.
 */
public interface Locatable {
	
	/**
	 * If no indication should be displayed to the <code>player</code>,
	 * then this method should return <code>false</code>.
	 * 
	 * @param player Player to test for indications
	 * @return	<code>true</code> if location indications should be displayed
	 * 			to the player, <code>false</code> otherwise.
	 */
	default boolean isShown(Player player) {
		return true;
	}
	
	/**
	 * Indicates if the Located instances gotten from {@link PreciseLocatable#getLocated()}
	 * and {@link MultipleLocatable#getNearbyLocated(MultipleLocatable.NearbyFetcher)}
	 * can be safely retrieved from an asynchronous thread.
	 * 
	 * @return	<code>true</code> <b>only if</b> the Located fetch operations can
	 * 			be done asynchronously, <code>false</code> otherwise.
	 */
	default boolean canBeFetchedAsynchronously() {
		return true;
	}
	
	/**
	 * This interface indicates that an object can provide a unique and precise location,
	 * no matter the player.
	 */
	interface PreciseLocatable extends Locatable {
		
		/**
		 * Gets the uniquely located object.
		 * <p>
		 * The result should be consistent, which means that calling it twice without
		 * having something else changed in the game state would return the same value.
		 * @return the located object
		 */
		Located getLocated();
		
	}
	
	/**
	 * This interface indicates that an object can provide multiple locations depending on
	 * factors such as a center and a maximum distance, detailed in {@link NearbyFetcher}.
	 */
	interface MultipleLocatable extends Locatable {
		
		/**
		 * Gets a {@link Spliterator} of all targets in the region specified by
		 * the {@link NearbyFetcher} parameter.
		 * @param fetcher describes the region from where the targets must be found
		 * @return a Spliterator which allows iterating through the targets
		 */
		Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher);
		
		/**
		 * This POJO contains informations on the region from where
		 * the {@link MultipleLocatable} object has to find its targets.
		 */
		interface NearbyFetcher {
			
			Location getCenter();
			
			double getMaxDistance();
			
			default double getMaxDistanceSquared() {
				return getMaxDistance() * getMaxDistance();
			}
			
			default boolean isTargeting(LocatedType type) {
				return true;
			}
			
			static NearbyFetcher create(Location location, double maxDistance) {
				return new NearbyFetcherImpl(location, maxDistance, null);
			}
			
			static NearbyFetcher create(Location location, double maxDistance, LocatedType targetType) {
				return new NearbyFetcherImpl(location, maxDistance, targetType);
			}
			
			class NearbyFetcherImpl implements NearbyFetcher {
				private final Location center;
				private final double maxDistance;
				private final LocatedType targetType;
				
				public NearbyFetcherImpl(Location center, double maxDistance, LocatedType targetType) {
					this.center = center;
					this.maxDistance = maxDistance;
					this.targetType = targetType;
				}
				
				@Override
				public Location getCenter() {
					return center;
				}
				
				@Override
				public double getMaxDistance() {
					return maxDistance;
				}
				
				@Override
				public boolean isTargeting(LocatedType type) {
					return targetType == null || targetType == type;
				}
				
			}
			
		}
		
	}
	
	/**
	 * This annotation indicates which types of {@link Located} objects can be retrieved from the attached class.
	 * <p>
	 * It should only be attached to {@link Locatable} implementing classes.
	 */
	@Retention (RetentionPolicy.RUNTIME)
	@Target (ElementType.TYPE)
	@interface LocatableType {
		/**
		 * @return	an array of {@link LocatedType} that can be retrieved
		 * 			from the class attached to this annotation.
		 */
		LocatedType[] types() default { LocatedType.ENTITY, LocatedType.BLOCK, LocatedType.OTHER };
	}
	
	/**
	 * Represents something that is locatable on a world.
	 */
	interface Located {
		
		Location getLocation();
		
		LocatedType getType();
		
		static Located create(Location location) {
			return new LocatedImpl(location);
		}
		
		class LocatedImpl implements Located {
			protected Location location;
			
			public LocatedImpl(Location location) {
				this.location = location;
			}
			
			@Override
			public Location getLocation() {
				return location.clone();
			}
			
			@Override
			public LocatedType getType() {
				return LocatedType.OTHER;
			}
			
			@Override
			public int hashCode() {
				return location.hashCode();
			}
			
			@Override
			public boolean equals(Object obj) {
				if (!(obj instanceof LocatedImpl)) return false;
				LocatedImpl other = (LocatedImpl) obj;
				return other.location.equals(location);
			}
			
		}
		
		interface LocatedEntity extends Located {
			
			Entity getEntity();
			
			@Override
			default Location getLocation() {
				Entity entity = getEntity();
				return entity == null ? null : entity.getLocation();
			}
			
			@Override
			default LocatedType getType() {
				return LocatedType.ENTITY;
			}
			
			static LocatedEntity create(Entity entity) {
				return new LocatedEntityImpl(entity);
			}
			
			class LocatedEntityImpl implements LocatedEntity {
				private Entity entity;
				
				public LocatedEntityImpl(Entity entity) {
					this.entity = entity;
				}
				
				@Override
				public Entity getEntity() {
					return entity;
				}
				
				@Override
				public int hashCode() {
					return entity.hashCode();
				}
				
				@Override
				public boolean equals(Object obj) {
					if (!(obj instanceof LocatedEntityImpl)) return false;
					LocatedEntityImpl other = (LocatedEntityImpl) obj;
					return other.entity.equals(entity);
				}
				
			}
			
		}
		
		interface LocatedBlock extends Located {
			
			default Block getBlock() {
				return getLocation().getBlock();
			}
			
			default Block getBlockNullable() {
				Location location = getLocation();
				if (location == null || location.getWorld() == null)
					return null;
				return location.getBlock();
			}

			@Override
			default LocatedType getType() {
				return LocatedType.BLOCK;
			}
			
			static LocatedBlock create(Block block) {
				return new LocatedBlockImpl(block);
			}
			
			static LocatedBlock create(Location location) {
				return new LocatedBlockLocationImpl(location);
			}
			
			class LocatedBlockLocationImpl extends LocatedImpl implements LocatedBlock {
				public LocatedBlockLocationImpl(Location location) {
					super(location);
				}
				
				@Override
				public Block getBlock() {
					return location.getBlock();
				}
				
				@Override
				public LocatedType getType() {
					// As LocatedBlockImpl inherits getType()
					// from both LocatedBlock AND LocatedImpl,
					// we redefine the method correctly.
					return LocatedType.BLOCK;
				}
				
			}
			
			class LocatedBlockImpl implements LocatedBlock {

				private Block block;

				public LocatedBlockImpl(Block block) {
					this.block = block;
				}

				@Override
				public Location getLocation() {
					return block.getLocation();
				}

				@Override
				public Block getBlock() {
					return block;
				}

				@Override
				public Block getBlockNullable() {
					return getBlock();
				}

			}

		}
		
	}
	
	enum LocatedType {
		BLOCK, ENTITY, OTHER;
	}
	
	/**
	 * Allows to check if some {@link LocatedType}s can be retrieved from a {@link Locatable} class.
	 * <p>
	 * If the Class <code>clazz</code> does not have a {@link LocatableType} annotation attached,
	 * then this will return <code>true</code> as it is assumed that it can retrieve all kinds of
	 * located types.
	 * 
	 * @param clazz	Class implementing the {@link Locatable} interface for which the <code>types</code>
	 * 				will get tested.
	 * @param types	Array of {@link LocatedType}s that must be checked they can be retrieved from
	 * 				the <code>clazz</code>.
	 * @return	<code>true</code> if the <code>clazz</code> can retrieve all passed <code>types</code>
	 * 			<b>OR</b> if the clazz does not have a {@link LocatableType} annotation attached,
	 * 			<code>false</code> otherwise.
	 */
	static boolean hasLocatedTypes(Class<? extends Locatable> clazz, LocatedType... types) {
		Set<LocatedType> toTest = new HashSet<>(Arrays.asList(types));
		boolean foundAnnotation = false;
		
		Class<?> superclass = clazz;
		do {
			LocatableType annotation = superclass.getDeclaredAnnotation(Locatable.LocatableType.class);
			if (annotation != null) {
				foundAnnotation = true;
				for (Locatable.LocatedType locatedType : annotation.types()) {
					toTest.remove(locatedType);
					if (toTest.isEmpty()) return true;
				}
			}
		}while ((superclass = superclass.getSuperclass()) != null);
		
		if (!foundAnnotation) {
			DebugUtils.logMessage("Class " + clazz.getName() + " does not have the @LocatableType annotation.");
			return true;
		}
		
		return false;
	}
	
}
