package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface EditorManager {

	public <T extends Editor> T start(@NotNull T editor);

	public default void stop(@NotNull Editor editor) {
		leave(editor.getPlayer());
	}

	public void leave(@NotNull Player player);

	public void leaveAll();

	public boolean isInEditor(@NotNull Player player);

	public @NotNull EditorFactory getFactory();

	public void setFactory(@NotNull EditorFactory factory);

}
