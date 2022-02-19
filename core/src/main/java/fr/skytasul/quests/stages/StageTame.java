package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;

import fr.skytasul.quests.api.stages.AbstractEntityStage;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;

public class StageTame extends AbstractEntityStage {
	
	public StageTame(QuestBranch branch, EntityType entity, int amount) {
		super(branch, entity, amount);
	}
	
	@EventHandler
	public void onTame(EntityTameEvent e) {
		if (e.getOwner() instanceof Player) {
			Player p = (Player) e.getOwner();
			event(p, e.getEntityType());
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_TAME.format(getMobsLeft(acc));
	}
	
	public static StageTame deserialize(Map<String, Object> map, QuestBranch branch) {
		String type = (String) map.get("entityType");
		return new StageTame(branch, "any".equals(type) ? null : EntityType.valueOf(type), (int) map.get("amount"));
	}
	
	public static class Creator extends AbstractEntityStage.AbstractCreator<StageTame> {
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected boolean canUseEntity(EntityType type) {
			return Tameable.class.isAssignableFrom(type.getEntityClass());
		}
		
		@Override
		protected StageTame finishStage(QuestBranch branch) {
			return new StageTame(branch, entity, amount);
		}
		
	}
	
}
