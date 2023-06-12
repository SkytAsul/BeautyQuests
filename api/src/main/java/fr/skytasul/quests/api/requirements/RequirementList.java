package fr.skytasul.quests.api.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;

public class RequirementList extends ArrayList<@NotNull AbstractRequirement> {

	private static final long serialVersionUID = 5568034962195448395L;

	public RequirementList() {}

	public RequirementList(@NotNull Collection<@NotNull AbstractRequirement> requirements) {
		super(requirements);
	}

	public boolean testPlayer(@NotNull Player p, boolean message) {
		for (AbstractRequirement requirement : this) {
			try {
				if (!requirement.test(p)) {
					if (message && !requirement.sendReason(p))
						continue; // means a reason has not yet been sent
					return false;
				}
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
				return false;
			}
		}
		return true;
	}

	public void attachQuest(@NotNull Quest quest) {
		forEach(requirement -> requirement.attach(quest));
	}

	public void detachQuest() {
		forEach(requirement -> requirement.detach());
	}

	public @NotNull List<Map<String, Object>> serialize() {
		return SerializableObject.serializeList(this);
	}

	public static RequirementList deserialize(@NotNull List<Map<?, ?>> mapList) {
		return new RequirementList(SerializableObject.deserializeList(mapList, AbstractRequirement::deserialize));
	}

}
