package fr.skytasul.quests.utils.compatibility.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.boss.api.Boss;
import org.mineacademy.boss.api.event.BossDeathEvent;

import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;

public class BossAPI implements MobFactory<Boss> {

	public String getID() {
		return "boss";
	}

	private ItemStack item = ItemUtils.item(XMaterial.BLAZE_ROD, Lang.boss.toString());
	public ItemStack getFactoryItem() {
		return item;
	}

	public void itemClick(Player p, Consumer<Boss> run) {
		new PagedGUI<Boss>("List of Bosses", DyeColor.ORANGE, org.mineacademy.boss.api.BossAPI.getBosses(), null, x -> x.getName()) {
			public ItemStack getItemStack(Boss object) {
				return ItemUtils.item(XMaterial.mobItem(object.getType()), object.getName());
			}

			public void click(Boss existing, ItemStack item, ClickType clickType) {
				run.accept(existing);
			}
		}.create(p);
	}

	public Boss fromValue(String value) {
		return org.mineacademy.boss.api.BossAPI.getBoss(value);
	}

	public String getValue(Boss data) {
		return data.getName();
	}

	public String getName(Boss data) {
		return data.getName();
	}

	public EntityType getEntityType(Boss data) {
		return data.getType();
	}

	public List<String> getDescriptiveLore(Boss data) {
		return Arrays.asList(Lang.EntityType.format(MinecraftNames.getEntityName(data.getType())), "Health:" + data.getSettings().getHealth());
	}

	@EventHandler
	public void onBossDeath(BossDeathEvent e){
		LivingEntity en = e.getEntity();
		Player killer = en.getKiller();
		if (killer == null) return;
		callEvent(e, e.getBoss(), en, en.getKiller());
	}
}