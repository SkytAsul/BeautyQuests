package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.RewardsGUI;
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

	
	protected void save(Map<String, Object> datas){
		datas.put("tp", teleportation.serialize());
	}

	protected void load(Map<String, Object> savedDatas){
		teleportation = Location.deserialize((Map<String, Object>) savedDatas.get("tp"));
	}

	public static class Creator implements RewardCreationRunnables {

		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			Lang.MOVE_TELEPORT_POINT.send(p);
			Editor.enterOrLeave(p, new WaitClick(p, NPCGUI.validMove.clone(), () -> {
				Location lc = p.getLocation();
				datas.put("loc", lc);
				ItemUtils.lore(clicked, Utils.locationToString(lc));
				gui.reopen(p, false);
			}));
		}

		public void edit(Map<String, Object> datas, AbstractReward reward, ItemStack is) {
			TeleportationReward rew = (TeleportationReward) reward;
			Location lc = rew.teleportation;
			datas.put("loc", lc);
			ItemUtils.lore(is, Utils.locationToString(lc));
		}

		public AbstractReward finish(Map<String, Object> datas) {
			return new TeleportationReward((Location) datas.get("loc"));
		}

	}

}
