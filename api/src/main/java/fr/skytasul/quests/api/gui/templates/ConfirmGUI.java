package fr.skytasul.quests.api.gui.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.layout.Button;
import fr.skytasul.quests.api.gui.layout.ClickHandler;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI;
import fr.skytasul.quests.api.localization.Lang;

public final class ConfirmGUI {

	private ConfirmGUI() {}

	public static CustomInventory confirm(@Nullable Runnable yes, @Nullable Runnable no, @NotNull String indication,
			@Nullable String @Nullable... lore) {
		return confirm(yes, no, indication, lore == null ? null : Arrays.asList(lore));
	}

	public static CustomInventory confirm(@Nullable Runnable yes, @Nullable Runnable no, @NotNull String indication,
			@Nullable List<@Nullable String> lore) {
		return LayoutedGUI.newBuilder()
				.addButton(1,
						Button.create(XMaterial.LIME_DYE, Lang.confirmYes.toString(), Collections.emptyList(), event -> {
							event.close();
							if (yes != null)
								yes.run();
						}))
				.addButton(3,
						Button.create(XMaterial.RED_DYE, Lang.confirmNo.toString(), Collections.emptyList(), event -> {
							event.close();
							if (no != null)
								no.run();
						}))
				.addButton(2, Button.create(XMaterial.PAPER, indication, lore, ClickHandler.EMPTY))
				.setInventoryType(InventoryType.HOPPER)
				.setName(Lang.INVENTORY_CONFIRM.toString())
				.setCloseBehavior(StandardCloseBehavior.REOPEN)
				.build();
	}

}
