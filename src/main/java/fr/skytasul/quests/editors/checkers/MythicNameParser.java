package fr.skytasul.quests.editors.checkers;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class MythicNameParser implements fr.skytasul.quests.editors.checkers.AbstractParser {

	public Object parse(Player p, String msg) throws Throwable{
		MythicMob mm = MythicMobs.inst().getMobManager().getMythicMob(msg);
		if (mm == null){
			Utils.sendMessage(p, Lang.MYTHICMOB_NOT_EXISTS.toString() + " " + Lang.TYPE_CANCEL.toString());
			return null;
		}
		return mm;
	}

}
