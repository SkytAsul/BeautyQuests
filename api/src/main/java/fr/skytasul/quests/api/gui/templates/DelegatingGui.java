package fr.skytasul.quests.api.gui.templates;

import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DelegatingGui implements Gui {

	protected abstract @NotNull Gui getDelegate();

	@Override
	public @Nullable Inventory getInventory() {
		return getDelegate().getInventory();
	}

	@Override
	public void showInternal(@NotNull Player player) {
		getDelegate().showInternal(player);
	}

	@Override
	public void refresh(@NotNull Player player) {
		getDelegate().refresh(player);
	}

	@Override
	public void onClick(@NotNull GuiClickEvent event) {
		getDelegate().onClick(event);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return getDelegate().onClose(player);
	}

}
