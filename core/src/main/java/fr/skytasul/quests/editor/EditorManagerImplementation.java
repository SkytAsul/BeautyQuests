package fr.skytasul.quests.editor;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.Editor;
import fr.skytasul.quests.api.editors.EditorFactory;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.utils.QuestUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorManagerImplementation implements EditorManager, Listener {

	private final @NotNull Map<Player, Editor> players = new HashMap<>();
	private final @NotNull BossBar bar;

	private @NotNull EditorFactory factory;

	public EditorManagerImplementation(@NotNull EditorFactory factory) {
		this.factory = factory;

		bar = BossBar.bossBar(Component.text("Quests Editor", NamedTextColor.GOLD), 0, BossBar.Color.YELLOW,
				BossBar.Overlay.PROGRESS);
	}

	@Override
	public <T extends Editor> T start(@NotNull T editor) {
		Player player = editor.getPlayer();
		if (isInEditor(player)) {
			Lang.ALREADY_EDITOR.send(player);
			throw new IllegalStateException(player.getName() + " is already in an editor");
		}

		players.put(player, editor);
		QuestsPlugin.getPlugin().getGuiManager().closeAndExit(player);
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.debug(player.getName() + " is entering editor " + editor.getClass().getName() + ".");

		player.sendTitle(Lang.ENTER_EDITOR_TITLE.toString(), Lang.ENTER_EDITOR_SUB.toString(), 5, 50, 5);
		player.showBossBar(bar);

		QuestUtils.autoRegister(editor);

		try {
			editor.begin();
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while beginning editor", ex);
			DefaultErrors.sendGeneric(player, "impossible to begin editor");
			editor.cancel();
		}

		return editor;
	}

	@Override
	public void leave(@NotNull Player player) {
		Editor editor = players.remove(player);
		if (editor == null)
			return;

		QuestsPlugin.getPlugin().getLoggerExpanded().debug(player.getName() + " has left the editor.");
		player.hideBossBar(bar);
		editor.end();

		QuestUtils.autoUnregister(editor);
	}

	@Override
	public void leaveAll() {
		new ArrayList<>(players.keySet()).forEach(this::leave);
	}

	@Override
	public boolean isInEditor(@NotNull Player player) {
		return players.containsKey(player);
	}

	@Override
	public @NotNull EditorFactory getFactory() {
		return factory;
	}

	@Override
	public void setFactory(@NotNull EditorFactory factory) {
		this.factory = factory;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		leave(e.getPlayer());
	}

}
