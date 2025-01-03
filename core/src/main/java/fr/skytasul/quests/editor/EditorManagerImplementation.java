package fr.skytasul.quests.editor;

import fr.skytasul.quests.api.BossBarManager.BQBossBar;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.Editor;
import fr.skytasul.quests.api.editors.EditorFactory;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorManagerImplementation implements EditorManager, Listener {

	private final @NotNull Map<Player, Editor> players = new HashMap<>();
	private final @Nullable BQBossBar bar;

	private @NotNull EditorFactory factory;

	public EditorManagerImplementation() {
		if (QuestsAPI.getAPI().hasBossBarManager()) {
			bar = QuestsAPI.getAPI().getBossBarManager().buildBossBar("§6Quests Editor", "YELLOW", "SOLID");
			bar.setProgress(0);
		} else {
			bar = null;
		}

		setFactory(new DefaultEditorFactory());
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

		if (MinecraftVersion.MAJOR > 11) {
			player.sendTitle(Lang.ENTER_EDITOR_TITLE.toString(), Lang.ENTER_EDITOR_SUB.toString(), 5, 50, 5);
		} else {
			Lang.ENTER_EDITOR_TITLE.send(player);
			Lang.ENTER_EDITOR_SUB.send(player);
		}
		if (bar != null)
			bar.addPlayer(player);

		QuestUtils.autoRegister(editor);

		try {
			editor.begin();
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while beginning editor", ex);
			DefaultErrors.sendGeneric(QuestsPlugin.getPlugin().getAudiences().player(player), "impossible to begin editor");
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
		if (bar != null)
			bar.removePlayer(player);
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

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onChat(AsyncPlayerChatEvent e) {
		Editor editor = players.get(e.getPlayer());
		if (editor == null)
			return;

		e.setCancelled(true);
		QuestUtils.runOrSync(() -> editor.callChat(e.getMessage()));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		leave(e.getPlayer());
	}

}
