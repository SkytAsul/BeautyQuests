package fr.skytasul.quests.api.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemFactory {

	public @NotNull ItemStack getPreviousPage();

	public @NotNull ItemStack getNextPage();

	public @NotNull ItemStack getCancel();

	public @NotNull ItemStack getDone();

	public @NotNull ItemStack getNotDone();

	public @NotNull ItemStack getNone();

}
