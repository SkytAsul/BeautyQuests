package fr.skytasul.quests.api.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.bossbar.MobBossBar;

public abstract class AbstractCountableStage<T> extends AbstractStage {

	protected Map<Integer, Entry<T, Integer>> objects;

	protected Map<Player, BossBar> bars = new HashMap<>();
	private boolean barsEnabled = false;
	private int cachedSize = 0;

	public AbstractCountableStage(QuestBranch branch, Map<Integer, Entry<T, Integer>> objects) {
		super(branch);
		this.objects = objects;
		calculateSize();
	}

	public Map<Integer, Entry<T, Integer>> getObjects() {
		return objects;
	}

	public Map<Integer, Entry<T, Integer>> cloneObjects() {
		Map<Integer, Entry<T, Integer>> map = new HashMap<>();
		for (Entry<Integer, Entry<T, Integer>> entry : objects.entrySet()) {
			map.put(entry.getKey(), new AbstractMap.SimpleEntry<>(cloneObject(entry.getValue().getKey()), entry.getValue().getValue()));
		}
		return map;
	}

	public Map<Integer, Integer> getPlayerRemainings(PlayerAccount acc) {
		return getData(acc, "remaining");
	}

	protected void calculateSize() {
		cachedSize = 0;
		for (Entry<T, Integer> objectsEntry : objects.values()) {
			cachedSize += objectsEntry.getValue();
		}
		barsEnabled = QuestsConfiguration.showMobsProgressBar() && cachedSize > 0;
	}

	protected String descriptionLine(PlayerAccount acc, Source source){
		return Utils.descriptionLines(source, buildRemainingArray(acc, source));
	}

	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new String[] { Utils.itemsToFormattedString(buildRemainingArray(acc, source), QuestsConfiguration.getItemAmountColor()) };
	}

	private String[] buildRemainingArray(PlayerAccount acc, Source source) {
		Map<Integer, Integer> playerAmounts = getPlayerRemainings(acc);
		if (playerAmounts == null) {
			BeautyQuests.logger.severe("The plugin has been unable to retrieve stage datas for account " + acc.debugName() + " on " + super.debugName());
			return new String[] { "§4§lerror" };
		}
		String[] elements = new String[playerAmounts.size()];
		int i = 0;
		for (Entry<Integer, Integer> obj : playerAmounts.entrySet()) {
			Entry<T, Integer> object = objects.get(obj.getKey());
			elements[i] = object == null ? "no object " + obj.getKey() : QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(getName(object.getKey()), QuestsConfiguration.getItemAmountColor(), obj.getValue(), object.getValue(), QuestsConfiguration.showDescriptionItemsXOne(source));
			i++;
		}
		return elements;
	}

	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		Map<Integer, Integer> amounts = new HashMap<>();
		for (Entry<Integer, Entry<T, Integer>> entry : objects.entrySet()) {
			amounts.put(entry.getKey(), entry.getValue().getValue());
		}
		datas.put("remaining", amounts);
	}

	public void event(PlayerAccount acc, Player p, Object object, int amount) {
		if (amount < 0) throw new IllegalArgumentException("Event amount must be positive (" + amount + ")");
		if (!canUpdate(p)) return;
		for (Entry<Integer, Entry<T, Integer>> entry : objects.entrySet()) {
			int id = entry.getKey();
			if (objectApplies(entry.getValue().getKey(), object)) {
				Map<Integer, Integer> playerAmounts = getPlayerRemainings(acc);
				if (!playerAmounts.containsKey(id)) return;
				int playerAmount = playerAmounts.get(id);
				if (playerAmount <= amount) {
					playerAmounts.remove(id);
				}else playerAmounts.put(id, playerAmount -= amount);

				if (playerAmounts.isEmpty()) {
					finishStage(p);
				}else {
					if (barsEnabled) {
						BossBar bar = bars.get(p);
						if (bar == null) {
							BeautyQuests.logger.warning(p.getName() + " does not have boss bar for stage " + debugName() + ". This is a bug!");
						}else bar.update(playerAmounts.values().stream().mapToInt(Integer::intValue).sum());
					}
					updateObjective(acc, p, "remaining", playerAmounts);
				}
				return;
			}
		}
	}

	@SuppressWarnings ("deprecation")
	@Deprecated
	protected void migrateDatas(PlayerAccount acc, Map<T, Integer> oldObjects) {
		Map<Integer, Integer> amounts = new HashMap<>();
		for (int i = 0; i < objects.size(); i++) {
			Entry<T, Integer> object = objects.get(i);
			if (oldObjects.containsKey(object.getKey())) {
				amounts.put(i, oldObjects.get(object.getKey()));
			}
		}
		setData(acc, "remaining", amounts);
	}

	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) createBar(acc.getPlayer(), cachedSize);
	}

	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) removeBar(acc.getPlayer());
	}

	public void unload() {
		super.unload();
		bars.values().forEach(BossBar::remove);
	}

	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		Map<Integer, Integer> remainings = getPlayerRemainings(acc);
		if (remainings == null) {
			BeautyQuests.logger.severe(p.getName() + " does not have remaining datas for stage " + debugName() + ". This is a bug!");
			return;
		}
		createBar(p, remainings.values().stream().mapToInt(Integer::intValue).sum());
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		removeBar(p);
	}

	protected void createBar(Player p, int amount) {
		if (barsEnabled) bars.put(p, new BossBar(p, amount));
	}

	protected void removeBar(Player p) {
		if (bars.containsKey(p)) bars.remove(p).remove();
	}

	protected boolean objectApplies(T object, Object other) {
		return object.equals(other);
	}
	
	protected T cloneObject(T object) {
		return object;
	}

	protected abstract String getName(T object);

	protected abstract Object serialize(T object);

	protected abstract T deserialize(Object object);

	protected void serialize(Map<String, Object> map) {
		Map<Integer, Map<String, Object>> serializedObjects = new HashMap<>(objects.size());
		for (Entry<Integer, Entry<T, Integer>> obj : objects.entrySet()) {
			Map<String, Object> serializedObject = new HashMap<>(2);
			serializedObject.put("object", serialize(obj.getValue().getKey()));
			serializedObject.put("amount", obj.getValue().getValue());
			serializedObjects.put(obj.getKey(), serializedObject);
		}
		map.put("objects", serializedObjects);
	}

	protected void deserialize(Map<String, Object> serializedDatas) {
		Map<Integer, Map<String, Object>> serializedObjects = (Map<Integer, Map<String, Object>>) serializedDatas.get("objects");
		if (serializedObjects != null) {
			for (Entry<Integer, Map<String, Object>> obj : serializedObjects.entrySet()) {
				objects.put(obj.getKey(), new AbstractMap.SimpleEntry<>(deserialize(obj.getValue().get("object")), (int) obj.getValue().get("amount")));
			}
		}
		if (objects.isEmpty()) BeautyQuests.logger.warning("A " + getClass().getSimpleName() + " stage in the quest ID " + branch.getQuest().getID() + " have no content.");
		calculateSize();
	}

	class BossBar {
		private Player p;
		private MobBossBar bar;
		private BukkitTask timer;

		public BossBar(Player p, int amount) {
			this.p = p;
			bar = new MobBossBar(branch.getQuest().getName(), cachedSize);
			update(amount);
		}

		public void remove() {
			bar.removeAll();
			if (timer != null) timer.cancel();
		}

		public void update(int amount) {
			bar.setProgress(amount);
			bar.addPlayer(p);
			timer();
		}

		private void timer() {
			if (QuestsConfiguration.getProgressBarTimeout() <= 0) return;
			if (timer != null) timer.cancel();
			timer = Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
				bar.removePlayer(p);
				timer = null;
			}, QuestsConfiguration.getProgressBarTimeout() * 20L);
		}
	}

}
