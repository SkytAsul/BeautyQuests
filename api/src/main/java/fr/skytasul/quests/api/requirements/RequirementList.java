package fr.skytasul.quests.api.requirements;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RequirementList extends ArrayList<@NotNull AbstractRequirement> {

	private static final long serialVersionUID = 5568034962195448395L;

	public RequirementList() {}

	public RequirementList(@NotNull Collection<@NotNull AbstractRequirement> requirements) {
		super(requirements);
	}

	public @NotNull MatchResult allMatch(@NotNull Player p) {
		boolean match = true;
		for (AbstractRequirement requirement : this) {
			try {
				if (!requirement.isValid() || !requirement.test(p)) {
					String reason = requirement.getReason(p);
					if (reason != null)
						return new MatchResult(false, reason);

					// if we are here, it means a reason has not yet been sent
					// so we continue until a reason is sent OR there is no more requirement
					match = false;
				}
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
				return new MatchResult(false, "error");
			}
		}
		return new MatchResult(match, null);
	}

	public boolean allMatch(@NotNull Player p, boolean message) {
		var result = allMatch(p);
		if (message && result.reason() != null)
			MessageUtils.sendMessage(p, result.reason(), MessageType.DefaultMessageType.PREFIXED);
		return result.result();
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

	public record MatchResult(boolean result, @Nullable String reason) {
	}

}
