package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.AbstractCountableStage;
import fr.skytasul.quests.gui.Inventories;
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

public class StageMobs extends AbstractCountableStage<Mob<?>> {

	private boolean shoot = false;
	
	public StageMobs(QuestBranch branch, Map<Integer, Entry<Mob<?>, Integer>> mobs) {
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
			event(acc, p, e.getPluginMob(), 1);
		}
	}
	
	@Override
	protected boolean objectApplies(Mob<?> object, Object other) {
		return object.applies(other);
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_MOBS.format(super.descriptionLine(acc, source));
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent() && sendStartMessage()) {
			Lang.STAGE_MOBSLIST.send(acc.getPlayer(), super.descriptionFormat(acc, Source.FORCELINE));
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
	
	public static StageMobs deserialize(ConfigurationSection section, QuestBranch branch) {
		StageMobs stage = new StageMobs(branch, new HashMap<>());
		stage.deserialize(section);

		if (section.contains("shoot")) stage.shoot = section.getBoolean("shoot");
		return stage;
	}

	public static class Creator extends StageCreation<StageMobs> {

		private Map<Integer, Entry<Mob<?>, Integer>> mobs;
		private boolean shoot = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.STONE_SWORD, Lang.editMobs.toString()), (p, item) -> {
				MobsListGUI mobsGUI = Inventories.create(p, new MobsListGUI());
				mobsGUI.setMobsFromMap(mobs);
				mobsGUI.run = (obj) -> {
					mobs = obj;
					reopenGUI(p, true);
				};
			});
			line.setItem(6, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), shoot), (p, item) -> setShoot(ItemUtils.toggle(item)));
		}
		
		public void setMobs(Map<Integer, Entry<Mob<?>, Integer>> mobs) {
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
			MobsListGUI mobsGUI = Inventories.create(p, new MobsListGUI());
			mobsGUI.run = (obj) -> {
				setMobs(obj);
				reopenGUI(p, true);
			};
		}

		@Override
		public StageMobs finishStage(QuestBranch branch) {
			StageMobs stage = new StageMobs(branch, mobs);
			stage.setShoot(shoot);
			return stage;
		}

		@Override
		public void edit(StageMobs stage) {
			super.edit(stage);
			setMobs(stage.cloneObjects());
			setShoot(stage.shoot);
		}
	}

}
