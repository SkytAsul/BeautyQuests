package fr.skytasul.quests.stages;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractEntityStage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.jetbrains.annotations.NotNull;

public class StageTame extends AbstractEntityStage implements Listener {

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
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_TAME.toString();
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
