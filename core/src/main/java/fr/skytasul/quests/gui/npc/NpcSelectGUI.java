package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.SelectNPC;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI.Builder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BQNPC;

public final class NpcSelectGUI {

	private NpcSelectGUI() {}
	
	public static ItemStack createNPC = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.createNPC.toString());
	public static ItemStack selectNPC = ItemUtils.item(XMaterial.STICK, Lang.selectNPC.toString());

	public static @NotNull Gui select(@NotNull Runnable cancel, @NotNull Consumer<@NotNull BQNPC> end) {
		return select(cancel, end, false);
	}

	public static @NotNull Gui selectNullable(@NotNull Runnable cancel,
			@NotNull Consumer<@Nullable BQNPC> end) {
		return select(cancel, end, true);
	}

	private static Gui select(@NotNull Runnable cancel, @NotNull Consumer<BQNPC> end,
			boolean nullable) {
		Builder builder = LayoutedGUI.newBuilder().addButton(1, LayoutedButton.create(createNPC, event -> {
			new NpcCreateGUI(end, event::reopen).open(event.getPlayer());
		})).addButton(3, LayoutedButton.create(selectNPC, event -> {
			new SelectNPC(event.getPlayer(), event::reopen, end).start();
		}));
		if (nullable)
			builder.addButton(2, LayoutedButton.create(ItemUtils.itemNone, event -> {
				event.close();
				end.accept(null);
			}));
		return builder
				.setInventoryType(InventoryType.HOPPER)
				.setName(Lang.INVENTORY_SELECT.toString())
				.setCloseBehavior(new DelayCloseBehavior(cancel))
				.build();
	}

}