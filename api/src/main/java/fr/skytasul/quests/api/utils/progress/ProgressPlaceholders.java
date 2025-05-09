package fr.skytasul.quests.api.utils.progress;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.Placeholder;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.QuesterPlaceholdersContext;
import fr.skytasul.quests.api.utils.progress.itemdescription.HasItemsDescriptionConfiguration.HasMultipleObjects;
import fr.skytasul.quests.api.utils.progress.itemdescription.HasItemsDescriptionConfiguration.HasSingleObject;
import fr.skytasul.quests.api.utils.progress.itemdescription.ItemsDescriptionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

public final class ProgressPlaceholders {

	private static final PlaceholderRegistry PROGRESS_REGISTRY = new PlaceholderRegistry()
			.registerIndexedContextual("remaining", ProgressPlaceholderContext.class,
					context -> Long.toString(context.getProgress().getRemainingAmount(context.getQuester())))
			.registerIndexedContextual("done", ProgressPlaceholderContext.class,
					context -> Long.toString(context.getProgress().getTotalAmount()
							- context.getProgress().getRemainingAmount(context.getQuester())))
			.registerIndexedContextual("total", ProgressPlaceholderContext.class,
					context -> Long.toString(context.getProgress().getTotalAmount()))
			.registerIndexedContextual("percentage", ProgressPlaceholderContext.class,
					context -> {
						long perc = (long) (100D - context.getProgress().getRemainingAmount(context.getQuester()) * 100D
								/ context.getProgress().getTotalAmount());
						return Long.toString(perc);
					});
	private static final PlaceholderRegistry DESCRIPTION_REGISTRY = PROGRESS_REGISTRY.with(new PlaceholderRegistry()
			.registerIndexedContextual("name", ProgressObjectPlaceholderContext.class,
					context -> context.getProgress().getObjectName()));

	private ProgressPlaceholders() {}

	public static void registerProgress(@NotNull PlaceholderRegistry placeholders, @NotNull String key,
			@NotNull HasProgress progress) {
		placeholders.register(Placeholder.ofPatternContextual(key + "_(remaining|done|total|percentage)",
				QuesterPlaceholdersContext.class, (matcher, context) -> {
					return PROGRESS_REGISTRY.resolve(matcher.group(1), new ProgressPlaceholderContext(
							context.getQuester(), context.replacePluginPlaceholders(), progress));
				}));
	}

	public static void registerObject(@NotNull PlaceholderRegistry placeholders, @NotNull String key,
			@NotNull HasSingleObject object) {
		placeholders.registerIndexedContextual(key, QuesterPlaceholdersContext.class,
				context -> formatObject(object, context));

		placeholders.register(Placeholder.ofPatternContextual(key + "_(remaining|done|total|percentage|name)",
				QuesterPlaceholdersContext.class, (matcher, context) -> {
					return DESCRIPTION_REGISTRY.resolve(matcher.group(1), new ProgressObjectPlaceholderContext(
							context.getQuester(), context.replacePluginPlaceholders(), object));
				}));
	}

	public static <T> void registerObjects(@NotNull PlaceholderRegistry placeholders, @NotNull String key,
			@NotNull HasMultipleObjects<T> objects) {
		registerProgress(placeholders, key, objects);

		placeholders.registerIndexedContextual(key, StageDescriptionPlaceholdersContext.class, context -> {
			Map<CountableObject<T>, Integer> amounts = objects.getRemainingAmounts(context.getQuester());
			String[] objectsDescription = amounts.entrySet().stream()
					.map(entry -> formatObject(buildFrom(objects, entry.getKey(), entry.getValue()), context))
					.toArray(String[]::new);
			return formatObjectList(context.getDescriptionSource(), objects.getItemsDescriptionConfiguration(),
					objectsDescription);
		});

		placeholders.register(Placeholder.ofPatternContextual(key + "_(\\d+)(?:_(remaining|done|total|percentage))?",
				QuesterPlaceholdersContext.class, (matcher, context) -> {
					int index = Integer.parseInt(matcher.group(1));
					CountableObject<T> object = objects.getObject(index);
					if (object == null)
						return Lang.Unknown.toString();
					HasSingleObject item =
							buildFrom(objects, object, objects.getRemainingAmount(context.getQuester(), object));

					String operation = matcher.group(2);
					if (operation == null)
						return formatObject(item, context);

					return DESCRIPTION_REGISTRY.resolve(operation, new ProgressObjectPlaceholderContext(context.getQuester(),
							context.replacePluginPlaceholders(), item));
				}));
	}

	private static <T> @NotNull HasSingleObject buildFrom(@NotNull HasMultipleObjects<T> objects, CountableObject<T> object,
			long amount) {
		return new HasSingleObject() {
			@Override
			public @NotNull ItemsDescriptionConfiguration getItemsDescriptionConfiguration() {
				return objects.getItemsDescriptionConfiguration();
			}

			@Override
			public long getRemainingAmount(@NotNull Quester quester) {
				return amount;
			}

			@Override
			public @NotNull String getObjectName() {
				return objects.getObjectName(object);
			}

			@Override
			public long getObjectAmount() {
				return object.getAmount();
			}
		};
	}

	public static @NotNull String formatObject(@NotNull HasSingleObject object,
			@NotNull QuesterPlaceholdersContext context) {
		String formatString = object.getRemainingAmount(context.getQuester()) > 1
				? object.getItemsDescriptionConfiguration().getMultipleItemsFormat()
				: object.getItemsDescriptionConfiguration().getSingleItemFormat();
		return MessageUtils.format(formatString, DESCRIPTION_REGISTRY,
				new ProgressObjectPlaceholderContext(context.getQuester(), context.replacePluginPlaceholders(), object));
	}

	public static @NotNull String formatObjectList(@NotNull DescriptionSource source,
			@NotNull ItemsDescriptionConfiguration configuration, @NotNull String @NotNull... elements) {
		if (elements.length == 0)
			return Lang.Unknown.toString();
		if (elements.length == 1 && configuration.isAloneSplitInlined())
			return elements[0];
		if (configuration.isSourceSplit(source))
			return configuration.getSplitPrefix() + String.join(configuration.getSplitPrefix(), elements);
		return MessageUtils.itemsToFormattedString(elements, "§r");
	}

	private static class ProgressPlaceholderContext implements QuesterPlaceholdersContext {

		private final @NotNull Quester quester;
		private final boolean replacePluginPlaceholders;
		private final @NotNull HasProgress progress;

		public ProgressPlaceholderContext(@NotNull Quester quester, boolean replacePluginPlaceholders,
				@NotNull HasProgress progress) {
			this.quester = quester;
			this.replacePluginPlaceholders = replacePluginPlaceholders;
			this.progress = progress;
		}

		@Override
		public @NotNull Quester getQuester() {
			return quester;
		}

		@Override
		public boolean replacePluginPlaceholders() {
			return replacePluginPlaceholders;
		}

		public @NotNull HasProgress getProgress() {
			return progress;
		}

		@Override
		public @Nullable MessageType getMessageType() {
			return null;
		}

	}

	private static class ProgressObjectPlaceholderContext extends ProgressPlaceholderContext {

		public ProgressObjectPlaceholderContext(@NotNull Quester quester, boolean replacePluginPlaceholders,
				@NotNull HasSingleObject object) {
			super(quester, replacePluginPlaceholders, object);
		}

		@Override
		public @NotNull HasSingleObject getProgress() {
			return (@NotNull HasSingleObject) super.getProgress();
		}

	}

}
