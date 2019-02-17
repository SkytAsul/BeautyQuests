package fr.skytasul.quests.scoreboards;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.ReflectUtils;
import fr.skytasul.quests.utils.nms.NMS;

/**
 * @author zyuiop, SkytAsul
 */
public class ScoreboardSigns {
	private static ReflectUtils nms = NMS.getNMS().getNMSReflect();
	
	private boolean created = false;
	private final VirtualTeam[] lines = new VirtualTeam[15];
	private final Player player;
	private String objectiveName;

	/**
	 * Create a scoreboard sign for a given player and using a specifig objective name
	 * @param player the player viewing the scoreboard sign
	 * @param objectiveName the name of the scoreboard sign (displayed at the top of the scoreboard)
	 */
	public ScoreboardSigns(Player player, String objectiveName) {
		this.player = player;
		this.objectiveName = objectiveName;
	}

	/**
	 * Send the initial creation packets for this scoreboard sign. Must be called at least once.
	 */
	public void create() {
		if (created)
			return;

		try {
			NMS.getNMS().sendPacket(player, createObjectivePacket(0, objectiveName));
			NMS.getNMS().sendPacket(player, setObjectiveSlot());
			int i = 0;
			while (i < lines.length)
				sendLine(i++);

			created = true;
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send the packets to remove this scoreboard sign. A destroyed scoreboard sign must be recreated using create() in order
	 * to be used again
	 */
	public void destroy() {
		if (!created)
			return;

		try {
			NMS.getNMS().sendPacket(player, createObjectivePacket(1, null));
			for (VirtualTeam team : lines)
				if (team != null)
					NMS.getNMS().sendPacket(player, team.removeTeam());

			created = false;
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the name of the objective. The name is displayed at the top of the scoreboard.
	 * @param name the name of the objective, max 32 char
	 * @throws ClassNotFoundException reflection problem
	 */
	public void setObjectiveName(String name) throws ClassNotFoundException {
		this.objectiveName = name;
		if (created)
			NMS.getNMS().sendPacket(player, createObjectivePacket(2, name));
	}

	/**
	 * Change a scoreboard line and send the packets to the player. Can be called async.
	 * @param line the number of the line (0 &#60;= line &#60; 15)
	 * @param value the new value for the scoreboard line
	 */
	public void setLine(int line, String value) {
		try {
			VirtualTeam team = getOrCreateTeam(line);
			String old = team.getCurrentPlayer();

			if (old != null && created)
				NMS.getNMS().sendPacket(player, removeLine(old));

			team.setValue(value);
			sendLine(line);
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a given scoreboard line
	 * @param line the line to remove
	 * @throws ClassNotFoundException reflection problem
	 */
	public void removeLine(int line) throws ClassNotFoundException {
		VirtualTeam team = getOrCreateTeam(line);
		String old = team.getCurrentPlayer();

		if (old != null && created) {
			NMS.getNMS().sendPacket(player, removeLine(old));
			NMS.getNMS().sendPacket(player, team.removeTeam());
		}

		lines[line] = null;
	}

	/**
	 * Get the current value for a line
	 * @param line the line
	 * @return the content of the line
	 */
	public String getLine(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
		return getOrCreateTeam(line).getValue();
	}

	/**
	 * Get the team assigned to a line
	 * @param line line number
	 * @return the VirtualTeam used to display this line
	 */
	public VirtualTeam getTeam(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
		return getOrCreateTeam(line);
	}

	private void sendLine(int line) throws ClassNotFoundException {
		if (line > 14)
			return;
		if (line < 0)
			return;
		if (!created)
			return;

		int score = (15 - line);
		VirtualTeam val = getOrCreateTeam(line);
		for (Object packet : val.sendLine()) {
			NMS.getNMS().sendPacket(player, packet);
		}
		NMS.getNMS().sendPacket(player, sendScore(val.getCurrentPlayer(), score));
		//System.out.println("AFTER PA");
		val.reset();
	}

	private VirtualTeam getOrCreateTeam(int line) {
		if (lines[line] == null)
			lines[line] = new VirtualTeam("__fakeScore" + line);

		return lines[line];
	}

	/*
		Factories
		 */
	private Object createObjectivePacket(int mode, String displayName) throws ClassNotFoundException {
		Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardObjective");
		// Nom de l'objectif
		setField(packet, "a", player.getName());

		// Mode
		// 0 : créer
		// 1 : Supprimer
		// 2 : Mettre à jour
		setField(packet, "d", mode);

		if (mode == 0 || mode == 2) {
			setField(packet, "b", NMS.getMCVersion() < 13 ? displayName : NMS.getNMS().getIChatBaseComponent(displayName));
			setField(packet, "c", ReflectUtils.fromEnum(ReflectUtils.getClassDotClass(nms.fromName("IScoreboardCriteria"), "EnumScoreboardHealthDisplay"), 0));
		}

		return packet;
	}

	private Object setObjectiveSlot() {
		Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardDisplayObjective");
		// Slot
		setField(packet, "a", 1);
		setField(packet, "b", player.getName());

		return packet;
	}

	private Object sendScore(String line, int score) throws ClassNotFoundException {
		Object packet;
		if (NMS.getMCVersion() < 13){
			packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardScore", line);
			setField(packet, "b", player.getName());
			setField(packet, "c", score);
			setField(packet, "d", ReflectUtils.fromEnum(ReflectUtils.getClassDotClass(nms.fromName("PacketPlayOutScoreboardScore"), "EnumScoreboardAction"), 0));
		}else {
			packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardScore");
			setField(packet, "a", line);
			setField(packet, "b", player.getName());
			setField(packet, "c", score);
			setField(packet, "d", ReflectUtils.fromEnum(ReflectUtils.getClassDotClass(nms.fromName("ScoreboardServer"), "Action"), 0));
		}
		return packet;
	}

	private Object removeLine(String line) throws ClassNotFoundException {
		if (NMS.getMCVersion() < 13){
			return NMS.getNMS().newPacket("PacketPlayOutScoreboardScore", line);
		}
		Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardScore");
		setField(packet, "a", line);
		setField(packet, "d", ReflectUtils.fromEnum(ReflectUtils.getClassDotClass(nms.fromName("ScoreboardServer"), "Action"), 1));
		return packet;
	}

	/**
	 * This class is used to manage the content of a line. Advanced users can use it as they want, but they are encouraged to read and understand the
	 * code before doing so. Use these methods at your own risk.
	 */
	public class VirtualTeam {
		private final String name;
		private String prefix;
		private String suffix;
		private String currentPlayer;
		private String oldPlayer;

		private boolean prefixChanged, suffixChanged, playerChanged = false;
		private boolean first = true;

		private VirtualTeam(String name, String prefix, String suffix) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
		}

		private VirtualTeam(String name) {
			this(name, "", "");
		}

		public String getName() {
			return name;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			if (this.prefix == null || !this.prefix.equals(prefix))
				this.prefixChanged = true;
			this.prefix = prefix;
		}

		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			if (this.suffix == null || !this.suffix.equals(prefix))
				this.suffixChanged = true;
			this.suffix = suffix;
		}

		private Object createPacket(int mode) {
			//System.out.println("pre" + prefix);
			Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardTeam");
			setField(packet, "a", name);
			setField(packet, "i", mode);
			setField(packet, "b", NMS.getMCVersion() < 13 ? "" : NMS.getNMS().getIChatBaseComponent(""));
			setField(packet, "c", NMS.getMCVersion() < 13 ? prefix : NMS.getNMS().getIChatBaseComponent(prefix));
			setField(packet, "d", NMS.getMCVersion() < 13 ? suffix : NMS.getNMS().getIChatBaseComponent(suffix));
			setField(packet, "j", 0);
			setField(packet, "e", "always");
			setField(packet, "g", NMS.getMCVersion() < 13 ? 0 : NMS.getNMS().getEnumChatFormat(0));

			return packet;
		}

		public Object createTeam() {
			return createPacket(0);
		}

		public Object updateTeam() {
			return createPacket(2);
		}

		public Object removeTeam() {
			Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardTeam");
			setField(packet, "a", name);
			setField(packet, "i", 1);
			first = true;
			return packet;
		}

		public void setPlayer(String name) {
			if (this.currentPlayer == null || !this.currentPlayer.equals(name))
				this.playerChanged = true;
			this.oldPlayer = this.currentPlayer;
			this.currentPlayer = name;
		}

		public Iterable<Object> sendLine() {
			List<Object> packets = new ArrayList<>();

			if (first) {
				packets.add(createTeam());
			} else if (prefixChanged || suffixChanged) {
				packets.add(updateTeam());
			}

			if (first || playerChanged) {
				if (oldPlayer != null)										// remove these two lines ?
					packets.add(addOrRemovePlayer(4, oldPlayer)); 	//
				packets.add(changePlayer());
			}

			if (first)
				first = false;

			return packets;
		}

		public void reset() {
			prefixChanged = false;
			suffixChanged = false;
			playerChanged = false;
			oldPlayer = null;
		}

		public Object changePlayer() {
			return addOrRemovePlayer(3, currentPlayer);
		}

		public Object addOrRemovePlayer(int mode, String playerName) {
			Object packet = NMS.getNMS().newPacket("PacketPlayOutScoreboardTeam");
			setField(packet, "a", name);
			setField(packet, "i", mode);

			try {
				Field f = packet.getClass().getDeclaredField("h");
				f.setAccessible(true);
				((List<String>) f.get(packet)).add(playerName);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}

			return packet;
		}

		public String getCurrentPlayer() {
			return currentPlayer;
		}

		public String getValue() {
			return getPrefix() + getCurrentPlayer() + getSuffix();
		}

		public void setValue(String value) {
			if (value.length() <= 16) {
				setPrefix("");
				setSuffix("");
				setPlayer(value);
			} else if (value.length() <= 32) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16));
				setSuffix("");
				//System.out.println("pre : " + prefix + " | player : " + currentPlayer);
			} else if (value.length() <= 48) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16, 32));
				setSuffix(value.substring(32));
			} else {
				throw new IllegalArgumentException("Too long value ! Max 48 characters, value was " + value.length() + " !");
			}
		}
	}

	private static void setField(Object edit, String fieldName, Object value) {
		Validate.notNull(edit);
		try {
			Field field = edit.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(edit, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}