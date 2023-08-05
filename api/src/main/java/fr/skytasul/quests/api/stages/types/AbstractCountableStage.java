package fr.skytasul.quests.api.stages.types;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import fr.skytasul.quests.api.BossBarManager.BQBossBar;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.api.utils.itemdescription.HasItemsDescriptionConfiguration.HasMultipleObjects;
import fr.skytasul.quests.api.utils.itemdescription.ItemsDescriptionPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public abstract class AbstractCountableStage<T> extends AbstractStage implements HasMultipleObjects<T> {

	protected final @NotNull List<@NotNull CountableObject<T>> objects;

	protected @NotNull Map<Player, BossBar> bars = new HashMap<>();
	private boolean barsEnabled = false;
	private int cachedSize = 0;

	protected AbstractCountableStage(@NotNull StageController controller,
			@NotNull List<@NotNull CountableObject<T>> objects) {
		super(controller);
		this.objects = objects;
		calculateSize();
	}

	@Override
	public @NotNull List<@NotNull CountableObject<T>> getObjects() {
		return objects;
	}

	public @NotNull List<@NotNull MutableCountableObject<T>> getMutableObjects() {
		return objects.stream().map(countable -> CountableObject.createMutable(countable.getUUID(),
				cloneObject(countable.getObject()), countable.getAmount())).collect(Collectors.toList());
	}

	public @NotNull Optional<CountableObject<T>> getObject(@NotNull UUID uuid) {
		return objects.stream().filter(object -> object.getUUID().equals(uuid)).findAny();
	}

	@Deprecated
	public Map<Integer, Entry<T, Integer>> cloneObjects() {
		Map<Integer, Entry<T, Integer>> map = new HashMap<>();
		for (int id = 0; id < objects.size(); id++) {
			CountableObject<T> object = objects.get(id);
			map.put(id, new AbstractMap.SimpleEntry<>(cloneObject(object.getObject()), object.getAmount()));
		}
		return map;
	}

	@SuppressWarnings("rawtypes")
	public @UnknownNullability Map<@NotNull UUID, @NotNull Integer> getPlayerRemainings(@NotNull PlayerAccount acc,
			boolean warnNull) {
		Map<?, Integer> remaining = getData(acc, "remaining");
		if (warnNull && remaining == null)
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Cannot retrieve stage datas for " + acc.getNameAndID() + " on " + super.toString());

		if (remaining == null || remaining.isEmpty())
			return (Map) remaining;

		Object object = remaining.keySet().iterator().next();
		if (object instanceof Integer) {
			// datas before migration
			Map<UUID, Integer> newRemaining = new HashMap<>(remaining.size());
			Map<String, Integer> dataMap = new HashMap<>(remaining.size());
			remaining.forEach((key, amount) -> {
				UUID uuid = uuidFromLegacyIndex((Integer) key);
				if (!getObject(uuid).isPresent()) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("Cannot migrate " + acc.getNameAndID() + " data for stage " + toString()
							+ " as there is no migrated data for object " + key);
				}
				newRemaining.put(uuid, amount);
				dataMap.put(uuid.toString(), amount);
			});
			if (acc.isCurrent())
				updateObjective(acc.getPlayer(), "remaining", dataMap);
			return newRemaining;
		} else if (object instanceof String) {
			// datas stored as string
			return remaining.entrySet().stream()
					.collect(Collectors.toMap(entry -> UUID.fromString((String) entry.getKey()), Entry::getValue));
		} else
			throw new UnsupportedOperationException(object.getClass().getName());
	}

	@Override
	public @NotNull Map<CountableObject<T>, Integer> getPlayerAmounts(@NotNull PlayerAccount account) {
		Map<@NotNull UUID, @NotNull Integer> remainings = getPlayerRemainings(account, false);
		if (remainings == null || remainings.isEmpty())
			return (Map) remainings;

		return remainings.entrySet().stream()
				.map(entry -> Map.entry(getObject(entry.getKey()).orElse(null), entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public @Nullable CountableObject<T> getObject(int index) {
		return index >= objects.size() ? null : objects.get(index);
	}

	@Override
	public int getPlayerAmount(@NotNull PlayerAccount account, CountableObject<T> object) {
		// we do not use default implementation in HasMultipleObjects to avoid conversion from UUID to
		// CountableObject
		return getPlayerRemainings(account, false).get(object.getUUID());
	}

	@Override
	public int getTotalPlayerAmount(@NotNull PlayerAccount account) {
		// same as in getPlayerAmount
		return getPlayerRemainings(account, false).values().stream().mapToInt(Integer::intValue).sum();
	}

	@Override
	public @NotNull String getObjectName(CountableObject<T> object) {
		return getName(object.getObject());
	}

	protected void updatePlayerRemaining(@NotNull Player player, @NotNull Map<@NotNull UUID, @NotNull Integer> remaining) {
		updateObjective(player, "remaining", remaining.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue)));
	}

	protected void calculateSize() {
		cachedSize = objects.stream().mapToInt(CountableObject::getAmount).sum();
		barsEnabled = QuestsConfiguration.getConfig().getQuestsConfig().mobsProgressBar() && cachedSize > 0;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		ItemsDescriptionPlaceholders.register(placeholders, getPlaceholderKey(), this);
	}

	@Override
	public void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("remaining", objects.stream()
				.collect(Collectors.toMap(object -> object.getUUID().toString(), CountableObject::getAmount)));
	}

	/**
	 * When called, this will test the player datas for the passed object. If found, the remaining
	 * amount will be lowered. If no remaining items are found, the stage will complete.
	 * 
	 * @param acc player account
	 * @param p player
	 * @param object object of the event
	 * @param amount amount completed
	 * @return <code>true</code> if there is no need to call this method again in the same game tick.
	 */
	public boolean event(@NotNull Player p, @UnknownNullability Object object, int amount) {
		if (amount < 0) throw new IllegalArgumentException("Event amount must be positive (" + amount + ")");
		if (!canUpdate(p)) return true;

		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		for (CountableObject<T> countableObject : objects) {
			if (objectApplies(countableObject.getObject(), object)) {
				Map<UUID, Integer> playerAmounts = getPlayerRemainings(acc, true);
				if (playerAmounts == null) return true;
				if (playerAmounts.containsKey(countableObject.getUUID())) {
					int playerAmount = playerAmounts.remove(countableObject.getUUID());
					if (playerAmount <= amount) {
						// playerAmount - amount will be negative, so this object must be removed.
						// we do nothing as the entry has already been deleted
					} else
						playerAmounts.put(countableObject.getUUID(), playerAmount - amount);
				} else
					continue;
				
				if (playerAmounts.isEmpty()) {
					finishStage(p);
					return true;
				}else {
					if (barsEnabled) {
						BossBar bar = bars.get(p);
						if (bar == null) {
							QuestsPlugin.getPlugin().getLoggerExpanded().warning(p.getName() + " does not have boss bar for stage " + toString() + ". This is a bug!");
						}else bar.update(playerAmounts.values().stream().mapToInt(Integer::intValue).sum());
					}

					updatePlayerRemaining(p, playerAmounts);
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void started(@NotNull PlayerAccount acc) {
		super.started(acc);
		if (acc.isCurrent()) createBar(acc.getPlayer(), cachedSize);
	}

	@Override
	public void ended(@NotNull PlayerAccount acc) {
		super.ended(acc);
		if (acc.isCurrent()) removeBar(acc.getPlayer());
	}

	@Override
	public void unload() {
		super.unload();
		bars.values().forEach(BossBar::remove);
	}

	@Override
	public void joined(@NotNull Player p) {
		super.joined(p);
		Map<UUID, Integer> remainings = getPlayerRemainings(PlayersManager.getPlayerAccount(p), true);
		if (remainings == null) return;
		createBar(p, remainings.values().stream().mapToInt(Integer::intValue).sum());
	}
	
	@Override
	public void left(@NotNull Player p) {
		super.left(p);
		removeBar(p);
	}

	protected void createBar(@NotNull Player p, int amount) {
		if (barsEnabled) {
			if (bars.containsKey(p)) { // NOSONAR Map#computeIfAbsent cannot be used here as we should log the issue
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Trying to create an already existing bossbar for player " + p.getName());
				return;
			}
			bars.put(p, new BossBar(p, amount));
		}
	}

	protected void removeBar(@NotNull Player p) {
		if (bars.containsKey(p)) bars.remove(p).remove();
	}

	protected boolean objectApplies(@NotNull T object, @UnknownNullability Object other) {
		return object.equals(other);
	}
	
	protected @NotNull T cloneObject(@NotNull T object) {
		return object;
	}

	protected abstract @NotNull String getPlaceholderKey();

	protected abstract @NotNull String getName(@NotNull T object);

	protected abstract @NotNull Object serialize(@NotNull T object);

	protected abstract @NotNull T deserialize(@NotNull Object object);
	
	@Override
	protected void serialize(@NotNull ConfigurationSection section) {
		ConfigurationSection objectsSection = section.createSection("objects");
		for (CountableObject<T> obj : objects) {
			ConfigurationSection objectSection = objectsSection.createSection(obj.getUUID().toString());
			objectSection.set("amount", obj.getAmount());
			objectSection.set("object", serialize(obj.getObject()));
		}
	}

	protected void deserialize(@NotNull ConfigurationSection section) {
		ConfigurationSection objectsSection = section.getConfigurationSection("objects");
		if (objectsSection != null) {
			for (String key : objectsSection.getKeys(false)) {
				UUID uuid;
				try {
					uuid = UUID.fromString(key);
				} catch (IllegalArgumentException ex) {
					uuid = uuidFromLegacyIndex(Integer.parseInt(key));
				}

				ConfigurationSection objectSection = objectsSection.getConfigurationSection(key);
				Object serialized = objectSection.get("object");
				if (serialized instanceof ConfigurationSection) serialized = ((ConfigurationSection) serialized).getValues(false);
				objects.add(CountableObject.create(uuid, deserialize(serialized), objectSection.getInt("amount")));
			}
		}

		if (objects.isEmpty()) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Stage with no content: " + toString());
		calculateSize();
	}

	private static UUID uuidFromLegacyIndex(int index) { // useful for migration purpose
		return new UUID(index, 2478L);
		// 2478 is a magic value, the only necessity is that it stays constant
		// and I like the number 2478
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
			bar = QuestsAPI.getAPI().getBossBarManager()
					.buildBossBar(Lang.MobsProgression.format(getQuest().getName(), 100, 100), BarColor.YELLOW, style);
			update(amount);
		}

		public void remove() {
			bar.removeAll();
			if (timer != null) timer.cancel();
		}

		public void update(int amount) {
			if (amount >= 0 && amount <= cachedSize) {
				bar.setProgress((double) (cachedSize - amount) / (double) cachedSize);
			}else QuestsPlugin.getPlugin().getLoggerExpanded().warning("Amount of objects superior to max objects in " + AbstractCountableStage.this.toString() + " for player " + p.getName() + ": " + amount + " > " + cachedSize);
			bar.setTitle(Lang.MobsProgression.format(getQuest().getName(), cachedSize - amount, cachedSize));
			bar.addPlayer(p);
			timer();
		}

		private void timer() {
			if (QuestsConfiguration.getConfig().getQuestsConfig().progressBarTimeoutSeconds() <= 0)
				return;
			if (timer != null)
				timer.cancel();

			timer = Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(), () -> {
				bar.removePlayer(p);
				timer = null;
			}, QuestsConfiguration.getConfig().getQuestsConfig().progressBarTimeoutSeconds() * 20L);
		}
	}

}
