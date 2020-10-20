package fr.skytasul.quests.editors.checkers;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;
import fr.skytasul.quests.utils.nms.NMS;

public class MaterialParser implements AbstractParser<XMaterial> {

	private boolean item, block;
	
	public MaterialParser(boolean item, boolean block) {
		this.item = item;
		this.block = block;
	}
	
	public XMaterial parse(Player p, String msg) throws Throwable {
		XMaterial tmp = XMaterial.matchXMaterial(msg).orElse(null);
		if (tmp == null){
			Material mat = Material.matchMaterial(msg);
			if (mat != null) tmp = XMaterial.matchXMaterial(mat);
			if (tmp == null) {
				if (block) {
					Lang.UNKNOWN_BLOCK_TYPE.send(p);
				}else Lang.UNKNOWN_ITEM_TYPE.send(p);
			}
		}else if (item) {
			if (NMS.getMCVersion() >= 13 && !Post1_13.isItem(tmp.parseMaterial())) {
				Lang.INVALID_ITEM_TYPE.send(p);
				return null;
			}
		}else if (block) {
			if (NMS.getMCVersion() >= 13 && !Post1_13.isBlock(tmp.parseMaterial())) {
				Lang.INVALID_BLOCK_TYPE.send(p);
				return null;
			}
		}
		return tmp;
	}

}
