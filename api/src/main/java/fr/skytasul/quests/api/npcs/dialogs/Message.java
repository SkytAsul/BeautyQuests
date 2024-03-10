package fr.skytasul.quests.api.npcs.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import fr.euphyllia.energie.model.SchedulerTaskInter;
import fr.euphyllia.energie.model.SchedulerType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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

	public SchedulerTaskInter sendMessage(@NotNull Player p, @Nullable BqNpc npc, @Nullable String npcCustomName, int id, int size) {
		AtomicReference<SchedulerTaskInter> task = new AtomicReference<>(null);

		String sent = formatMessage(p, npc, npcCustomName, id, size);
		if (QuestsConfiguration.getConfig().getDialogsConfig().sendInActionBar()) {
			BaseComponent[] components = TextComponent.fromLegacyText(sent.replace("{nl}", " "));
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
			if (getWaitTime() > 60) {
				AtomicInteger time = new AtomicInteger(40);
				QuestsPlugin.getPlugin().getScheduler().runAtFixedRate(SchedulerType.ASYNC, schedulerTaskInter -> {
					if (schedulerTaskInter == null) return;
					task.set(schedulerTaskInter);
					if (!p.isOnline()) {
						schedulerTaskInter.cancel();
						return;
					}

					time.addAndGet(40);
					if (time.get() > getWaitTime())
						schedulerTaskInter.cancel();
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
				}, 40, 40);
			}
		} else
			p.sendMessage(StringUtils.splitByWholeSeparator(sent, "{nl}"));

		if (!"none".equals(sound)) {
			String sentSound = getSound();
			if (sentSound != null)
				p.playSound(p.getLocation(), sentSound, 1, 1);
		}

		return task.get();
	}

	private String getSound() {
		String sentSound = sound;
		if (sentSound == null) {
			if (sender == Sender.PLAYER) {
				sentSound = QuestsConfiguration.getConfig().getDialogsConfig().getDefaultPlayerSound();
			} else if (sender == Sender.NPC) {
				sentSound = QuestsConfiguration.getConfig().getDialogsConfig().getDefaultNPCSound();
			}
		}
		return sentSound;
	}

	public String formatMessage(@NotNull Player p, @Nullable BqNpc npc, @Nullable String npcCustomName, int id, int size) {
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

	public void finished(Player p, boolean endOfDialog, boolean forced) {
		if (endOfDialog || !forced)
			return;
		String sentSound = getSound();
		if (sentSound != null)
			p.stopSound(sentSound);
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