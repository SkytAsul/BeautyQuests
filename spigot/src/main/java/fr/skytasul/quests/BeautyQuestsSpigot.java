package fr.skytasul.quests;

import fr.skytasul.quests.api.utils.Version;
import fr.skytasul.quests.utils.compatibility.Paper;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.nms.SpigotNMS;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class BeautyQuestsSpigot extends BeautyQuests {

	private final Version serverVersion;

	public BeautyQuestsSpigot() {
		super(false);
		serverVersion = Version.parse(Bukkit.getBukkitVersion().split("-R")[0]);
	}

	@Override
	public boolean isRunningPaper() {
		return false;
	}

	@Override
	public @NotNull Version getServerVersion() {
		return serverVersion;
	}

	@Override
	public @NotNull Optional<Paper> getPaperCompatibility() {
		return Optional.empty();
	}

	@Override
	protected @Nullable NMS createInternalsAccess() {
		if (serverVersion.isAfter(26, 1, 0)) {
			try {
				return new SpigotNMS();
			} catch (ReflectiveOperationException ex) {
				logger.severe("Failed to load internals compatibility for Spigot {0}", ex, serverVersion);
				return null;
			}
		} else {
			return NMS.createVersionedNms();
		}
	}

}
