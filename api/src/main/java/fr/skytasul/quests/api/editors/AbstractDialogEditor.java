package fr.skytasul.quests.api.editors;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import fr.skytasul.quests.api.QuestsPlugin;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public abstract class AbstractDialogEditor extends Editor implements Listener {
	
	protected static Component legacyToComponent(String legacyString) {
		return LegacyComponentSerializer.legacySection().deserializeOrNull(legacyString);
	}
	
	private static final AtomicInteger MONOTONIC_ID = new AtomicInteger();

	protected AbstractDialogEditor(@NotNull Player player, @NotNull Runnable cancelCallback) {
		super(player, cancelCallback);
	}

	protected final @NotNull Key createButtonKey() {
		return Key.key(QuestsPlugin.getPlugin(), "dialog/button/" + MONOTONIC_ID.getAndIncrement());
	}

	protected abstract void handleDialogClick(@NotNull PlayerCustomClickEvent event);

	@EventHandler
	public void onDialogClick(PlayerCustomClickEvent event) {
		if (event.getCommonConnection() != player.getConnection())
			return;

		player.getScheduler().run(QuestsPlugin.getPlugin(), __ -> handleDialogClick(event), null);
	}

	@Override
	public void end() {
		super.end();
		player.closeDialog();
	}

}
