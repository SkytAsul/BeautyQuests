package fr.skytasul.quests.rewards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.permissions.PermissionListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionReward extends AbstractReward {
	
	public List<Permission> permissions;

	public PermissionReward(){
		this(new ArrayList<>());
	}

	public PermissionReward(List<Permission> permissions) {
		super("permReward");
		if (!DependenciesManager.vault.isEnabled()) throw new MissingDependencyException("Vault");
		this.permissions = permissions;
	}

	public List<String> give(Player p) {
		for (Permission perm : permissions) {
			perm.give(p);
		}
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new PermissionReward(new ArrayList<>(permissions));
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + permissions.size() + " permissions", "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PermissionListGUI(permissions, permissions -> {
			PermissionReward.this.permissions = permissions;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}).create(p);
	}
	
	protected void save(Map<String, Object> datas){
		datas.put("perms", Utils.serializeList(permissions, Permission::serialize));
	}

	protected void load(Map<String, Object> savedDatas){
		permissions.addAll(Utils.deserializeList((List<Map<String, Object>>) savedDatas.get("perms"), Permission::deserialize));
	}
	
}
