package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.editors.SelectNPC;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI.Builder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.BqNpc;

public final class NpcSelectGUI {

	private NpcSelectGUI() {}
	
	public static ItemStack createNPC = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.createNPC.toString());
	public static ItemStack selectNPC = ItemUtils.item(XMaterial.STICK, Lang.selectNPC.toString());

	public static AbstractGui select(@NotNull Runnable cancel, @NotNull Consumer<BqNpc> end,
			boolean nullable) {
		Builder builder = LayoutedGUI.newBuilder().addButton(1, LayoutedButton.create(createNPC, event -> {
			new NpcFactoryGUI(BeautyQuests.getInstance().getNpcManager()
					.getInternalFactories().stream()
					.filter(BqInternalNpcFactoryCreatable.class::isInstance)
					.map(BqInternalNpcFactoryCreatable.class::cast)
					.collect(Collectors.toList()), event::reopen, factory -> {
						new NpcCreateGUI((@NotNull BqInternalNpcFactoryCreatable) factory, end, event::reopen)
								.open(event.getPlayer());
					}).open(event.getPlayer());
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