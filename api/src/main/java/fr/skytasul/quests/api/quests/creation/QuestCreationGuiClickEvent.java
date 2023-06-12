package fr.skytasul.quests.api.quests.creation;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.gui.GuiClickEvent;

public class QuestCreationGuiClickEvent extends GuiClickEvent {

	public QuestCreationGuiClickEvent(@NotNull Player player, @NotNull QuestCreationGui gui, @Nullable ItemStack clicked,
			@Nullable ItemStack cursor, int slot, @NotNull ClickType click) {
		super(player, gui, clicked, cursor, slot, click);
	}

	@Override
	public void setCancelled(boolean cancelled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull QuestCreationGui getGui() {
		return (@NotNull QuestCreationGui) super.getGui();
	}

}
