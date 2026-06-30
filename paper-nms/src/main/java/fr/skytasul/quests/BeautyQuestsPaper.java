package fr.skytasul.quests;

import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.nms.PaperNMS;
import org.jetbrains.annotations.Nullable;

public class BeautyQuestsPaper extends BeautyQuests {

	public BeautyQuestsPaper() {
		super(false);
	}

	@Override
	protected @Nullable NMS createInternalsAccess() {
		try {
			return new PaperNMS();
		} catch (ReflectiveOperationException ex) {
			logger.severe("Failed to load internals compatibility for Paper {0}", ex, getServerVersion());
			return null;
		}
	}

}
