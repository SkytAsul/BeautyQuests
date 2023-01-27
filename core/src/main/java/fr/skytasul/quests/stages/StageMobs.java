package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.AbstractCountableStage;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.mobs.MobsListGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.mobs.CompatMobDeathEvent;
import fr.skytasul.quests.utils.types.CountableObject;
import fr.skytasul.quests.utils.types.CountableObject.MutableCountableObject;

@LocatableType (types = LocatedType.ENTITY)
public class StageMobs extends AbstractCountableStage<Mob<?>> implements Locatable.MultipleLocatable {

	private boolean shoot = false;
	
	public StageMobs(QuestBranch branch, List<CountableObject<Mob<?>>> mobs) {
		super(branch, mobs);
	}

	public boolean isShoot() {
		return shoot;
	}

	public void setShoot(boolean shoot) {
		this.shoot = shoot;
	}
	
	@EventHandler
	public void onMobKilled(CompatMobDeathEvent e){
		if (shoot && e.getBukkitEntity() != null && e.getBukkitEntity().getLastDamageCause().getCause() != DamageCause.PROJECTILE) return;
		Player p = e.getKiller();
		if (p == e.getBukkitEntity()) return; // player suicidal
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			event(acc, p, new KilledMob(e.getPluginMob(), e.getBukkitEntity()), e.getAmount());
		}
	}
	
	@Override
	protected boolean objectApplies(Mob<?> object, Object other) {
		KilledMob otherMob = (KilledMob) other;

		if (!object.applies(otherMob.pluginMob))
			return false;

		if (object.getMinLevel() != null) {
			if (object.getLevel(otherMob.bukkitEntity) < object.getMinLevel())
				return false;
		}

		return true;
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_MOBS.format(super.descriptionLine(acc, source));
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent() && sendStartMessage()) {
			Lang.STAGE_MOBSLIST.send(acc.getPlayer(), (Object[]) super.descriptionFormat(acc, Source.FORCELINE));
		}
	}

	@Override
	protected Mob<?> cloneObject(Mob<?> object) {
		return object.clone();
	}
	
	@Override
	protected String getName(Mob<?> object) {
		return object.getName();
	}

	@Override
	protected Object serialize(Mob<?> object) {
		return object.serialize();
	}

	@Override
	protected Mob<?> deserialize(Object object) {
		return Mob.deserialize((Map<String, Object>) object);
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		super.serialize(section);
		if (shoot) section.set("shoot", true);
	}
	
	@Override
	public boolean canBeFetchedAsynchronously() {
		return false;
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		if (!fetcher.isTargeting(LocatedType.ENTITY)) return Spliterators.emptySpliterator();
		return fetcher.getCenter().getWorld()
				.getEntities()
				.stream()
				.filter(entity -> objects.stream().anyMatch(entry -> entry.getObject().appliesEntity(entity)))
				.map(x -> {
					double ds = x.getLocation().distanceSquared(fetcher.getCenter());
					if (ds > fetcher.getMaxDistanceSquared()) return null;
					return new AbstractMap.SimpleEntry<>(x, ds);
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(Entry::getValue))
				.<Located>map(entry -> Located.LocatedEntity.create(entry.getKey()))
				.spliterator();
	}
	
	public static StageMobs deserialize(ConfigurationSection section, QuestBranch branch) {
		StageMobs stage = new StageMobs(branch, new ArrayList<>());
		stage.deserialize(section);

		if (section.contains("shoot")) stage.shoot = section.getBoolean("shoot");
		return stage;
	}

	public static class Creator extends StageCreation<StageMobs> {

		private List<MutableCountableObject<Mob<?>>> mobs;
		private boolean shoot = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.STONE_SWORD, Lang.editMobs.toString()), (p, item) -> {
				new MobsListGUI(mobs, newMobs -> {
					setMobs(newMobs);
					reopenGUI(p, true);
				}).create(p);
			});
			line.setItem(6, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), shoot), (p, item) -> setShoot(ItemUtils.toggle(item)));
		}
		
		public void setMobs(List<MutableCountableObject<Mob<?>>> mobs) {
			this.mobs = mobs;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(Lang.AmountMobs.format(mobs.size()))));
		}
		
		public void setShoot(boolean shoot) {
			if (this.shoot != shoot) {
				this.shoot = shoot;
				line.editItem(6, ItemUtils.set(line.getItem(6), shoot));
			}
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new MobsListGUI(Collections.emptyList(), newMobs -> {
				setMobs(newMobs);
				reopenGUI(p, true);
			}).create(p);
		}

		@Override
		public StageMobs finishStage(QuestBranch branch) {
			StageMobs stage = new StageMobs(branch,
					mobs.stream().map(MutableCountableObject::toImmutable).collect(Collectors.toList()));
			stage.setShoot(shoot);
			return stage;
		}

		@Override
		public void edit(StageMobs stage) {
			super.edit(stage);
			setMobs(stage.getMutableObjects());
			setShoot(stage.shoot);
		}
	}

	private class KilledMob {
		final Object pluginMob;
		final Entity bukkitEntity;

		KilledMob(Object pluginMob, Entity bukkitEntity) {
			this.pluginMob = pluginMob;
			this.bukkitEntity = bukkitEntity;
		}
	}

}
