package fr.skytasul.quests.api.npcs;

public enum NpcClickType {
	RIGHT, SHIFT_RIGHT, LEFT, SHIFT_LEFT;
	
	public static NpcClickType of(boolean left, boolean shift) {
		if (left) {
			return shift ? SHIFT_LEFT : LEFT;
		}else {
			return shift ? SHIFT_RIGHT : RIGHT;
		}
	}
}