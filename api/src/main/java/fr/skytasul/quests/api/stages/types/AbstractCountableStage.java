package fr.skytasul.quests.api.stages.types;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
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
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.BossBarManager.BQBossBar;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.utils.types.CountableObject;
import fr.skytasul.quests.utils.types.CountableObject.MutableCountableObject;

public abstract class AbstractCountableStage<T> extends AbstractStage {

	protected final @NotNull List<@NotNull CountableObject<T>> objects;

	protected @NotNull Map<Player, BossBar> bars = new HashMap<>();
	private boolean barsEnabled = false;
	private int cachedSize = 0;

	protected AbstractCountableStage(@NotNull QuestBranch branch, @NotNull List<@NotNull CountableObject<T>> objects) {
		super(branch);
		this.objects = objects;
		calculateSize();
	}

	@Deprecated
	protected AbstractCountableStage(QuestBranch branch, Map<Integer, Entry<T, Integer>> objects) {
		this(branch, objects.keySet().stream().sorted().map(index -> {
			Entry<T, Integer> entry = objects.get(index);
			return CountableObject.open(uuidFromLegacyIndex(index), entry.getKey(), entry.getValue());
		}).collect(Collectors.toList()));

		QuestsPlugin.getPlugin().getLoggerExpanded().warning("The stage " + getType().getName()
				+ " uses an outdated way to store player datas. Please notice its author.");
	}

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
	public @NotNull Map<@NotNull UUID, @NotNull Integer> getPlayerRemainings(@NotNull PlayerAccount acc, boolean warnNull) {
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
			updateObjective(acc, acc.getPlayer(), "remaining", dataMap);
			return newRemaining;
		} else if (object instanceof String) {
			// datas stored as string
			return remaining.entrySet().stream()
					.collect(Collectors.toMap(entry -> UUID.fromString((String) entry.getKey()), Entry::getValue));
		} else
			throw new UnsupportedOperationException(object.getClass().getName());
	}

	protected void updatePlayerRemaining(@NotNull PlayerAccount acc, @NotNull Player player,
			@NotNull Map<@NotNull UUID, @NotNull Integer> remaining) {
		updateObjective(acc, player, "remaining", remaining.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue)));
	}

	protected void calculateSize() {
		cachedSize = objects.stream().mapToInt(CountableObject::getAmount).sum();
		barsEnabled = QuestsConfiguration.showMobsProgressBar() && cachedSize > 0;
	}

	@Override
	protected @NotNull String descriptionLine(@NotNull PlayerAccount acc, @NotNull Source source) {
		return Utils.descriptionLines(source, buildRemainingArray(acc, source));
	}

	@Override
	protected @NotNull Supplier<Object> @NotNull [] descriptionFormat(@NotNull PlayerAccount acc, @NotNull Source source) {
		return new Supplier[] { () -> Utils.descriptionLines(source, buildRemainingArray(acc, source)) };
	}

	private @NotNull String @NotNull [] buildRemainingArray(@NotNull PlayerAccount acc, @NotNull Source source) {
		Map<UUID, Integer> playerAmounts = getPlayerRemainings(acc, true);
		if (playerAmounts == null) return new String[] { "§4§lerror" };
		String[] elements = new String[playerAmounts.size()];

		int i = 0;
		for (Entry<UUID, Integer> entry : playerAmounts.entrySet()) {
			elements[i++] = getObject(entry.getKey())
					.map(object -> QuestsConfiguration.getItemNameColor() + Utils.getStringFromNameAndAmount(
							getName(object.getObject()), QuestsConfiguration.getItemAmountColor(), entry.getValue(),
							object.getAmount(), QuestsConfiguration.showDescriptionItemsXOne(source)))
					.orElse("no object " + entry.getKey());
		}
		return elements;
	}

	@Override
	protected void initPlayerDatas(@NotNull PlayerAccount acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {
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
	public boolean event(@NotNull PlayerAccount acc, @NotNull Player p, @UnknownNullability Object object, int amount) {
		if (amount < 0) throw new IllegalArgumentException("Event amount must be positive (" + amount + ")");
		if (!canUpdate(p)) return true;

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

					updatePlayerRemaining(acc, p, playerAmounts);
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public void start(@NotNull PlayerAccount acc) {
		super.start(acc);
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
	public void joins(@NotNull PlayerAccount acc, @NotNull Player p) {
		super.joins(acc, p);
		Map<UUID, Integer> remainings = getPlayerRemainings(acc, true);
		if (remainings == null) return;
		createBar(p, remainings.values().stream().mapToInt(Integer::intValue).sum());
	}
	
	@Override
	public void leaves(@NotNull PlayerAccount acc, @NotNull Player p) {
		super.leaves(acc, p);
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

	protected abstract @NotNull String getName(@NotNull T object);

	protected abstract @NotNull Object serialize(@NotNull T object);

	protected abstract @NotNull T deserialize(@NotNull Object object);

	/**
	 * @deprecated for removal, {@link #serialize(ConfigurationSection)} should be used instead.
	 */
	@Override
	@Deprecated
	protected void serialize(Map<String, Object> map) {}
	
	@Override
	protected void serialize(@NotNull ConfigurationSection section) {
		ConfigurationSection objectsSection = section.createSection("objects");
		for (CountableObject<T> obj : objects) {
			ConfigurationSection objectSection = objectsSection.createSection(obj.getUUID().toString());
			objectSection.set("amount", obj.getAmount());
			objectSection.set("object", serialize(obj.getObject()));
		}

		Map<String, Object> serialized = new HashMap<>();
		serialize(serialized);
		Utils.setConfigurationSectionContent(section, serialized);
	}
	
	/**
	 * @deprecated for removal, {@link #deserialize(ConfigurationSection)} should be used instead.
	 */
	@Deprecated
	protected void deserialize(Map<String, Object> serializedDatas) {
		deserialize(Utils.createConfigurationSection(serializedDatas));
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
				objects.add(CountableObject.open(uuid, deserialize(serialized), objectSection.getInt("amount")));
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
			}else QuestsPlugin.getPlugin().getLoggerExpanded().warning("Amount of objects superior to max objects in " + AbstractCountableStage.this.toString() + " for player " + p.getName() + ": " + amount + " > " + cachedSize);
			bar.setTitle(Lang.MobsProgression.format(branch.getQuest().getName(), cachedSize - amount, cachedSize));
			bar.addPlayer(p);
			timer();
		}

		private void timer() {
			if (QuestsConfiguration.getProgressBarTimeout() <= 0) return;
			if (timer != null) timer.cancel();
			timer = Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
				bar.removePlayer(player);
				timer = null;
			}, QuestsConfiguration.getProgressBarTimeout() * 20L);
		}
	}

}
