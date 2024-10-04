package fr.skytasul.quests.integrations.mobs;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.Utils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.boss.api.BossAPI;
import org.mineacademy.boss.api.event.BossDeathEvent;
import org.mineacademy.boss.model.Boss;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class BQBoss implements MobFactory<Boss>, Listener {

	@Override
	public String getID() {
		return "boss";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_ROD, Lang.boss.toString());
	@Override
	public ItemStack getFactoryItem() {
		return item;
	}

	@Override
	public void itemClick(Player p, Consumer<Boss> run) {
		new PagedGUI<Boss>("List of Bosses", DyeColor.ORANGE, org.mineacademy.boss.api.BossAPI.getBosses(), null, x -> x.getName()) {
			@Override
			public ItemStack getItemStack(Boss object) {
				return ItemUtils.item(Utils.mobItem(object.getType()), object.getName());
			}

			@Override
			public void click(Boss existing, ItemStack item, ClickType clickType) {
				close();
				run.accept(existing);
			}
		}.open(p);
	}

	@Override
	public Boss fromValue(String value) {
		return org.mineacademy.boss.api.BossAPI.getBoss(value);
	}
	
	@Override
	public boolean bukkitMobApplies(Boss first, Entity entity) {
		return BossAPI.getBoss(entity).getBoss().equals(first);
	}

	@Override
	public String getValue(Boss data) {
		return data.getName();
	}

	@Override
	public String getName(Boss data) {
		return data.getName();
	}

	@Override
	public EntityType getEntityType(Boss data) {
		return data.getType();
	}

	@Override
	public List<String> getDescriptiveLore(Boss data) {
		return Arrays.asList(
				Lang.EntityType.quickFormat("entity_type", MinecraftNames.getEntityName(data.getType())),
				"§8Health: §7§l" + data.getDefaultHealth());
	}

	@EventHandler
	public void onBossDeath(BossDeathEvent e){
		LivingEntity en = e.getEntity();
		Player killer = en.getKiller();
		if (killer == null) return;
		callEvent(e, e.getBoss(), en, en.getKiller());
	}
}