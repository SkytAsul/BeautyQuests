package fr.skytasul.quests.api.stages.types;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public interface Locatable {
	
	default boolean isShown() {
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
