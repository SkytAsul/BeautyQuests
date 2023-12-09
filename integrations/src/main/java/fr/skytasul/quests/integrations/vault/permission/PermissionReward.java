package fr.skytasul.quests.integrations.vault.permission;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class PermissionReward extends AbstractReward {
	
	public List<Permission> permissions;

	public PermissionReward(){
		this(null, new ArrayList<>());
	}

	public PermissionReward(String customDescription, List<Permission> permissions) {
		super(customDescription);
		this.permissions = permissions;
	}

	@Override
	public List<String> give(Player p) {
		for (Permission perm : permissions) {
			perm.give(p);
		}
		return null;
	}

	@Override
	public AbstractReward clone() {
		return new PermissionReward(getCustomDescription(), new ArrayList<>(permissions));
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("permissions_amount", permissions.size());
	}
	
	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescriptionAsValue(Lang.AmountPermissions.format(this));
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new PermissionListGUI(permissions, permissions -> {
			PermissionReward.this.permissions = permissions;
			event.reopenGUI();
		}).open(event.getPlayer());
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("perms", Utils.serializeList(permissions, Permission::serialize));
	}

	@Override
	public void load(ConfigurationSection section){
		super.load(section);
		permissions.addAll(Utils.deserializeList(section.getMapList("perms"), Permission::deserialize));
	}
	
}
