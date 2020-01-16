package fr.skytasul.quests.api.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerAccountJoinEvent;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.Post1_9;

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
			map.put(entry.getKey(), new AbstractMap.SimpleEntry<>(entry.getValue().getKey(), entry.getValue().getValue()));
		}
		return map;
	}

	public Map<Integer, Integer> getPlayerRemainings(PlayerAccount acc) {
		return getData(acc, "remaining", Map.class);
	}

	protected void calculateSize() {
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
		String[] elements = new String[playerAmounts.size()];
		int i = 0;
		for (Entry<Integer, Integer> obj : playerAmounts.entrySet()) {
			elements[i] = QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(getName(objects.get(obj.getKey()).getKey()), QuestsConfiguration.getItemAmountColor(), obj.getValue(), QuestsConfiguration.showDescriptionItemsXOne(source));
			i++;
		}
		return elements;
	}

	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		Map<Integer, Integer> amounts = new HashMap<>();
		for (int i = 0; i < objects.size(); i++) {
			amounts.put(i, objects.get(i).getValue());
		}
		datas.put("remaining", amounts);
	}

	protected void event(PlayerAccount acc, Player p, Object object, int amount) {
		for (int id = 0; id < objects.size(); id++) {
			if (objectApplies(objects.get(id).getKey(), object)) {
				Map<Integer, Integer> playerAmounts = getPlayerRemainings(acc);
				if (!playerAmounts.containsKey(id)) return;
				int playerAmount = playerAmounts.get(id);
				if (playerAmount <= amount) {
					playerAmounts.remove(id);
				}else playerAmounts.put(id, playerAmount -= amount);

				if (playerAmounts.isEmpty()) {
					finishStage(p);
				}else {
					if (barsEnabled) bars.get(p).update(playerAmounts.values().stream().mapToInt(Integer::intValue).sum());
					updateObjective(acc, p, "remaining", playerAmounts);
				}
				return;
			}
		}
	}

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
		for (BossBar bar : bars.values()) {
			bar.remove();
		}
	}

	@EventHandler
	public void onAccountUse(PlayerAccountJoinEvent e) {
		Player p = e.getPlayer();
		removeBar(p);
		if (branch.hasStageLaunched(e.getPlayerAccount(), this)) {
			createBar(p, getPlayerRemainings(e.getPlayerAccount()).values().stream().mapToInt(x -> x).sum());
		}
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
		private Object bar;
		private BukkitTask timer;

		public BossBar(Player p, int amount) {
			this.p = p;
			bar = Post1_9.createMobsBar(branch.getQuest().getName(), cachedSize);
			update(amount);
		}

		public void remove() {
			Post1_9.removeBar(bar);
			if (timer != null) timer.cancel();
		}

		public void update(int amount) {
			Post1_9.setBarProgress(branch.getQuest().getName(), bar, amount, cachedSize);
			Post1_9.showBar(bar, p);
			timer();
		}

		private void timer() {
			if (QuestsConfiguration.getProgressBarTimeout() <= 0) return;
			if (timer != null) timer.cancel();
			timer = new BukkitRunnable() {
				public void run() {
					Post1_9.hideBar(bar, p);
					timer = null;
				}
			}.runTaskLater(BeautyQuests.getInstance(), QuestsConfiguration.getProgressBarTimeout() * 20L);
		}
	}

}
