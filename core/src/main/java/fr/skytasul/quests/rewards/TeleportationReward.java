package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class TeleportationReward extends AbstractReward {

	public Location teleportation;

	public TeleportationReward(){
		super("tpReward");
	}
	
	public TeleportationReward(Location teleportation){
		this();
		this.teleportation = teleportation;
	}

	public String give(Player p){
		p.teleport(teleportation);
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new TeleportationReward(teleportation.clone());
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + Utils.locationToString(teleportation), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.MOVE_TELEPORT_POINT.send(p);
		new WaitClick(p, () -> {
			if (teleportation == null) gui.remove(this);
			gui.reopen();
		}, NPCGUI.validMove.clone(), () -> {
			teleportation = p.getLocation();
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}).enter();
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("tp", teleportation.serialize());
	}
	
	protected void load(Map<String, Object> savedDatas) {
		teleportation = Location.deserialize((Map<String, Object>) savedDatas.get("tp"));
	}
	
}
