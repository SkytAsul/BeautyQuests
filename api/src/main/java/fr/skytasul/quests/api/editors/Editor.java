package fr.skytasul.quests.api.editors;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.AutoRegistered;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@AutoRegistered
public abstract class Editor {

	protected final @NotNull Player player;
	protected final @NotNull Runnable cancel;
	private boolean started = false;

	protected Editor(@NotNull Player player, @NotNull Runnable cancel) {
		this.player = player;
		this.cancel = cancel;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public void begin() {
		if (started)
			throw new IllegalStateException("Editor already started");

		started = true;
	}

	public void end() {
		if (!started)
			throw new IllegalStateException("Editor did not started");

		started = false;
	}

	public final void start() {
		QuestsPlugin.getPlugin().getEditorManager().start(this);
	}

	public final void stop() {
		QuestsPlugin.getPlugin().getEditorManager().stop(this);
	}

	public final void cancel() {
		QuestsPlugin.getPlugin().getEditorManager().stop(this);
		cancel.run();
	}

}
