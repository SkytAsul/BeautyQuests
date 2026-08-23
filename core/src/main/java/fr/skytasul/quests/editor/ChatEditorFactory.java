package fr.skytasul.quests.editor;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.skytasul.quests.api.editors.Editor;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.TextListEditor;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.TransparentParser;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;

public class ChatEditorFactory extends AbstractEditorFactory {

	@Override
	public <T> @NotNull ChatTextEditorBuilder<T> createTextEditorBuilderParser(@NotNull Player player,
			@NotNull AbstractParser<T> parser, @NotNull Runnable cancelCallback, @NotNull Consumer<T> endCallback) {
		return new ChatTextEditorBuilder<>(player, cancelCallback, endCallback, parser);
	}

	class ChatTextEditorBuilder<T> extends TextEditorBuilder<T> {

		public ChatTextEditorBuilder(@NotNull Player player, @NotNull Runnable cancelCallback,
				@NotNull Consumer<T> endCallback,
				@NotNull AbstractParser<T> parser) {
			super(player, cancelCallback, endCallback, parser);
		}

		@Override
		public Editor build() {
			if (indication != null)
				MessageUtils.sendMessage(player, indication, MessageType.DefaultMessageType.PREFIXED);
			if (allowMultiline && forceMultiline) {
				if (!(parser instanceof TransparentParser)) throw new UnsupportedOperationException();
				var endCallback = (Consumer<String>) super.endCallback;
				return new TextListEditor(player, cancelCallback, stringList -> endCallback.accept(String.join("\n", stringList)),
						initialString == null ? List.of() : List.of(initialString.split("\n")));
			} else {
				return new TextEditor<>(player, cancelCallback, endCallback, resetCallback, resetWord, parser);
			}
		}
	}

}
