package fr.skytasul.quests;

import fr.skytasul.quests.api.utils.Version;
import fr.skytasul.quests.utils.compatibility.Paper;
import fr.skytasul.quests.utils.compatibility.PaperImplementation;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.nms.PaperNMS;
import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class BeautyQuestsPaper extends BeautyQuests {

	private final Version serverVersion;
	private final PaperImplementation paperCompat;

	public BeautyQuestsPaper() {
		paperCompat = new PaperImplementation();

		String minecraftVersion;
		try {
			minecraftVersion = ServerBuildInfo.buildInfo().minecraftVersionId();
		} catch (NoClassDefFoundError ex) {
			// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
			minecraftVersion = Bukkit.getBukkitVersion().split("-R")[0];
		}
		serverVersion = Version.parse(minecraftVersion);
	}

	@Override
	public boolean isRunningPaper() {
		return true;
	}

	@Override
	public @NotNull Version getServerVersion() {
		return serverVersion;
	}

	@Override
	public @NotNull Optional<Paper> getPaperCompatibility() {
		return Optional.of(paperCompat);
	}

	@Override
	protected @Nullable NMS createInternalsAccess() {
		if (serverVersion.isAfter(1, 20, 5)) {
			try {
				return new PaperNMS();
			} catch (ReflectiveOperationException ex) {
				logger.severe("Failed to load internals compatibility for Paper {0}", ex, serverVersion);
				return null;
			}
		} else {
			return NMS.createVersionedNms();
		}
	}

}
