package fr.skytasul.quests.api.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.AbstractParser.ParsingError;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput.MultilineOptions;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public class TextEditorDialog<T> extends AbstractDialogEditor {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.TextEditorDialog");

	private static final String INPUT_KEY = "input";

	private final Key validateButton = Key.key(QuestsPlugin.getPlugin(), hashCode() + "/validate");
	private final Key cancelButton = Key.key(QuestsPlugin.getPlugin(), hashCode() + "/cancel");
	private final Key resetButton = Key.key(QuestsPlugin.getPlugin(), hashCode() + "/reset");

	private final @NotNull Consumer<T> endCallback;
	private final @NotNull AbstractParser<T> parser;
	private final @Nullable Runnable resetCallback;
	private final boolean allowEmpty;
	private final boolean allowMultiline;
	private final @NotNull String title;
	private final @Nullable String indication;
	
	private @Nullable String initialValue; // this one is not final on purpose

	public TextEditorDialog(@NotNull Player player, @NotNull Runnable cancelCallback, @NotNull Consumer<T> endCallback,
			@NotNull AbstractParser<T> parser, @Nullable Runnable resetCallback, boolean allowEmpty,
			boolean allowMultiline, @NotNull String title, @Nullable String indication, @Nullable String initialValue) {
		super(player, cancelCallback);
		this.endCallback = Objects.requireNonNull(endCallback);
		this.parser = Objects.requireNonNull(parser);
		this.resetCallback = resetCallback;
		this.allowEmpty = allowEmpty;
		this.allowMultiline = allowMultiline;
		this.title = Objects.requireNonNull(title);
		this.indication = indication;
		this.initialValue = initialValue;
	}

	@Override
	public void begin() {
		super.begin();
		showDialog(null);
	}

	private void showDialog(@Nullable String errorMessage) {
		var bodies = new ArrayList<DialogBody>();
		if (errorMessage != null)
			bodies.add(DialogBody.plainMessage(legacyToComponent(errorMessage)));
		if (indication != null)
			bodies.add(DialogBody.plainMessage(legacyToComponent(indication)));
		if (parser != null) {
			var parserIndication = parser.getIndication();
			if (parserIndication != null)
				bodies.add(DialogBody.plainMessage(legacyToComponent(parserIndication)));
		}

		var buttons = new ArrayList<ActionButton>();
		buttons.add(ActionButton.builder(Component.text("Validate"))
				.action(DialogAction.customClick(validateButton, null)).build());
		if (resetCallback != null)
			buttons.add(ActionButton.builder(Component.text("Reset"))
					.action(DialogAction.customClick(resetButton, null)).build());

		var dialog = Dialog.create(builder -> builder.empty()
				.base(DialogBase.builder(legacyToComponent(title))
						.body(bodies)
						.inputs(List.of(
								DialogInput.text(INPUT_KEY, Component.text("input label"))
										.labelVisible(false)
										.initial(initialValue == null ? "" : initialValue)
										.maxLength(1000)
										.width(300)
										.multiline(allowMultiline ? MultilineOptions.create(null, 150) : null)
										.build()))
						.canCloseWithEscape(false)
						.build())
				.type(DialogType.multiAction(
						buttons,
						ActionButton.builder(Component.text("Cancel"))
								.action(DialogAction.customClick(cancelButton, null)).build(),
						2)));

		player.showDialog(dialog);
	}

	private void finish(T value) {
		stop();
		endCallback.accept(value);
	}

	private void reset() {
		stop();
		resetCallback.run();
	}

	@Override
	protected void handleDialogClick(@NotNull PlayerCustomClickEvent event) {
		if (event.getIdentifier().equals(validateButton)) {
			String text = event.getDialogResponseView().getText(INPUT_KEY);

			if (text == null || text == "") {
				if (allowEmpty) {
					finish(null);
				} else {
					showDialog(Lang.CANNOT_BE_EMPTY.toString());
				}
			} else {
				try {
					initialValue = text;
					// This way, if we reopen the dialog due to a parsing error,
					// the player does not loose the text it sent before.

					T parsedValue = parser.parse(text);
					finish(parsedValue);
				} catch (ParsingError ex) {
					showDialog(ex.getMessage());
				} catch (Exception ex) {
					LOGGER.severe("Failure to parse text: {0}", ex, text);
					showDialog("Unhandled error");
				}
			}
		} else if (event.getIdentifier().equals(cancelButton)) {
			cancel();
		} else if (event.getIdentifier().equals(resetButton)) {
			reset();
		}
	}

}
