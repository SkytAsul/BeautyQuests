package fr.skytasul.quests.stages;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.internal.BQMobDeathEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.AbstractCountableStage;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.gui.mobs.MobsListGUI;
import fr.skytasul.quests.mobs.BukkitEntityFactory;
import fr.skytasul.quests.mobs.Mob;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@LocatableType (types = LocatedType.ENTITY)
public class StageMobs extends AbstractCountableStage<Mob<?>> implements Locatable.MultipleLocatable, Listener {

	private boolean shoot = false;
	private boolean preventNpcsForVanilla = true;

	public StageMobs(StageController controller, List<CountableObject<Mob<?>>> mobs) {
		super(controller, mobs);
	}

	public boolean isShoot() {
		return shoot;
	}

	public void setShoot(boolean shoot) {
		this.shoot = shoot;
	}

	@EventHandler
	public void onMobKilled(BQMobDeathEvent e){
		if (shoot && e.getBukkitEntity() != null && e.getBukkitEntity().getLastDamageCause() != null
				&& e.getBukkitEntity().getLastDamageCause().getCause() != DamageCause.PROJECTILE)
			return;

		Player p = e.getKiller();
		if (p == e.getBukkitEntity()) return; // player suicidal
		event(p, new KilledMob(e.getPluginMob(), e.getBukkitEntity()), e.getAmount());
	}

	@Override
	protected boolean objectApplies(Mob<?> object, Object other) {
		KilledMob otherMob = (KilledMob) other;

		if (!object.applies(otherMob.pluginMob))
			return false;

		if (preventNpcsForVanilla && object.getFactory() instanceof BukkitEntityFactory) {
			// kinda hacky, TODO find a way to make this more flexible (list of NPC provider exclusion, other
			// mob factories, etc..)
			// when removing the preventNpcsForVanilla option, notify AerWyn81 on Discord
			if (QuestsPlugin.getPlugin().getNpcManager().isNPC(otherMob.bukkitEntity))
				return false;
		}

		if (object.getMinLevel() != null) {
			if (object.getLevel(otherMob.bukkitEntity) < object.getMinLevel())
				return false;
		}

		return true;
	}

	@Override
	protected @NotNull String getPlaceholderKey() {
		return "mobs";
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_MOBS.toString();
	}

	@Override
	public void started(PlayerAccount acc) {
		super.started(acc);
		if (acc.isCurrent() && sendStartMessage()) {
			MessageUtils.sendRawMessage(acc.getPlayer(), Lang.STAGE_MOBSLIST.toString(), getPlaceholdersRegistry(),
					StageDescriptionPlaceholdersContext.of(true, acc, DescriptionSource.FORCELINE, MessageType.DefaultMessageType.PREFIXED));
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
		if (!preventNpcsForVanilla)
			section.set("preventNpcsForVanilla", false);
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

	public static StageMobs deserialize(ConfigurationSection section, StageController controller) {
		StageMobs stage = new StageMobs(controller, new ArrayList<>());
		stage.deserialize(section);

		if (section.contains("shoot")) stage.shoot = section.getBoolean("shoot");
		if (section.contains("preventNpcsForVanilla"))
			stage.preventNpcsForVanilla = section.getBoolean("preventNpcsForVanilla");

		return stage;
	}

	public static class Creator extends StageCreation<StageMobs> {

		private List<MutableCountableObject<Mob<?>>> mobs;
		private boolean shoot = false;
		private boolean preventNpcsForVanilla = true;

		public Creator(@NotNull StageCreationContext<StageMobs> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(7, ItemUtils.item(XMaterial.STONE_SWORD, Lang.editMobs.toString()), event -> {
				new MobsListGUI(mobs, newMobs -> {
					setMobs(newMobs);
					event.reopen();
				}).open(event.getPlayer());
			});
			line.setItem(6, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), shoot), event -> setShoot(!shoot));
		}

		public void setMobs(List<MutableCountableObject<Mob<?>>> mobs) {
			this.mobs = mobs;
			getLine().refreshItem(7,
					item -> ItemUtils.loreOptionValue(item, Lang.AmountMobs.quickFormat("mobs_amount", mobs.size())));
		}

		public void setShoot(boolean shoot) {
			if (this.shoot != shoot) {
				this.shoot = shoot;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, shoot));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			new MobsListGUI(Collections.emptyList(), newMobs -> {
				setMobs(newMobs);
				context.reopenGui();
			}).open(p);
		}

		@Override
		public StageMobs finishStage(StageController controller) {
			StageMobs stage = new StageMobs(controller,
					mobs.stream().map(MutableCountableObject::toImmutable).collect(Collectors.toList()));
			stage.setShoot(shoot);
			stage.preventNpcsForVanilla = preventNpcsForVanilla;
			return stage;
		}

		@Override
		public void edit(StageMobs stage) {
			super.edit(stage);
			setMobs(stage.getMutableObjects());
			setShoot(stage.shoot);
			preventNpcsForVanilla = stage.preventNpcsForVanilla;
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
