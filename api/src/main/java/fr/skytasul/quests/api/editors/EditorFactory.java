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

		public abstract Editor build();
		
	}
}
