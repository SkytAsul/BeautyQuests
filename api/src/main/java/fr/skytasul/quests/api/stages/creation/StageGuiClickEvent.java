package fr.skytasul.quests.api.stages.creation;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StageGuiClickEvent {

	private final @NotNull Player player;
	private final @NotNull ItemStack clicked;
	private final @NotNull ClickType click;
	private final @NotNull StageCreationContext<?> context;

	public StageGuiClickEvent(@NotNull Player player, @NotNull ItemStack clicked, @NotNull ClickType click,
			@NotNull StageCreationContext<?> context) {
		this.player = player;
		this.clicked = clicked;
		this.click = click;
		this.context = context;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public @NotNull ItemStack getClicked() {
		return clicked;
	}

	public @NotNull ClickType getClick() {
		return click;
	}

	public void reopen() {
		context.reopenGui();
	}

	public void remove() {
		context.remove();
	}

	public void removeAndReopen() {
		context.removeAndReopenGui();
	}

}
