package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class StagePlayTime extends AbstractStage {

	public long playTicks;
	
	private Map<PlayerAccount, BukkitTask> tasks = new HashMap<>();
	
	public StagePlayTime(QuestBranch branch, long ticks) {
		super(branch);
		this.playTicks = ticks;
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
	
	public static StagePlayTime deserialize(Map<String, Object> map, QuestBranch branch) {
		return new StagePlayTime(branch, Utils.parseLong(map.get("playTicks")));
	}
	
	public static class Creator extends StageCreation<StagePlayTime> {
		
		private long ticks;

		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.CLOCK, Lang.changeTicksRequired.toString()), (p, item) -> {
				Lang.GAME_TICKS.send(p);
				new TextEditor<>(p, () -> reopenGUI(p, false), obj -> {
					setTicks(obj);
					reopenGUI(p, false);
				}, new NumberParser<>(Long.class, true, true)).enter();
			});
		}
		
		public void setTicks(long ticks) {
			this.ticks = ticks;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(ticks + " ticks")));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Lang.GAME_TICKS.send(p);
			new TextEditor<>(p, removeAndReopen(p, false), obj -> {
				setTicks(obj);
				reopenGUI(p, false);
			}, new NumberParser<>(Long.class, true, true)).enter();
		}
		
		@Override
		public void edit(StagePlayTime stage) {
			super.edit(stage);
			setTicks(stage.playTicks);
		}
		
		@Override
		public StagePlayTime finishStage(QuestBranch branch) {
			return new StagePlayTime(branch, ticks);
		}
		
	}
	
}
