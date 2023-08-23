package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.WaitClick;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory.BqInternalNpcFactoryCreatable;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;

public class NpcCreateGUI extends AbstractGui {

	private static final ItemStack nameItem = ItemUtils.item(XMaterial.NAME_TAG, Lang.name.toString());
	private static final ItemStack move = ItemUtils.item(XMaterial.MINECART, Lang.move.toString(), Lang.moveLore.toString());
	public static ItemStack validMove = ItemUtils.item(XMaterial.EMERALD, Lang.moveItem.toString());
	
	private final @NotNull BqInternalNpcFactoryCreatable factory;
	private final @NotNull Consumer<@NotNull BqNpc> end;
	private final @NotNull Runnable cancel;
	
	private EntityType en;
	private String name;
	private String skin;
	
	public NpcCreateGUI(@NotNull BqInternalNpcFactoryCreatable factory, @NotNull Consumer<@NotNull BqNpc> end,
			@NotNull Runnable cancel) {
		this.factory = factory;
		this.end = end;
		this.cancel = cancel;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, 9, Lang.INVENTORY_NPC.toString());
	}
	
	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(0, move.clone());
		inventory.setItem(1, nameItem.clone());
		setName("§cno name selected");
		setSkin("Knight");
		setType(EntityType.PLAYER);
		inventory.setItem(7, ItemUtils.itemCancel);
		inventory.setItem(8, ItemUtils.itemDone);
	}
	
	private void setName(String name) {
		this.name = name;
		ItemUtils.lore(getInventory().getItem(1), QuestOption.formatNullableValue(name));
	}
	
	private void setType(EntityType type) {
		this.en = type;
		if (en == EntityType.PLAYER) {
			getInventory().setItem(5, ItemUtils.skull(Lang.npcType.toString(), null, QuestOption.formatNullableValue("player")));
		} else
			getInventory().setItem(5,
					ItemUtils.item(Utils.mobItem(en), Lang.npcType.toString(),
							QuestOption.formatNullableValue(en.getName())));
	}
	
	private void setSkin(String skin) {
		this.skin = skin;
		getInventory().setItem(3, ItemUtils.skull(Lang.skin.toString(), skin, QuestOption.formatNullableValue(skin)));
	}

	@Override
	public void onClick(GuiClickEvent event) {
		switch (event.getSlot()) {
		
		case 0:
				new WaitClick(event.getPlayer(), event::reopen, validMove.clone(), event::reopen).start();
			break;

		case 1:
			Lang.NPC_NAME.send(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
				setName(obj);
				event.reopen();
			}).start();
			break;

		case 3:
			Lang.NPC_SKIN.send(event.getPlayer());
			new TextEditor<String>(event.getPlayer(), event::reopen, obj -> {
				if (obj != null) setSkin(obj);
				event.reopen();
			}).useStrippedMessage().start();
			break;
			
		case 5:
			QuestsPlugin.getPlugin().getGuiManager().getFactory().createEntityTypeSelection(en -> {
				setType(en);
				event.reopen();
			}, x -> x != null && factory.isValidEntityType(x)).open(event.getPlayer());
			break;
			
		case 7:
			event.close();
			cancel.run();
			break;
			
		case 8:
			event.close();
			try {
				end.accept(QuestsPlugin.getPlugin().getNpcManager().createNPC(factory, event.getPlayer().getLocation(), en,
						name, skin));
			}catch (Exception ex) {
				ex.printStackTrace();
				DefaultErrors.sendGeneric(event.getPlayer(), "npc creation " + ex.getMessage());
				cancel.run();
			}
			break;
		
		}
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(cancel);
	}
	
}