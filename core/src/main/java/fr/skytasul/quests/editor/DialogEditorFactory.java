package fr.skytasul.quests.editor;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.skytasul.quests.api.editors.Editor;
import fr.skytasul.quests.api.editors.TextEditorDialog;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;

public class DialogEditorFactory extends AbstractEditorFactory {

	@Override
	public <T> @NotNull DialogTextEditorBuilder<T> createTextEditorBuilderParser(@NotNull Player player,
			@NotNull AbstractParser<T> parser, @NotNull Runnable cancelCallback, @NotNull Consumer<T> endCallback) {
		return new DialogTextEditorBuilder<>(player, cancelCallback, endCallback, parser);
	}

	class DialogTextEditorBuilder<T> extends TextEditorBuilder<T> {

		public DialogTextEditorBuilder(@NotNull Player player, @NotNull Runnable cancelCallback,
				@NotNull Consumer<T> endCallback,
				@NotNull AbstractParser<T> parser) {
			super(player, cancelCallback, endCallback, parser);
		}

		@Override
		public Editor build() {
			return new TextEditorDialog<>(player, cancelCallback, endCallback, parser, resetCallback, resetWord, allowEmpty, allowMultiline, title, indication, initialString);
		}
	}

}
