package fr.skytasul.quests.api.utils.itemdescription;

import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.itemdescription.HasItemsDescriptionConfiguration.HasMultipleObjects;
import fr.skytasul.quests.api.utils.itemdescription.HasItemsDescriptionConfiguration.HasSingleObject;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.Placeholder;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.PlayerPlaceholdersContext;

public final class ItemsDescriptionPlaceholders {

	private static final PlaceholderRegistry DESCRIPTION_REGISTRY = new PlaceholderRegistry()
			.registerIndexedContextual("remaining", DescriptionPlaceholderContext.class,
					context -> Integer.toString(context.getItem().getPlayerAmount(context.getPlayerAccount())))
			.registerIndexedContextual("done", DescriptionPlaceholderContext.class,
					context -> Integer.toString(context.getItem().getObjectAmount()
							- context.getItem().getPlayerAmount(context.getPlayerAccount())))
			.registerIndexedContextual("amount", DescriptionPlaceholderContext.class,
					context -> Integer.toString(context.getItem().getObjectAmount()))
			.registerIndexedContextual("percentage", DescriptionPlaceholderContext.class,
					context -> Integer.toString((int) (context.getItem().getPlayerAmount(context.getPlayerAccount()) * 100D
							/ context.getItem().getObjectAmount())))
			.registerIndexedContextual("name", DescriptionPlaceholderContext.class,
					context -> context.getItem().getObjectName());

	private ItemsDescriptionPlaceholders() {}

	public static void register(@NotNull PlaceholderRegistry placeholders, @NotNull String key,
			@NotNull HasSingleObject object) {
		placeholders.registerIndexedContextual(key, PlayerPlaceholdersContext.class,
				context -> formatObject(object, context));

		placeholders.register(Placeholder.ofPatternContextual(key + "_(remaining|done|amount|percentage|name)",
				PlayerPlaceholdersContext.class, (matcher, context) -> {
					return DESCRIPTION_REGISTRY.resolve(matcher.group(1), new DescriptionPlaceholderContext(
							context.getActor(), context.replacePluginPlaceholders(), object));
				}));
	}

	public static <T> void register(@NotNull PlaceholderRegistry placeholders, @NotNull String key,
			@NotNull HasMultipleObjects<T> objects) {
		placeholders.registerIndexedContextual(key, StageDescriptionPlaceholdersContext.class, context -> {
			Map<CountableObject<T>, Integer> amounts = objects.getPlayerAmounts(context.getPlayerAccount());
			String[] objectsDescription = amounts.entrySet().stream()
					.map(entry -> formatObject(buildFrom(objects, entry.getKey(), entry.getValue()), context))
					.toArray(String[]::new);
			return formatObjectList(context.getDescriptionSource(), objects.getItemsDescriptionConfiguration(),
					objectsDescription);
		});

		placeholders.register(Placeholder.ofPatternContextual(key + "_(remaining|done|amount|percentage)",
				PlayerPlaceholdersContext.class, (matcher, context) -> {
					return DESCRIPTION_REGISTRY.resolve(matcher.group(1), new DescriptionPlaceholderContext(
							context.getActor(), context.replacePluginPlaceholders(), objects.asTotalObject()));
				}));

		placeholders.register(Placeholder.ofPatternContextual(key + "_(\\d+)(?:_(remaining|done|amount|percentage))?",
				PlayerPlaceholdersContext.class, (matcher, context) -> {
					int index = Integer.parseInt(matcher.group(1));
					CountableObject<T> object = objects.getObject(index);
					if (object == null)
						return Lang.Unknown.toString();
					HasSingleObject item =
							buildFrom(objects, object, objects.getPlayerAmount(context.getPlayerAccount(), object));

					String operation = matcher.group(2);
					if (operation == null)
						return formatObject(item, context);
					
					return DESCRIPTION_REGISTRY.resolve(operation, new DescriptionPlaceholderContext(context.getActor(),
							context.replacePluginPlaceholders(), item));
				}));
	}

	private static <T> @NotNull HasSingleObject buildFrom(@NotNull HasMultipleObjects<T> objects, CountableObject<T> object,
			int amount) {
		return new HasSingleObject() {
			@Override
			public @NotNull ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
				return objects.getItemsDescriptionConfiguration();
			}

			@Override
			public int getPlayerAmount(@NotNull PlayerAccount account) {
				return amount;
			}

			@Override
			public @NotNull String getObjectName() {
				return objects.getObjectName(object);
			}

			@Override
			public int getObjectAmount() {
				return object.getAmount();
			}
		};
	}

	public static @NotNull String formatObject(@NotNull HasSingleObject object, @NotNull PlayerPlaceholdersContext context) {
		String formatString = object.getPlayerAmount(context.getPlayerAccount()) > 1
				? object.getItemsDescriptionConfiguration().getMultipleItemsFormat()
				: object.getItemsDescriptionConfiguration().getSingleItemFormat();
		return MessageUtils.format(formatString, DESCRIPTION_REGISTRY,
				new DescriptionPlaceholderContext(context.getActor(), context.replacePluginPlaceholders(), object));
	}

	public static @NotNull String formatObjectList(@NotNull DescriptionSource source,
			@NotNull ItemsDescriptionConfiguration configuration, @NotNull String @NotNull... elements) {
		if (elements.length == 0)
			return Lang.Unknown.toString();
		if ((elements.length == 1 && configuration.isAloneSplitInlined()) || configuration.isSourceSplit(source))
			return MessageUtils.itemsToFormattedString(elements, "§r");
		return String.join(configuration.getSplitPrefix(), elements);
	}

	private static class DescriptionPlaceholderContext implements PlayerPlaceholdersContext {

		private final @NotNull Player player;
		private final boolean replacePluginPlaceholders;
		private final @NotNull HasSingleObject item;

		public DescriptionPlaceholderContext(@NotNull Player player, boolean replacePluginPlaceholders,
				@NotNull HasSingleObject item) {
			this.player = player;
			this.replacePluginPlaceholders = replacePluginPlaceholders;
			this.item = item;
		}

		@Override
		public @NotNull Player getActor() {
			return player;
		}

		@Override
		public boolean replacePluginPlaceholders() {
			return replacePluginPlaceholders;
		}

		public @NotNull HasSingleObject getItem() {
			return item;
		}

	}

}
