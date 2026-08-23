package fr.skytasul.quests.api.quests.creation;

import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.options.QuestOption;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestCreationGuiClickEvent extends GuiClickEvent {

	private final @NotNull QuestOption<?> option;

	public QuestCreationGuiClickEvent(@NotNull Player player, @NotNull QuestCreationGui gui, @Nullable ItemStack clicked,
			@Nullable ItemStack cursor, int slot, @NotNull ClickType click, @NotNull QuestOption<?> option) {
		super(player, gui, clicked, cursor, slot, click);
		this.option = option;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @NotNull QuestCreationGui getGui() {
		return (@NotNull QuestCreationGui) super.getGui();
	}

	public void refreshItem() {
		getGui().updateOptionItem(option);
	}

}
