package fr.skytasul.quests.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Vault;

public class PermissionReward extends AbstractReward {
	
	public final Map<String, Boolean> permissions = new HashMap<>();

	public PermissionReward(){
		super("permReward");
		if (!Dependencies.vault) throw new MissingDependencyException("Vault");
	}
	
	public PermissionReward(Map<String, Boolean> perms){
		this();
		this.permissions.putAll(perms);
	}

	public String give(Player p){
		for (Entry<String, Boolean> en : permissions.entrySet()) {
			Vault.changePermission(p, en.getKey(), en.getValue());
		}	
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("permissions", permissions);
	}

	protected void load(Map<String, Object> savedDatas){
		if (savedDatas.containsKey("perm")) {
			permissions.put((String) savedDatas.get("perm"), false);
		}else permissions.putAll((Map<String, Boolean>) savedDatas.get("permissions"));
	}

}
