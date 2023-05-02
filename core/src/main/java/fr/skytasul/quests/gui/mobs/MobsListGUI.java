package fr.skytasul.quests.gui.mobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.mobs.Mob;
import fr.skytasul.quests.utils.QuestUtils;

public class MobsListGUI extends ListGUI<MutableCountableObject<Mob<?>>> {

	private Consumer<List<MutableCountableObject<Mob<?>>>> end;

	public MobsListGUI(Collection<MutableCountableObject<Mob<?>>> objects,
			Consumer<List<MutableCountableObject<Mob<?>>>> end) {
		super(Lang.INVENTORY_MOBS.toString(), DyeColor.ORANGE, objects);
		this.end = end;
	}
	
	@Override
	public void finish(List<MutableCountableObject<Mob<?>>> objects) {
		end.accept(objects);
	}

	@Override
	public void clickObject(MutableCountableObject<Mob<?>> mob, ItemStack item, ClickType click) {
		super.clickObject(mob, item, click);
		if (click == ClickType.SHIFT_LEFT) {
			Lang.MOB_NAME.send(player);
			new TextEditor<>(player, super::reopen, name -> {
				mob.getObject().setCustomName((String) name);
				setItems();
				reopen();
			}).passNullIntoEndConsumer().start();
		} else if (click == ClickType.LEFT) {
			Lang.MOB_AMOUNT.send(player);
			new TextEditor<>(player, super::reopen, amount -> {
				mob.setAmount(amount);
				setItems();
				reopen();
			}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).start();
		} else if (click == ClickType.SHIFT_RIGHT) {
			if (mob.getObject().getFactory() instanceof LeveledMobFactory) {
				new TextEditor<>(player, super::reopen, level -> {
					mob.getObject().setMinLevel(level);
					setItems();
					reopen();
				}, new NumberParser<>(Double.class, true, false)).start();
			} else {
				QuestUtils.playPluginSound(player.getLocation(), "ENTITY_VILLAGER_NO", 0.6f);
			}
		} else if (click == ClickType.RIGHT) {
			remove(mob);
		}
	}

	@Override
	public void createObject(Function<MutableCountableObject<Mob<?>>, ItemStack> callback) {
		new MobSelectionGUI(mob -> {
			if (mob == null)
				reopen();
			else
				callback.apply(CountableObject.createMutable(UUID.randomUUID(), mob, 1));
		}).open(player);
	}

	@Override
	public ItemStack getObjectItemStack(MutableCountableObject<Mob<?>> mob) {
		List<String> lore = new ArrayList<>();
		lore.add(Lang.Amount.format(mob.getAmount()));
		lore.addAll(mob.getObject().getDescriptiveLore());
		lore.add("");
		lore.add(Lang.click.toString());
		if (mob.getObject().getFactory() instanceof LeveledMobFactory) {
			lore.add("§7" + Lang.ClickShiftRight + " > §e" + Lang.setLevel);
		} else {
			lore.add("§8§n" + Lang.ClickShiftRight + " > " + Lang.setLevel);
		}
		ItemStack item = ItemUtils.item(mob.getObject().getMobItem(), mob.getObject().getName(), lore);
		item.setAmount(Math.min(mob.getAmount(), 64));
		return item;
	}

}