package fr.skytasul.quests.gui.npc;

import java.util.Collection;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;

public class NpcFactoryGUI extends PagedGUI<BqInternalNpcFactory> {

	private final @NotNull Runnable cancel;
	private final @NotNull Consumer<BqInternalNpcFactory> callback;

	public NpcFactoryGUI(@NotNull Collection<BqInternalNpcFactory> objects, @NotNull Runnable cancel,
			@NotNull Consumer<BqInternalNpcFactory> callback) {
		super(Lang.INVENTORY_SELECT.toString(), DyeColor.LIGHT_BLUE, objects);
		this.cancel = cancel;
		this.callback = callback;
	}

	@Override
	public void open(@NotNull Player player) {
		if (objects.size() == 1)
			callback.accept(objects.get(0));
		else
			super.open(player);
	}

	@Override
	public @NotNull ItemStack getItemStack(@NotNull BqInternalNpcFactory object) {
		return ItemUtils.item(XMaterial.FILLED_MAP, "§a" + BeautyQuests.getInstance().getNpcManager().getFactoryKey(object),
				Lang.Amount.quickFormat("amount", object.getIDs().size()));
	}

	@Override
	public void click(@NotNull BqInternalNpcFactory existing, @NotNull ItemStack item, @NotNull ClickType clickType) {
		callback.accept(existing);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return new DelayCloseBehavior(cancel);
	}

}
