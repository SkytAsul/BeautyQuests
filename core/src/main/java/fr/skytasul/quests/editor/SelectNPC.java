package fr.skytasul.quests.editor;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.InventoryClear;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.utils.messaging.MessageType.DefaultMessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.npcs.BQNPCClickEvent;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.function.Consumer;

public class SelectNPC extends InventoryClear implements Listener {

	private Consumer<BqNpc> run;

	public SelectNPC(Player p, Runnable cancel, Consumer<BqNpc> end) {
		super(p, cancel);
		this.run = end;
	}

	@EventHandler (priority = EventPriority.LOW)
	private void onNPCClick(BQNPCClickEvent e) {
		if (e.getPlayer() != player) return;
		e.setCancelled(true);
		stop();
		run.accept(e.getNPC());
	}

	@Override
	public void begin(){
		super.begin();
		if (!QuestsPlugin.getPlugin().getNpcManager().isEnabled()) {
			MessageUtils.sendMessage(player, "§cWARNING: No NPC plugin registered.\nLeft editor.",
					DefaultMessageType.PREFIXED);
			QuestUtils.runSync(this::cancel);
			return;
		}

		Lang.NPC_EDITOR_ENTER.send(player);
	}

}