package fr.skytasul.quests.gui.npc;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.layout.LayoutedButton;
import fr.skytasul.quests.api.gui.layout.LayoutedClickEvent;
import fr.skytasul.quests.api.gui.layout.LayoutedGUI.LayoutedRowsGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.function.Consumer;

public class NpcCreateGUI extends LayoutedRowsGUI {

	public static ItemStack validMove = ItemUtils.item(XMaterial.EMERALD, Lang.moveItem.toString());

	private final @NotNull BqInternalNpcFactoryCreatable factory;
	private final @NotNull Consumer<@NotNull BqNpc> end;
	private final @NotNull Runnable cancel;

	private EntityType en = EntityType.PLAYER;
	private String name = "§cUnknown";
	private String skin = "Knight";

	public NpcCreateGUI(@NotNull BqInternalNpcFactoryCreatable factory, @NotNull Consumer<@NotNull BqNpc> end,
			@NotNull Runnable cancel) {
		super(Lang.INVENTORY_NPC.toString(), new HashMap<>(), new DelayCloseBehavior(cancel), 1);

		this.factory = factory;
		this.end = end;
		this.cancel = cancel;

		buttons.put(0, LayoutedButton.create(ItemUtils.item(XMaterial.MINECART, Lang.move.toString(),
				QuestOption.formatDescription(Lang.moveLore.toString())), this::onMoveClick));
		buttons.put(1,
				LayoutedButton.createLoreValue(XMaterial.NAME_TAG, Lang.name.toString(), () -> name, this::onNameClick));
		buttons.put(3,
				LayoutedButton.create(
						() -> ItemUtils.skull(Lang.skin.toString(), skin, QuestOption.formatNullableValue(skin)),
						this::onSkinClick));
		buttons.put(5, LayoutedButton.create(() -> {
			if (en == EntityType.PLAYER) {
				return ItemUtils.skull(Lang.npcType.toString(), null, QuestOption.formatNullableValue("player"));
			} else
				return ItemUtils.item(Utils.mobItem(en), Lang.npcType.toString(),
						QuestOption.formatNullableValue(en.getName()));
		}, this::onEntityTypeClick));

		buttons.put(7, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getCancel(),
				this::onCancelClick));
		buttons.put(8, LayoutedButton.create(QuestsPlugin.getPlugin().getGuiManager().getItemFactory().getDone(),
				this::onDoneClick));
	}

	private void onMoveClick(LayoutedClickEvent event) {
		new WaitClick(event.getPlayer(), event::reopen, validMove.clone(), event::reopen).start();
	}

	private void onNameClick(LayoutedClickEvent event) {
		Lang.NPC_NAME.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
			this.name = obj;
			event.refreshItemReopen();
		}).start();
	}

	private void onSkinClick(LayoutedClickEvent event) {
		Lang.NPC_SKIN.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
			this.skin = obj;
			event.refreshItemReopen();
		}).useStrippedMessage().start();
	}

	private void onEntityTypeClick(LayoutedClickEvent event) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createEntityTypeSelection(en -> {
			this.en = en;
			event.refreshItemReopen();
		}, x -> x != null && factory.isValidEntityType(x)).open(event.getPlayer());
	}

	private void onCancelClick(LayoutedClickEvent event) {
		event.close();
		cancel.run();
	}

	private void onDoneClick(LayoutedClickEvent event) {
		event.close();
		try {
			end.accept(QuestsPlugin.getPlugin().getNpcManager().createNPC(factory, event.getPlayer().getLocation(), en,
					name, skin));
		} catch (Exception ex) {
			ex.printStackTrace();
			DefaultErrors.sendGeneric(event.getPlayer(), "npc creation " + ex.getMessage());
			cancel.run();
		}
	}

}