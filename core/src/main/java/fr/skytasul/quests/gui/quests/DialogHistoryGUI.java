package fr.skytasul.quests.gui.quests;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.types.Dialogable;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.gui.quests.DialogHistoryGUI.WrappedDialogable;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Stream;

public class DialogHistoryGUI extends PagedGUI<WrappedDialogable> {

	private final Runnable end;

	public DialogHistoryGUI(@NotNull Quester quester, Quest quest, Runnable end) {
		super(quest.getName(), DyeColor.LIGHT_BLUE, Collections.emptyList(), x -> end.run(), null);
		this.end = end;

		Validate.isTrue(quester.hasQuestDatas(quest), "Quester must have started the quest");

		if (quest.hasOption(OptionStartDialog.class))
			objects.add(new WrappedDialogable(quest.getOption(OptionStartDialog.class)));

		getDialogableStream(quester.getQuestDatas(quest), quest)
			.map(WrappedDialogable::new)
			.forEach(objects::add);
	}

	@Override
	public ItemStack getItemStack(WrappedDialogable object) {
		return object.setMeta(ItemUtils.clearVisibleAttributes(XMaterial.WRITTEN_BOOK.parseItem()));
	}

	@Override
	public void click(WrappedDialogable existing, ItemStack item, ClickType clickType) {
		boolean changed = false;
		if (clickType.isLeftClick()) {
			if (existing.page > 0) {
				existing.page--;
				changed = true;
				QuestUtils.playPluginSound(QuestsPlugin.getPlugin().getAudiences().player(player),
						"ENTITY_BAT_TAKEOFF", 0.4f, 1.5f);
			}
		}else if (clickType.isRightClick()) {
			if (existing.page + 1 < existing.pages.size()) {
				existing.page++;
				changed = true;
				QuestUtils.playPluginSound(QuestsPlugin.getPlugin().getAudiences().player(player),
						"ENTITY_BAT_TAKEOFF", 0.4f, 1.7f);
			}
		}

		if (changed) existing.setMeta(item);
	}

	@Override
	public CloseBehavior onClose(Player p) {
		QuestUtils.runSync(end);
		return StandardCloseBehavior.NOTHING;
	}

	public static Stream<Dialogable> getDialogableStream(PlayerQuestDatas datas, Quest quest) {
		return datas.getQuestFlowStages()
				.map(StageController::getStage)
				.filter(Dialogable.class::isInstance)
				.map(Dialogable.class::cast)
				.filter(Dialogable::hasDialog);
	}

	class WrappedDialogable {
		static final int MAX_LINES = 9;

		final Dialogable dialogable;
		final List<Page> pages;

		int page = 0;

		WrappedDialogable(Dialogable dialogable) {
			this.dialogable = dialogable;

			List<Message> messages = dialogable.getDialog().getMessages();
			List<List<String>> lines = new ArrayList<>(messages.size());
			for (int i = 0; i < messages.size(); i++) {
				Message msg = messages.get(i);
				String formatted = msg.formatMessage(player, dialogable.getNPC(),
						dialogable.getDialog().getNPCName(dialogable.getNPC()), i, messages.size());
				lines.add(ChatColorUtils.wordWrap(formatted, 40, 100));
			}

			if (lines.isEmpty()) {
				pages = Arrays.asList(new Page());
				return;
			}

			pages = new ArrayList<>();
			Page page = new Page();
			int messagesAdded = 0;
			int messagesInPage = 0;
			for (int i = 0; i < lines.size(); i++) {
				List<String> msg = lines.get(i);
				boolean last = i + 1 == lines.size();
				boolean pageFull = !page.lines.isEmpty() && page.lines.size() + msg.size() > MAX_LINES;
				if (QuestsConfiguration.getConfig().getDialogsConfig().getMaxMessagesPerHistoryPage() > 0)
					pageFull |= messagesInPage >= QuestsConfiguration.getConfig().getDialogsConfig()
							.getMaxMessagesPerHistoryPage();

				if (last || pageFull) {
					// means the page currently in writing must be flushed
					boolean added = false;
					if (page.lines.isEmpty() || (last && !pageFull)) {
						// means the current message must be added to the page before it is flushed
						page.lines.addAll(msg);
						messagesAdded++;
						messagesInPage++;
						added = true;
					}
					page.header = "§7§l" + messagesAdded + "§8 / §7§l"
							+ Lang.AmountDialogLines.quickFormat("lines_amount", messages.size());
					page.lines.addLast("  " + (pages.isEmpty() ? "§8" : "§7") + "◀ " + Lang.ClickLeft + " §8/ "
							+ (last && !pageFull ? "§8" : "§7") + Lang.ClickRight + " ▶");
					pages.add(page);
					page = new Page();
					messagesInPage = 0;
					if (added) // means the message has already been added to the page
						continue;
				}
				messagesAdded++;
				messagesInPage++;
				page.lines.addAll(msg);
			}

			if (!page.lines.isEmpty()) {
				page.header = "§7§l" + messagesAdded + "§8 / §7§l"
						+ Lang.AmountDialogLines.quickFormat("lines_amount", messages.size());
				page.lines.addLast(
						"  " + (pages.isEmpty() ? "§8" : "§7") + "◀ " + Lang.ClickLeft + " §8/ " + Lang.ClickRight + " ▶");
				pages.add(page);
			}
		}

		public Page getCurrentPage() {
			return pages.get(page);
		}

		public ItemStack setMeta(ItemStack item) {
			return ItemUtils.nameAndLore(
					item,
					"§8" + objects.indexOf(this) + " (" + dialogable.getNpcName() + "§8) - "
							+ getCurrentPage().header,
					getCurrentPage().lines);
		}
	}

	class Page {
		LinkedList<String> lines = new LinkedList<>();
		String header;
	}

}
