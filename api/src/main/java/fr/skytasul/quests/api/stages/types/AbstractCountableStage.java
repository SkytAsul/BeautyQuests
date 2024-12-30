package fr.skytasul.quests.api.stages.types;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import fr.skytasul.quests.api.utils.progress.itemdescription.HasItemsDescriptionConfiguration.HasMultipleObjects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class AbstractCountableStage<T> extends AbstractStage implements HasMultipleObjects<T> {

	protected final @NotNull List<@NotNull CountableObject<T>> objects;

	protected AbstractCountableStage(@NotNull StageController controller,
			@NotNull List<@NotNull CountableObject<T>> objects) {
		super(controller);
		this.objects = objects;
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

	protected @NotNull Map<@NotNull UUID, @NotNull Integer> getRawRemainingAmounts(@NotNull Quester quester,
			boolean warnNull) {
		Map<?, Integer> remaining = getData(quester, "remaining");
		if (warnNull && remaining == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(
					"Cannot retrieve remaining amounts for " + quester.getNameAndID() + " on " + controller.toString(),
					"datas" + quester.getNameAndID() + controller.toString(), 10);
		}

		if (remaining == null || remaining.isEmpty())
			return Collections.emptyMap();

		Object object = remaining.keySet().iterator().next();
		if (object instanceof Integer) {
			// datas before migration
			Map<UUID, Integer> newRemaining = new HashMap<>(remaining.size());
			Map<String, Integer> dataMap = new HashMap<>(remaining.size());
			remaining.forEach((key, amount) -> {
				UUID uuid = uuidFromLegacyIndex((Integer) key);
				if (!getObject(uuid).isPresent()) {
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.warning("Cannot migrate " + quester.getNameAndID() + " data for stage " + toString()
							+ " as there is no migrated data for object " + key);
				}
				newRemaining.put(uuid, amount);
				dataMap.put(uuid.toString(), amount);
			});
			updateObjective(quester, "remaining", dataMap);
			return newRemaining;
		} else if (object instanceof String) {
			// datas stored as string
			return remaining.entrySet().stream()
					.collect(Collectors.toMap(entry -> UUID.fromString((String) entry.getKey()), Entry::getValue));
		} else
			throw new UnsupportedOperationException(object.getClass().getName());
	}

	@Override
	public @NotNull Map<CountableObject<T>, Integer> getRemainingAmounts(@NotNull Quester quester) {
		return getRawRemainingAmounts(quester, false)
				.entrySet().stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(getObject(entry.getKey()).orElse(null), entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public @Nullable CountableObject<T> getObject(int index) {
		return index >= objects.size() ? null : objects.get(index);
	}

	@Override
	public long getRemainingAmount(@NotNull Quester quester, CountableObject<T> object) {
		// we do not use default implementation in HasMultipleObjects to avoid conversion from UUID to
		// CountableObject
		return getRawRemainingAmounts(quester, false).get(object.getUUID());
	}

	@Override
	public long getRemainingAmount(@NotNull Quester quester) {
		// same as in getPlayerAmount
		return getRawRemainingAmounts(quester, false).values().stream().mapToInt(Integer::intValue).sum();
	}

	@Override
	public long getTotalAmount() {
		return objects.stream().mapToInt(CountableObject::getAmount).sum();
	}

	@Override
	public @NotNull String getObjectName(CountableObject<T> object) {
		return getName(object.getObject());
	}

	protected void updateRemaining(@NotNull Quester quester, @NotNull Map<@NotNull UUID, @NotNull Integer> remaining) {
		updateObjective(quester, "remaining", remaining.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toString(), Entry::getValue)));
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		ProgressPlaceholders.registerObjects(placeholders, getPlaceholderKey(), this);
	}

	@Override
	public void initPlayerDatas(@NotNull Quester acc, @NotNull Map<@NotNull String, @Nullable Object> datas) {
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
		if (!canUpdate(p) || !hasStarted(p))
			return true;

		for (Quester quester : controller.getApplicableQuesters(p)) {
			for (CountableObject<T> countableObject : objects) {
				if (objectApplies(countableObject.getObject(), object)) {
					Map<UUID, Integer> playerAmounts = getRawRemainingAmounts(quester, true);
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
						finishStage(quester);
						return true;
					} else {
						updateRemaining(quester, playerAmounts);
						return false;
					}
				}
			}
		}
		return false;
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
	}

	private static UUID uuidFromLegacyIndex(int index) { // useful for migration purpose
		return new UUID(index, 2478L);
		// 2478 is a magic value, the only necessity is that it stays constant
		// and I like the number 2478
	}

}
