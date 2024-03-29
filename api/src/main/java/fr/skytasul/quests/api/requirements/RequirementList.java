package fr.skytasul.quests.api.requirements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;

public class RequirementList extends ArrayList<@NotNull AbstractRequirement> {

	private static final long serialVersionUID = 5568034962195448395L;

	public RequirementList() {}

	public RequirementList(@NotNull Collection<@NotNull AbstractRequirement> requirements) {
		super(requirements);
	}

	public boolean allMatch(@NotNull Player p, boolean message) {
		boolean match = true;
		for (AbstractRequirement requirement : this) {
			try {
				if (!requirement.isValid() || !requirement.test(p)) {
					if (!message || requirement.sendReason(p))
						return false;

					// if we are here, it means a reason has not yet been sent
					// so we continue until a reason is sent OR there is no more requirement
					match = false;
				}
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
				return false;
			}
		}
		return match;
	}

	public boolean anyMatch(@NotNull Player p) {
		for (AbstractRequirement requirement : this) {
			try {
				if (requirement.isValid() && requirement.test(p))
					return true;
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
			}
		}
		return false;
	}

	public void attachQuest(@NotNull Quest quest) {
		forEach(requirement -> requirement.attach(quest));
	}

	public void detachQuest() {
		forEach(requirement -> requirement.detach());
	}

	public String getSizeString() {
		return getSizeString(size());
	}

	public @NotNull List<Map<String, Object>> serialize() {
		return SerializableObject.serializeList(this);
	}

	public static RequirementList deserialize(@NotNull List<Map<?, ?>> mapList) {
		return new RequirementList(SerializableObject.deserializeList(mapList, AbstractRequirement::deserialize));
	}

	public static String getSizeString(int size) {
		return Lang.requirements.quickFormat("amount", size);
	}

}
