package fr.skytasul.quests.integrations.tooltips;

import fi.septicuss.tooltips.Tooltips;
import fi.septicuss.tooltips.managers.condition.Condition;
import fi.septicuss.tooltips.managers.condition.Context;
import fi.septicuss.tooltips.managers.condition.argument.Arguments;
import fi.septicuss.tooltips.utils.validation.Validity;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.npcs.dialogs.MessageSender;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TooltipsMessageSender implements MessageSender {

	private Map<Player, DialogRunner> players = new HashMap<>();

	public TooltipsMessageSender() {
		Tooltips.get().getConditionManager().register(new BqTooltipsCondition());
	}

	@Override
	public void displayMessage(@NotNull Player player, @NotNull DialogRunner dialog, @NotNull Message message) {
		if (players.containsKey(player)) {
			Bukkit.getScheduler().runTaskLater(QuestsPlugin.getPlugin(),
					() -> Tooltips.get().getTooltipManager().runActions("beautyquests-ready", player),
					Tooltips.get().getCheckFrequency() + 1);
		} else {
			players.put(player, dialog);
		}
	}

	@Override
	public void stopDisplayingMessages(@NotNull Player player, @NotNull DialogRunner dialog) {
		players.remove(player);
	}

	private class BqTooltipsCondition implements Condition {

		@Override
		public String id() {
			return "beautyquests";
		}

		@Override
		public Validity valid(Arguments arg0) {
			return Validity.TRUE;
		}

		@Override
		public boolean check(Player player, Arguments args) {
			return players.containsKey(player);
		}

		@Override
		public void writeContext(Player player, Arguments args, Context context) {
			DialogRunner dialog = players.get(player);
			if (dialog == null)
				return;

			Message message = dialog.getDialog().getMessages().get(dialog.getPlayerMessage(player));

			String npcName = dialog.getDialog().getNPCName(dialog.getNpc());
			context.put("beautyquests.npc", npcName);

			switch (message.sender) {
				case NPC:
					context.put("beautyquests.speaker", npcName);
					break;
				case PLAYER:
					context.put("beautyquests.speaker", player.getName());
					break;
				case NOSENDER:
					break;
			}

			String text = message.text;
			text = MessageUtils.finalFormat(text, null, PlaceholdersContext.of(player, true, null));
			text = ChatColorUtils.wordWrap(text, 75).stream()
					.map(LegacyComponentSerializer.legacySection()::deserialize)
					.map(MiniMessage.miniMessage()::serialize)
					.collect(Collectors.joining("\n"));
			context.put("beautyquests.text", text);
		}

	}

}
