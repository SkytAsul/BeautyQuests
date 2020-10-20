package fr.skytasul.quests.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.mobs.Mob;
import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.mobs.MobsListGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
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
		if (shoot && e.getBukkitEntity().getLastDamageCause().getCause() != DamageCause.PROJECTILE) return;
		Player p = e.getKiller();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			event(acc, p, e.getPluginMob(), 1);
		}
	}
	
	protected boolean objectApplies(Mob<?> object, Object other) {
		return object.applies(other);
	}

	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_MOBS.format(super.descriptionLine(acc, source));
	}
	
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent() && sendStartMessage()) {
			Lang.STAGE_MOBSLIST.send(acc.getPlayer(), super.descriptionFormat(acc, Source.FORCELINE));
		}
	}

	protected String getName(Mob<?> object) {
		return object.getName();
	}

	protected Object serialize(Mob<?> object) {
		return object.serialize();
	}

	protected Mob<?> deserialize(Object object) {
		return Mob.deserialize((Map<String, Object>) object);
	}

	protected void serialize(Map<String, Object> map){
		super.serialize(map);
		if (shoot) map.put("shoot", true);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		Map<Integer, Entry<Mob<?>, Integer>> objects = new HashMap<>();
		if (map.containsKey("mobs")) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("mobs");
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> serializedMob = list.get(i);
				Mob<?> mob = Mob.deserialize(serializedMob);
				objects.put(i, new AbstractMap.SimpleEntry<>(mob, (int) serializedMob.get("amount")));
			}
		}
		StageMobs stage = new StageMobs(branch, objects);
		stage.deserialize(map);
		
		if (map.containsKey("remaining")) {
			PlayersManagerYAML migration = PlayersManagerYAML.getMigrationYAML();
			Map<String, List<Map<String, Object>>> re = (Map<String, List<Map<String, Object>>>) map.get("remaining");
			for (Entry<String, List<Map<String, Object>>> en : re.entrySet()){
				PlayerAccount acc = migration.getByIndex(en.getKey());
				if (acc == null) continue;
				Map<Mob<?>, Integer> oldMobs = new HashMap<>();
				for (Map<String, Object> serializedMob : en.getValue()) {
					oldMobs.put(Mob.deserialize(serializedMob), (int) serializedMob.get("amount"));
				}
				stage.migrateDatas(acc, oldMobs);
			}
		}

		if (map.containsKey("shoot")) stage.shoot = (boolean) map.get("shoot");
		return stage;
	}

	public static class Creator implements StageCreationRunnables<StageMobs> {
		private static final ItemStack editMobs = ItemUtils.item(XMaterial.STONE_SWORD, Lang.editMobs.toString());

		public void start(Player p, LineData datas) {
			StagesGUI sg = datas.getGUI();
			Line line = datas.getLine();
			MobsListGUI mobs = Inventories.create(p, new MobsListGUI());
			mobs.run = (obj) -> {
				sg.reopen(p, true);
				setItems(line, sg, datas);
				datas.put("mobs", obj);
			};
		}

		public static void setItems(Line line, StagesGUI sg, LineData datas) {
			line.setItem(6, editMobs.clone(), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					MobsListGUI mobs = Inventories.create(p, new MobsListGUI());
					mobs.setMobsFromMap(datas.get("mobs"));
					mobs.run = (obj) -> {
						sg.reopen(p, true);
						datas.put("mobs", obj);
					};
				}
			});
			line.setItem(5, ItemUtils.itemSwitch(Lang.mobsKillType.toString(), datas.containsKey("shoot") ? datas.get("shoot") : false), new StageRunnable() {
				public void run(Player p, LineData datas, ItemStack item) {
					datas.put("shoot", ItemUtils.toggle(datas.getLine().getItem(5)));
				}
			});
		}

		public StageMobs finish(LineData datas, QuestBranch branch) {
			StageMobs stage = new StageMobs(branch, datas.get("mobs"));
			if (datas.containsKey("shoot")) stage.setShoot(datas.get("shoot"));
			return stage;
		}

		public void edit(LineData datas, StageMobs stage) {
			datas.put("mobs", stage.cloneObjects());
			datas.put("shoot", stage.isShoot());
			setItems(datas.getLine(), datas.getGUI(), datas);
		}
	}

}
