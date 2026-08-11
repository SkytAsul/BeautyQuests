package fr.skytasul.quests.gui.mobs;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.parsers.NumberParser;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.mobs.LeveledMobFactory;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.mobs.Mob;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
		if (click == ClickType.RIGHT) {
			QuestsPlugin.getPlugin().getEditorManager().getFactory()
					.createTextEditorBuilderString(getViewer(), super::reopen, name -> {
						mob.getObject().setCustomName(name);
						setItems();
						reopen();
					})
					.allowEmpty()
					.setInitialString(mob.getObject().getCustomName())
					.setIndication(Lang.MOB_NAME.toString())
					.build().start();
		} else if (click == ClickType.LEFT) {
			QuestsPlugin.getPlugin().getEditorManager().getFactory()
					.createTextEditorBuilderParser(getViewer(), NumberParser.INTEGER_PARSER_STRICT_POSITIVE,
							super::reopen, amount -> {
								mob.setAmount(amount);
								setItems();
								reopen();
							})
					.setInitialString(Integer.toString(mob.getAmount()))
					.setIndication(Lang.MOB_AMOUNT.toString())
					.build().start();
		} else if (click == ClickType.SHIFT_RIGHT) {
			if (mob.getObject().getFactory() instanceof LeveledMobFactory) {
				QuestsPlugin.getPlugin().getEditorManager().getFactory()
						.createTextEditorBuilderParser(getViewer(), new NumberParser<>(Double.class, true, false),
								super::reopen, level -> {
									mob.getObject().setMinLevel(level);
									setItems();
									reopen();
								})
						.setInitialString(
								mob.getObject().getMinLevel() == null ? null : mob.getObject().getMinLevel().toString())
						.build().start();
			} else {
				QuestUtils.playPluginSound(getViewer(), "ENTITY_VILLAGER_NO", 0.6f);
			}
		}
	}

	@Override
	public void createObject(Function<MutableCountableObject<Mob<?>>, ItemStack> callback) {
		new MobSelectionGUI(mob -> {
			if (mob == null) {
				reopen();
			} else {
				UUID uuid = UUID.nameUUIDFromBytes(mob.serialize().toString().getBytes());
				callback.apply(CountableObject.createMutable(uuid, mob, 1));
			}
		}).open(getViewer());
	}

	@Override
	public ItemStack getObjectItemStack(MutableCountableObject<Mob<?>> mob) {
		LoreBuilder loreBuilder = createLoreBuilder(mob)
				.addDescription(Lang.Amount.format(mob))
				.addClick(ClickType.LEFT, Lang.editAmount.toString())
				.addClick(ClickType.RIGHT, Lang.editMobName.toString())
				.addClick(ClickType.SHIFT_RIGHT, (mob.getObject().getFactory() instanceof LeveledMobFactory ? "" : "§8§m")
						+ Lang.setLevel.toString());
		ItemStack item = ItemUtils.item(mob.getObject().getMobItem(), mob.getObject().getName(), loreBuilder.toLoreArray());
		item.setAmount(Math.min(mob.getAmount(), 64));
		return item;
	}

}