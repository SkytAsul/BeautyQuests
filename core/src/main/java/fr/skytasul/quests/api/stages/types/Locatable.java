package fr.skytasul.quests.api.stages.types;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public interface Locatable {
	
	default boolean isShown() {
		return true;
	}
	
	default boolean canBeFetchedAsynchronously() {
		return true;
	}
	
	interface PreciseLocatable extends Locatable {
		
		Located getLocated();
		
	}
	
	interface MultipleLocatable extends Locatable {
		
		Collection<Located> getNearbyLocated(NearbyFetcher fetcher);
		
		interface NearbyFetcher {
			
			Location getCenter();
			
			double getMaxDistance();
			
			int getMaxAmount();
			
			default Class<? extends Located> getTargetClass() {
				return Located.class;
			}
			
			static NearbyFetcher create(Location location, double maxDistance, int maxAmount) {
				return new NearbyFetcherImpl(location, maxDistance, maxAmount, Located.class);
			}
			
			static NearbyFetcher create(Location location, double maxDistance, int maxAmount, Class<? extends Located> targetClass) {
				return new NearbyFetcherImpl(location, maxDistance, maxAmount, targetClass);
			}
			
			class NearbyFetcherImpl implements NearbyFetcher {
				private Location center;
				private double maxDistance;
				private int maxAmount;
				private Class<? extends Located> targetClass;
				
				public NearbyFetcherImpl(Location center, double maxDistance, int maxAmount, Class<? extends Located> targetClass) {
					this.center = center;
					this.maxDistance = maxDistance;
					this.maxAmount = maxAmount;
					this.targetClass = targetClass;
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
				public int getMaxAmount() {
					return maxAmount;
				}
				
				@Override
				public Class<? extends Located> getTargetClass() {
					return targetClass;
				}
				
			}
			
		}
		
	}
	
	interface Located {
		
		Location getLocation();
		
		static Located create(Location location) {
			return new LocatedImpl(location);
		}
		
		class LocatedImpl implements Located {
			private Location location;
			
			public LocatedImpl(Location location) {
				this.location = location;
			}
			
			@Override
			public Location getLocation() {
				return location;
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
			
			static LocatedBlock create(Block block) {
				return new LocatedBlockImpl(block.getLocation());
			}
			
			static LocatedBlock create(Location location) {
				return new LocatedBlockImpl(location);
			}
			
			class LocatedBlockImpl extends LocatedImpl implements LocatedBlock {
				public LocatedBlockImpl(Location location) {
					super(location);
				}
			}
			
		}
		
	}
	
}
