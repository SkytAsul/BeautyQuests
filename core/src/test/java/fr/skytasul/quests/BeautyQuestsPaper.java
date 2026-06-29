package fr.skytasul.quests;

import org.jetbrains.annotations.Nullable;

import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.nms.NullNMS;

public class BeautyQuestsPaper extends BeautyQuests {

    public BeautyQuestsPaper() {
        super(true);
    }

    @Override
    protected @Nullable NMS createInternalsAccess() {
        return new NullNMS();
    }

}
