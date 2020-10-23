package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreationRunnables;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.LineData;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StagePlayTime extends AbstractStage {

	public long playTicks;
	
	private Map<PlayerAccount, BukkitTask> tasks = new HashMap<>();
	
	public StagePlayTime(QuestBranch branch) {
		super(branch);
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_PLAY_TIME.format(playTicks);
	}
	
	private void launchTask(PlayerAccount acc, Player p, long remaining) {
		tasks.put(acc, Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> finishStage(p), remaining < 0 ? 0 : remaining));
	}
	
	@Override
	public void joins(PlayerAccount acc, Player p) {
		super.joins(acc, p);
		updateObjective(acc, null, "lastJoin", System.currentTimeMillis());
		launchTask(acc, p, Utils.parseLong(getData(acc, "remainingTime")));
	}
	
	@Override
	public void leaves(PlayerAccount acc, Player p) {
		super.leaves(acc, p);
		tasks.remove(acc).cancel();
		long remaining = Utils.parseLong(getData(acc, "remainingTime"));
		long lastJoin = Utils.parseLong(getData(acc, "lastJoin"));
		long playedTicks = (System.currentTimeMillis() - lastJoin) / 50;
		updateObjective(acc, null, "remainingTime", remaining - playedTicks);
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			launchTask(acc, acc.getPlayer(), playTicks);
		}
	}
	
	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		super.initPlayerDatas(acc, datas);
		datas.put("remainingTime", playTicks);
		datas.put("lastJoin", System.currentTimeMillis());
	}
	
	@Override
	public void unload() {
		super.unload();
		tasks.keySet().forEach(acc -> leaves(acc, null));
		tasks.clear();
	}

	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("playTicks", playTicks);
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch) {
		StagePlayTime stage = new StagePlayTime(branch);
		stage.playTicks = Utils.parseLong(map.get("playTicks"));
		return stage;
	}
	
	public static class Creator implements StageCreationRunnables<StagePlayTime> {
		
		@Override
		public void start(Player p, LineData datas) {
			Lang.GAME_TICKS.send(p);
			new TextEditor<>(p, () -> {
				datas.getGUI().deleteStageLine(datas, p);
				datas.getGUI().reopen(p, false);
			}, obj -> {
				setItem(datas.getLine(), obj);
				datas.put("ticks", obj);
				datas.getGUI().reopen(p, false);
			}, new NumberParser<>(Long.class, true, true)).enterOrLeave(p);
		}
		
		private void setItem(Line line, long ticks) {
			line.setItem(5, ItemUtils.item(XMaterial.CLOCK, Lang.changeTicksRequired.toString(), "§6Ticks: §e§l" + ticks), (p, item) -> {
				Lang.GAME_TICKS.send(p);
				new TextEditor<>(p, () -> line.getGUI().reopen(p, false), obj -> {
					ItemUtils.lore(item, "§6Ticks: §e§l" + obj);
					line.put("ticks", obj);
					line.getGUI().reopen(p, false);
				}, new NumberParser<>(Long.class, true, true)).enterOrLeave(p);
			});
		}
		
		@Override
		public void edit(LineData datas, StagePlayTime stage) {
			datas.put("ticks", stage.playTicks);
			setItem(datas.getLine(), stage.playTicks);
		}
		
		@Override
		public StagePlayTime finish(LineData datas, QuestBranch branch) {
			StagePlayTime stage = new StagePlayTime(branch);
			stage.playTicks = datas.get("ticks");
			return stage;
		}
		
	}
	
}
