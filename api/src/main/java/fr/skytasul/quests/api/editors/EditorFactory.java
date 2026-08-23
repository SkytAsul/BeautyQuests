package fr.skytasul.quests.api.editors;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cryptomorin.xseries.XMaterial;

import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.editors.parsers.TransparentParser;
import fr.skytasul.quests.api.npcs.BqNpc;

public interface EditorFactory {

	public @NotNull AbstractParser<XMaterial> getMaterialParser(boolean item, boolean block);

	public @NotNull Editor createNpcSelection(@NotNull Player player, @NotNull Runnable cancel,
			@NotNull Consumer<BqNpc> callback);

	public <T> @NotNull TextEditorBuilder<T> createTextEditorBuilderParser(@NotNull Player player, @NotNull AbstractParser<T> parser, @NotNull Runnable cancelCallback, @NotNull Consumer<T> endCallback);

	public default @NotNull TextEditorBuilder<String> createTextEditorBuilderString(@NotNull Player player, @NotNull Runnable cancelCallback, @NotNull Consumer<String> endCallback) {
		return createTextEditorBuilderParser(player, new TransparentParser(), cancelCallback, endCallback);
	}

	abstract class TextEditorBuilder<T> {
		protected final @NotNull Player player;
		protected final @NotNull Runnable cancelCallback;
		protected final @NotNull Consumer<T> endCallback;
		protected final @NotNull AbstractParser<T> parser;

		protected @Nullable Runnable resetCallback;
		protected @Nullable String resetWord;

		protected boolean allowEmpty = false;
		protected boolean allowMultiline = false;
		protected boolean forceMultiline = false;
		
		protected @NotNull String title = "Text Editor";
		protected @Nullable String indication = null;
		
		protected @Nullable String initialString;

		protected TextEditorBuilder(@NotNull Player player, @NotNull Runnable cancelCallback, @NotNull Consumer<T> endCallback,
				@NotNull AbstractParser<T> parser) {
			this.player = player;
			this.cancelCallback = cancelCallback;
			this.endCallback = endCallback;
			this.parser = parser;
		}

		public @NotNull TextEditorBuilder<T> allowEmpty() {
			allowEmpty = true;
			return this;
		}
		
		public @NotNull TextEditorBuilder<T> allowMultiline() {
			allowMultiline = true;
			return this;
		}
		
		/**
		 * Marks the editor as expecting multiple lines.
		 * On legacy server versions, this will result in a multiline chat editor.
		 * <p>
		 * Since multiline chat editor is clunky to use, avoid using this unless
		 * it's absolutely necessary.
		 * 
		 * @return the same builder
		 */
		public @NotNull TextEditorBuilder<T> forceMultiline() {
			// TODO delete this once chat editors are removed (1.21.8)
			forceMultiline = true;
			return this;
		}

		public @NotNull TextEditorBuilder<T> addReset(@NotNull Runnable resetCallback, @NotNull String resetWord) {
			this.resetCallback = resetCallback;
			this.resetWord = resetWord;
			return this;
		}
		
		public @NotNull TextEditorBuilder<T> setTitle(@NotNull String title) {
			this.title = title;
			return this;
		}
		
		public @NotNull TextEditorBuilder<T> setIndication(@Nullable String indication) {
			this.indication = indication;
			return this;
		}
		
		public @NotNull TextEditorBuilder<T> setInitialString(@Nullable String string) {
			this.initialString = string;
			return this;
		}
		
		public @NotNull TextEditorBuilder<T> setInitialValue(@Nullable T value) {
			if (value == null)
				this.initialString = null;
			else
				this.initialString = parser.serialize(value);
			return this;
		}

		public abstract Editor build();
		
	}
}
