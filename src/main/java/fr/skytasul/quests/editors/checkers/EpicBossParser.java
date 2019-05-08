package fr.skytasul.quests.editors.checkers;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.mobs.EpicBosses;

public class EpicBossParser implements fr.skytasul.quests.editors.checkers.AbstractParser {

	public Object parse(Player p, String msg) throws Throwable{
		if (!EpicBosses.bossExists(msg)){
			Utils.sendMessage(p, Lang.EPICBOSS_NOT_EXISTS.toString() + " " + Lang.TYPE_CANCEL.toString());
			return null;
		}
		return msg;
	}

}
