package fr.skytasul.quests.api.utils;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.localization.Locale;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PlayerListCategory {

	FINISHED(
			1,
			Lang.finisheds),
	IN_PROGRESS(
			2,
			Lang.inProgress),
	NOT_STARTED(
			3,
			Lang.notStarteds);

	private final int slot;
	private final @NotNull Locale name;
	private @NotNull ItemStack icon = XMaterial.BARRIER.parseItem();
	private @Nullable DyeColor color = DyeColor.RED;

	private PlayerListCategory(int slot, @NotNull Locale name) {
		this.slot = slot;
		this.name = name;
	}

	public int getSlot() {
		return slot;
	}

	public @NotNull String getName() {
		return name.getValue();
	}

	public @NotNull ItemStack getIcon() {
		return icon;
	}

	public void setIcon(@NotNull ItemStack icon) {
		this.icon = icon;
	}

	public @Nullable DyeColor getColor() {
		return color;
	}

	public void setColor(@NotNull DyeColor color) {
		this.color = color;
	}

	public boolean isEnabled() {
		return QuestsConfiguration.getConfig().getQuestsMenuConfig().getEnabledTabs().contains(this);
	}

	public static @Nullable PlayerListCategory fromString(@NotNull String name) {
		try {
			return PlayerListCategory.valueOf(name.toUpperCase());
		}catch (IllegalArgumentException ex) {}
		return null;
	}

}