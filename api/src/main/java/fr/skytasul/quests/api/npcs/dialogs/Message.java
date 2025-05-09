package fr.skytasul.quests.api.npcs.dialogs;

import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Message implements Cloneable {
	public String text;
	public Sender sender;
	public String sound;
	public int wait = -1;

	public Message(String msg, Sender sender) {
		this.text = msg;
		this.sender = sender;
	}

	public int getWaitTime() {
		return wait == -1 ? QuestsConfiguration.getConfig().getDialogsConfig().getDefaultTime() : wait;
	}

	/**
	 * Play the sound associated with the message to a player.
	 *
	 * @param player Player to play the sound to
	 * @param speaker Entity that "spoke"
	 */
	public void playSound(@NotNull Player player, @Nullable Entity speaker) {
		getSound().ifPresent(sound -> {
			Location location = speaker == null ? player.getLocation() : speaker.getLocation();
			// TODO attach the sound to the location. Investigate in which version of the game this was added.
			player.playSound(location, sound, 1, 1);
		});
	}

	public void stopSound(@NotNull Player player) {
		getSound().ifPresent(player::stopSound);
	}

	private @NotNull Optional<String> getSound() {
		String sentSound = sound;
		if (sentSound == null) {
			if (sender == Sender.PLAYER) {
				sentSound = QuestsConfiguration.getConfig().getDialogsConfig().getDefaultPlayerSound();
			} else if (sender == Sender.NPC) {
				sentSound = QuestsConfiguration.getConfig().getDialogsConfig().getDefaultNPCSound();
			}
		}
		if ("none".equals(sentSound))
			return Optional.empty();
		return Optional.ofNullable(sentSound);
	}

	public @NotNull String formatMessage(@NotNull Player p, @Nullable BqNpc npc, @Nullable String npcCustomName, int id,
			int size) {
		PlaceholderRegistry registry = new PlaceholderRegistry()
				.registerIndexed("player_name", p.getName())
				.registerIndexed("npc_name_message", npcCustomName)
				.registerIndexed("message_id", id + 1)
				.registerIndexed("message_count", size);
		if (npc != null)
			registry.compose(npc);

		String sent = MessageUtils.finalFormat(text, registry.withoutIndexes("npc_name_message", "player_name"),
				PlaceholdersContext.of(p, true, null));
		// ugly trick to have placeholders parsed in the message

		registry.registerIndexed("text", sent);

		switch (sender) {
			case PLAYER:
				sent = MessageUtils.finalFormat(Lang.SelfText.toString(), registry.withoutIndexes("npc_name_message"),
						PlaceholdersContext.of(p, true, null));
				break;
			case NPC:
				sent = MessageUtils.finalFormat(Lang.NpcText.toString(), registry.withoutIndexes("player_name"),
						PlaceholdersContext.of(p, true, null));
				break;
			case NOSENDER:
				// nothing to do: the placeholders has already been parsed
				break;
		}
		return sent;
	}

	@Override
	public Message clone() {
		Message clone = new Message(text, sender);
		clone.sound = sound;
		clone.wait = wait;
		return clone;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("text", text);
		map.put("sender", sender.name());
		if (sound != null)
			map.put("sound", sound);
		if (wait != -1)
			map.put("wait", wait);
		return map;
	}

	public static Message deserialize(Map<String, Object> map) {
		Message msg = new Message((String) map.get("text"), Sender.fromString((String) map.get("sender")));
		if (map.containsKey("sound"))
			msg.sound = (String) map.get("sound");
		if (map.containsKey("wait"))
			msg.wait = (int) map.get("wait");
		return msg;
	}

	public enum Sender {
		PLAYER, NPC, NOSENDER;

		public static Sender fromString(String string) {
			if (string.equalsIgnoreCase("NOTHING"))
				return NOSENDER;
			return valueOf(string.toUpperCase());
		}
	}
}