package fr.skytasul.quests.gui.quests;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;

public class ChooseQuestGUI extends PagedGUI<Quest> {
	
	private @NotNull Consumer<Quest> run;
	private @Nullable Runnable cancel;

	private ChooseQuestGUI(@NotNull Collection<@NotNull Quest> quests, @NotNull Consumer<@NotNull Quest> run,
			@Nullable Runnable cancel) {
		super(Lang.INVENTORY_CHOOSE.toString(), DyeColor.MAGENTA, quests);
		super.objects.sort(Comparator.naturalOrder());
		
		this.run = Objects.requireNonNull(run);
		this.cancel = cancel;
	}

	@Override
	public ItemStack getItemStack(Quest object) {
		return ItemUtils.nameAndLore(object.getQuestItem().clone(), ChatColor.YELLOW + object.getName(), object.getDescription());
	}

	@Override
	public void click(Quest existing, ItemStack item, ClickType clickType) {
		close(player);
		run.accept(existing);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		if (cancel != null)
			return new DelayCloseBehavior(cancel);
		return StandardCloseBehavior.REMOVE;
	}

	public static void choose(@NotNull Player player, @NotNull Collection<@NotNull Quest> quests,
			@NotNull Consumer<@Nullable Quest> run, @Nullable Runnable cancel, boolean canSkip) {
		choose(player, quests, run, cancel, canSkip, null);
	}

	public static void choose(@NotNull Player player, @NotNull Collection<@NotNull Quest> quests,
			@NotNull Consumer<@Nullable Quest> run, @Nullable Runnable cancel, boolean canSkip,
			@Nullable Consumer<@NotNull ChooseQuestGUI> guiConsumer) {
		if (quests.isEmpty()) {
			if (cancel != null)
				cancel.run();
		} else if (quests.size() == 1 && canSkip
				&& QuestsConfiguration.getConfig().getQuestsConfig().skipNpcGuiIfOnlyOneQuest()) {
			run.accept(quests.iterator().next());
		} else {
			ChooseQuestGUI gui = new ChooseQuestGUI(quests, run, cancel);
			if (guiConsumer != null)
				guiConsumer.accept(gui);
			gui.open(player);
		}
	}

}