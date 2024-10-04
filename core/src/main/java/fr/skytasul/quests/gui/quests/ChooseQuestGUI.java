package fr.skytasul.quests.gui.quests;

import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

public class ChooseQuestGUI extends PagedGUI<Quest> {

	private @NotNull Consumer<Quest> run;
	private @Nullable Runnable cancel;

	public ChooseQuestGUI(@NotNull Collection<@NotNull Quest> quests, @NotNull Consumer<@NotNull Quest> run,
			@Nullable Runnable cancel) {
		super(Lang.INVENTORY_CHOOSE.toString(), DyeColor.MAGENTA, quests);

		this.run = Objects.requireNonNull(run);
		this.cancel = cancel;

		Collections.sort(objects); // to have quests in ID ordering
	}

	@Override
	public ItemStack getItemStack(Quest object) {
		return ItemUtils.nameAndLore(object.getQuestItem().clone(), Lang.formatId.format(object),
				"§7" + object.getDescription());
	}

	@Override
	public void click(Quest existing, ItemStack item, ClickType clickType) {
		close(player);
		QuestUtils.runSync(() -> run.accept(existing));
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		if (cancel != null)
			return new DelayCloseBehavior(cancel);
		return StandardCloseBehavior.REMOVE;
	}

}