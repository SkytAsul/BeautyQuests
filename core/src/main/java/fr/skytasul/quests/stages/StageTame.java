package fr.skytasul.quests.stages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractEntityStage;

public class StageTame extends AbstractEntityStage {
	
	public StageTame(StageController controller, EntityType entity, int amount) {
		super(controller, entity, amount);
	}
	
	@EventHandler
	public void onTame(EntityTameEvent e) {
		if (e.getOwner() instanceof Player) {
			Player p = (Player) e.getOwner();
			event(p, e.getEntityType());
		}
	}
	
	@Override
	public String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_TAME.format(getMobsLeft(acc));
	}
	
	public static StageTame deserialize(ConfigurationSection section, StageController controller) {
		String type = section.getString("entityType");
		return new StageTame(controller, "any".equals(type) ? null : EntityType.valueOf(type), section.getInt("amount"));
	}
	
	public static class Creator extends AbstractEntityStage.AbstractCreator<StageTame> {
		
		public Creator(@NotNull StageCreationContext<StageTame> context) {
			super(context);
		}
		
		@Override
		protected boolean canUseEntity(EntityType type) {
			return Tameable.class.isAssignableFrom(type.getEntityClass());
		}
		
		@Override
		protected StageTame finishStage(StageController controller) {
			return new StageTame(controller, entity, amount);
		}
		
	}
	
}
