package fr.skytasul.quests.stages;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.TransparentParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StageChat extends AbstractStage implements Listener {

	private final String text;

	private final Pattern compiledText;
	private final boolean isCommand;

	private final boolean cancel;
	private final boolean ignoreCase;
	private final boolean placeholders;
	private final boolean isRegex;

	public StageChat(StageController controller, String text, boolean cancel, boolean ignoreCase, boolean placeholders,
			boolean isRegex) {
		super(controller);

		Validate.notNull(text, "Text cannot be null");
		this.text = text;
		this.cancel = cancel;
		this.ignoreCase = ignoreCase;
		this.placeholders = placeholders;
		this.isRegex = isRegex;

		this.isCommand = text.startsWith("/");
		this.compiledText = isRegex ? Pattern.compile(text) : null;
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("text", text);
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_CHAT.toString();
	}

	public String getText() {
		return text;
	}

	public boolean isEventCancelled() {
		return cancel;
	}

	public boolean isCaseIgnored() {
		return ignoreCase;
	}

	@EventHandler
	public void onTchat(AsyncPlayerChatEvent e) {
		if (isCommand)
			return;
		if (check(e.getMessage(), e.getPlayer()) && cancel) e.setCancelled(true);
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (!isCommand)
			return;
		if (check(e.getMessage(), e.getPlayer()) && cancel) e.setCancelled(true);
	}

	private boolean check(String message, Player p) {
		if (!hasApplicableQuester(p))
			return false;

		if (placeholders)
			message = MessageUtils.finalFormat(message, null, PlaceholdersContext.of(p, true, null));

		if (isRegex) {
			if (!compiledText.matcher(message).matches())
				return false;
		} else if (ignoreCase) {
			if (!message.equalsIgnoreCase(text))
				return false;
		} else {
			if (!message.equals(text))
				return false;
		}

		if (!matchesRequirements(p))
			return false;

		boolean hasQuester = false;
		for (var quester : controller.getApplicableQuesters(p)) {
			hasQuester = true;
			finishStage(quester);
		}
		return hasQuester;
	}


	@Override
	public void serialize(ConfigurationSection section) {
		Validate.notNull(text, "Text cannot be null");
		section.set("writeText", text);
		if (!cancel) section.set("cancel", false);
		if (ignoreCase) section.set("ignoreCase", true);
		if (!placeholders) section.set("placeholders", false);
		if (isRegex)
			section.set("isRegex", isRegex);
	}

	public static StageChat deserialize(ConfigurationSection section, StageController controller) {
		return new StageChat(controller, section.getString("writeText"), section.getBoolean("cancel", true),
				section.getBoolean("ignoreCase", false), section.getBoolean("placeholders", true),
				section.getBoolean("isRegex", false));
	}

	public static class Creator extends StageCreation<StageChat> {

		private static final int MESSAGE_SLOT = 6;
		private static final int CANCEL_EVENT_SLOT = 7;
		private static final int PLACEHOLDERS_SLOT = 8;
		private static final int IGNORE_CASE_SLOT = 9;
		private static final int IS_REGEX_SLOT = 10;

		private String text;
		private boolean placeholders = true;
		private boolean cancel = true;
		private boolean ignoreCase = false;
		private boolean isRegex = false;

		public Creator(@NotNull StageCreationContext<StageChat> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.setItem(PLACEHOLDERS_SLOT, ItemUtils.itemSwitch(Lang.placeholders.toString(), placeholders),
					event -> setPlaceholders(!placeholders));
			line.setItem(IGNORE_CASE_SLOT, ItemUtils.itemSwitch(Lang.ignoreCase.toString(), ignoreCase),
					event -> setIgnoreCase(!ignoreCase));
			line.setItem(CANCEL_EVENT_SLOT, ItemUtils.itemSwitch(Lang.cancelEvent.toString(), cancel),
					event -> setCancel(!cancel));
			line.setItem(IS_REGEX_SLOT,
					ItemUtils.itemSwitch(Lang.stageChatIsRegex.toString(), isRegex,
							QuestOption.formatDescription(Lang.stageChatIsRegexDescription.toString())),
					event -> setIsRegex(!isRegex));
			line.setItem(MESSAGE_SLOT, ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.editMessage.toString()),
					event -> launchEditor(event.getPlayer()));
		}

		public void setText(String text) {
			this.text = text;
			getLine().refreshItem(MESSAGE_SLOT, item -> ItemUtils.lore(item, QuestOption.formatNullableValue(text)));
		}

		public void setPlaceholders(boolean placeholders) {
			if (this.placeholders != placeholders) {
				this.placeholders = placeholders;
				getLine().refreshItem(PLACEHOLDERS_SLOT, item -> ItemUtils.setSwitch(item, placeholders));
			}
		}

		public void setIgnoreCase(boolean ignoreCase) {
			if (this.ignoreCase != ignoreCase) {
				this.ignoreCase = ignoreCase;
				getLine().refreshItem(IGNORE_CASE_SLOT, item -> ItemUtils.setSwitch(item, ignoreCase));
			}
		}

		public void setCancel(boolean cancel) {
			if (this.cancel != cancel) {
				this.cancel = cancel;
				getLine().refreshItem(CANCEL_EVENT_SLOT, item -> ItemUtils.setSwitch(item, cancel));
			}
		}

		public void setIsRegex(boolean isRegex) {
			if (this.isRegex == isRegex)
				return;

			this.isRegex = isRegex;
			getLine().refreshItem(IS_REGEX_SLOT, item -> ItemUtils.setSwitch(item, isRegex));

			if (isRegex) {
				try {
					Pattern.compile(text);
				} catch (PatternSyntaxException ex) {
					setText(Pattern.quote(text));
				}
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			launchEditor(p);
		}

		@Override
		public void edit(StageChat stage) {
			super.edit(stage);
			setText(stage.text);
			setIgnoreCase(stage.ignoreCase);
			setCancel(stage.cancel);
			setPlaceholders(stage.placeholders);
			setIsRegex(stage.isRegex);
		}

		@Override
		public StageChat finishStage(StageController controller) {
			return new StageChat(controller, text, cancel, ignoreCase, placeholders, isRegex);
		}

		private void launchEditor(Player p) {
			var parser = !isRegex ? new TransparentParser() : new AbstractParser<String>() {
				@Override
				public @NotNull String parse(@NotNull String string) throws ParsingError {
					try {
						Pattern.compile(string.replace("{SLASH}", "/"));
					} catch (PatternSyntaxException ex) {
						throw new ParsingError(Lang.INVALID_PATTERN.format(PlaceholderRegistry.of("input", string)));
					}
					return string;
				}

				@Override
				public @Nullable String getIndication() {
					return Lang.TEXT_PARSER_REGEX.toString();
				}
			};

			QuestsPlugin.getPlugin().getEditorManager().getFactory().createTextEditorBuilderParser(p, parser,
					text == null ? context::removeAndReopenGui : context::reopenGui, string -> {
						setText(string.replace("{SLASH}", "/"));
						context.reopenGui();
					})
					.setIndication(Lang.CHAT_MESSAGE.toString())
					.setInitialString(text)
					.build()
					.start();
		}
	}

}
