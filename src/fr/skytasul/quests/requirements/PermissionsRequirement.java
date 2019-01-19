package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Utils;

public class PermissionsRequirement extends AbstractRequirement {

	public List<String> permissions = new ArrayList<>();
	public String message = null;
	
	public PermissionsRequirement() {
		super("permissionRequired");
	}

	public boolean test(Player p) {
		for (String perm : permissions){
			if (!p.hasPermission(perm)) return false;
		}
		return true;
	}
	
	public void sendReason(Player p){
		if (message != null) Utils.IsendMessage(p, message, true);
	}

	
	protected void save(Map<String, Object> datas) {
		datas.put("permissions", permissions);
		if (message != null) datas.put("message", message);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		permissions = (List<String>) savedDatas.get("permissions");
		if (savedDatas.containsKey("message")) message = (String) savedDatas.get("message");
	}

}
