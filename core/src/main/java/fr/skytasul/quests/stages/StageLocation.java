package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.WaitClick;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.gui.npc.NPCGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.events.PlayerAccountJoinEvent;
import fr.skytasul.quests.players.events.PlayerAccountLeaveEvent;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.GPS;

public class StageLocation extends AbstractStage {

	private final Location lc;
	private final int radius;
	private final int radiusSquared;
	
	private String descMessage;
	
	public StageLocation(QuestBranch branch, Location lc, int radius){
		super(branch);
		this.lc = lc;
		this.radius = radius;
		this.radiusSquared = radius * radius;
		
		this.descMessage = Lang.SCOREBOARD_LOCATION.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorld().getName());
	}
	
	public Location getLocation(){
		return lc;
	}
	
	public int getRadius(){
		return radius;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (e.getTo().getWorld() != lc.getWorld()) return;
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return; // only rotation
		
		Player p = e.getPlayer();
		if (hasStarted(p) && canUpdate(p)) {
			if (e.getTo().distanceSquared(lc) <= radiusSquared) finishStage(p);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (QuestsConfiguration.handleGPS() && branch.hasStageLaunched(e.getPlayerAccount(), this)) GPS.launchCompass(e.getPlayer(), lc);
	}
	
	@EventHandler
	public void onLeave(PlayerAccountLeaveEvent e) {
		if (QuestsConfiguration.handleGPS() && branch.hasStageLaunched(e.getPlayerAccount(), this)) GPS.stopCompass(e.getPlayer());
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return descMessage;
	}

	protected void serialize(Map<String, Object> map){
		map.put("location", lc.serialize());
		map.put("radius", radius);
	}

	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		return new StageLocation(branch, Location.deserialize((Map<String, Object>) map.get("location")), (int) map.get("radius"));
	}
	
	public static class Creator implements StageCreationRunnables<StageLocation> {
		public void start(Player p, LineData datas) {
			Lang.LOCATION_GO.send(p);
			new WaitClick(p, NPCGUI.validMove, () -> {
				datas.put("location", p.getLocation());
				datas.put("radius", 5);
				datas.getGUI().reopen(p, false);
				setItems(datas.getLine());
			}).enterOrLeave(p);
		}

		public StageLocation finish(LineData datas, QuestBranch branch) {
			StageLocation stage = new StageLocation(branch, (Location) datas.get("location"), (int) datas.get("radius"));
			return stage;
		}

		public void edit(LineData datas, StageLocation stage) {
			datas.put("location", stage.getLocation());
			datas.put("radius", stage.getRadius());
			setItems(datas.getLine());
		}

		public static void setItems(Line line) {
			line.setItem(7, ItemUtils.item(XMaterial.REDSTONE, Lang.editRadius.toString(), Lang.currentRadius.format(line.data.get("radius"))), (p, datas, item) -> {
				Lang.LOCATION_RADIUS.send(p);
				new TextEditor(p, (x) -> {
					datas.put("radius", x);
					datas.getGUI().reopen(p, false);
					ItemUtils.lore(item, Lang.currentRadius.format(x));
				}, new NumberParser(Integer.class, true, true), () -> datas.getGUI().reopen(p, false), null).enterOrLeave(p);
			});
			line.setItem(6, ItemUtils.item(XMaterial.STICK, Lang.editLocation.toString()), (p, datas, item) -> {
				Lang.LOCATION_GO.send(p);
				new WaitClick(p, NPCGUI.validMove, () -> {
					datas.put("location", p.getLocation());
					datas.getGUI().reopen(p, false);
				}).enterOrLeave(p);
			});
		}
	}
	
}
