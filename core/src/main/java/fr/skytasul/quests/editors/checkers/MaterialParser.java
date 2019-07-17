package fr.skytasul.quests.editors.checkers;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;
import fr.skytasul.quests.utils.nms.NMS;

public class MaterialParser implements AbstractParser {

	private boolean item;
	
	public MaterialParser(boolean item) {
		this.item = item;
	}
	
	
	public Object parse(Player p, String msg) throws Throwable{
		XMaterial tmp = XMaterial.fromString(msg);
		if (tmp == null){
			Material mat = Material.matchMaterial(msg);
			if (mat != null) tmp = XMaterial.fromString(mat.name());
			if (tmp == null) Lang.UNKNOWN_ITEM_TYPE.send(p);
		}else if (item && (NMS.getMCVersion() >= 13 && !Post1_13.isItem(tmp.parseMaterial()))){
			Lang.INVALID_ITEM_TYPE.send(p);
			return null;
		}
		return tmp;
	}

}
