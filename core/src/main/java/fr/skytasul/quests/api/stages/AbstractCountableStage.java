package fr.skytasul.quests.api.stages;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.bossbar.BQBossBarManager.BQBossBar;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

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

	@Override
	protected String descriptionLine(PlayerAccount acc, Source source){
		return Utils.descriptionLines(source, buildRemainingArray(acc, source));
	}

	@Override
	protected Supplier<Object>[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Supplier[] { () -> Utils.descriptionLines(source, buildRemainingArray(acc, source)) };
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

	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		Map<Integer, Integer> amounts = new HashMap<>();
		for (Entry<Integer, Entry<T, Integer>> entry : objects.entrySet()) {
			amounts.put(entry.getKey(), entry.getValue().getValue());
		}
		datas.put("remaining", amounts);
	}

	/**
	 * When called, this will test the player datas for the passed object.
	 * If found, the remaining amount will be lowered.
	 * If no remaining items are found, the stage will complete.
	 * @param acc player account
	 * @param p player
	 * @param object object of the event
	 * @param amount amount completed
	 * @return <code>false</code> if there is no need to call this method again in the same game tick.
	 */
	public boolean event(PlayerAccount acc, Player p, Object object, int amount) {
		if (amount < 0) throw new IllegalArgumentException("Event amount must be positive (" + amount + ")");
		if (!canUpdate(p)) return true;
		for (Entry<Integer, Entry<T, Integer>> entry : objects.entrySet()) {
			int id = entry.getKey();
			if (objectApplies(entry.getValue().getKey(), object)) {
				Map<Integer, Integer> playerAmounts = getPlayerRemainings(acc);
				if (playerAmounts == null) {
					BeautyQuests.logger.warning(p.getName() + " does not have object datas for stage " + debugName() + ". This is a bug!");
					return true;
				}
				if (playerAmounts.containsKey(id)) {
					int playerAmount = playerAmounts.get(id);
					if (playerAmount <= amount) {
						playerAmounts.remove(id);
					}else playerAmounts.put(id, playerAmount -= amount);
				}
				
				if (playerAmounts.isEmpty()) {
					finishStage(p);
					return true;
				}else {
					if (barsEnabled) {
						BossBar bar = bars.get(p);
						if (bar == null) {
							BeautyQuests.logger.warning(p.getName() + " does not have boss bar for stage " + debugName() + ". This is a bug!");
						}else bar.update(playerAmounts.values().stream().mapToInt(Integer::intValue).sum());
					}
					updateObjective(acc, p, "remaining", playerAmounts);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) createBar(acc.getPlayer(), cachedSize);
	}

	@Override
	public void end(PlayerAccount acc) {
		super.end(acc);
		if (acc.isCurrent()) removeBar(acc.getPlayer());
	}

	@Override
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

	/**
	 * @deprecated for removal, {@link #serialize(ConfigurationSection)} should be used instead.
	 */
	@Override
	@Deprecated
	protected void serialize(Map<String, Object> map) {}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		ConfigurationSection objectsSection = section.createSection("objects");
		for (Entry<Integer, Entry<T, Integer>> obj : objects.entrySet()) {
			ConfigurationSection objectSection = objectsSection.createSection(Integer.toString(obj.getKey()));
			objectSection.set("amount", obj.getValue().getValue());
			objectSection.set("object", serialize(obj.getValue().getKey()));
		}
		Map<String, Object> serialized = new HashMap<>();
		serialize(serialized);
		serialized.forEach(section::set);
	}
	
	/**
	 * @deprecated for removal, {@link #deserialize(ConfigurationSection)} should be used instead.
	 */
	@Deprecated
	protected void deserialize(Map<String, Object> serializedDatas) {
		MemoryConfiguration configuration = new MemoryConfiguration();
		serializedDatas.forEach(configuration::set);
		deserialize(configuration);
	}

	protected void deserialize(ConfigurationSection section) {
		ConfigurationSection objectsSection = section.getConfigurationSection("objects");
		if (objectsSection != null) {
			for (String key : objectsSection.getKeys(false)) {
				ConfigurationSection object = objectsSection.getConfigurationSection(key);
				Object serialized = object.get("object");
				if (serialized instanceof ConfigurationSection) serialized = ((ConfigurationSection) serialized).getValues(false);
				objects.put(Integer.parseInt(key), new AbstractMap.SimpleEntry<>(deserialize(serialized), object.getInt("amount")));
			}
		}
		
		if (objects.isEmpty()) BeautyQuests.logger.warning("Stage with no content: " + debugName());
		calculateSize();
	}

	class BossBar {
		private Player p;
		private BQBossBar bar;
		private BukkitTask timer;

		public BossBar(Player p, int amount) {
			this.p = p;
			
			BarStyle style = null;
			if (cachedSize % 20 == 0) {
				style = BarStyle.SEGMENTED_20;
			}else if (cachedSize % 10 == 0) {
				style = BarStyle.SEGMENTED_10;
			}else if (cachedSize % 12 == 0) {
				style = BarStyle.SEGMENTED_12;
			}else if (cachedSize % 6 == 0) {
				style = BarStyle.SEGMENTED_6;
			}else style = BarStyle.SOLID;
			bar = QuestsAPI.getBossBarManager().buildBossBar(Lang.MobsProgression.format(branch.getQuest().getName(), 100, 100), BarColor.YELLOW, style);
			update(amount);
		}

		public void remove() {
			bar.removeAll();
			if (timer != null) timer.cancel();
		}

		public void update(int amount) {
			if (amount >= 0 && amount <= cachedSize) {
				bar.setProgress((double) (cachedSize - amount) / (double) cachedSize);
			}else BeautyQuests.logger.warning("Amount of objects superior to max objects in " + debugName() + " for player " + p.getName() + ": " + amount + " > " + cachedSize);
			bar.setTitle(Lang.MobsProgression.format(branch.getQuest().getName(), cachedSize - amount, cachedSize));
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
